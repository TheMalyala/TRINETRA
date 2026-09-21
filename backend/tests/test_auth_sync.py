import pyotp
import pytest
from httpx import ASGITransport, AsyncClient

from app.core.security import verify_password
from app.main import app
from app.services.auth_service import auth_service
from app.services.consent_service import consent_service


@pytest.mark.asyncio
async def test_registration_and_argon2id():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        payload = {
            "email": "test.patient@example.com",
            "password": "SecurePassword123!",
            "role": "patient",
        }
        resp = await ac.post("/api/v1/auth/register", json=payload)
        assert resp.status_code == 201
        data = resp.json()
        assert "user_id" in data
        assert "access_token" in data
        assert "refresh_token" in data

        # Verify Argon2id hash in auth service
        user = auth_service.users.get("test.patient@example.com")
        assert user is not None
        assert user.pw_hash.startswith("$argon2id$")
        assert verify_password("SecurePassword123!", user.pw_hash)

        # Duplicate email registration must fail
        dup_resp = await ac.post("/api/v1/auth/register", json=payload)
        assert dup_resp.status_code == 400


@pytest.mark.asyncio
async def test_login_and_token_rotation():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # Register user
        reg_payload = {
            "email": "login.user@example.com",
            "password": "LoginSecret456!",
            "role": "patient",
        }
        await ac.post("/api/v1/auth/register", json=reg_payload)

        # Successful login
        login_payload = {
            "email": "login.user@example.com",
            "password": "LoginSecret456!",
        }
        login_resp = await ac.post("/api/v1/auth/login", json=login_payload)
        assert login_resp.status_code == 200
        tokens = login_resp.json()
        old_refresh = tokens["refresh_token"]

        # Failed login
        bad_login = {
            "email": "login.user@example.com",
            "password": "WrongPassword!",
        }
        bad_resp = await ac.post("/api/v1/auth/login", json=bad_login)
        assert bad_resp.status_code == 401

        # Token refresh
        refresh_resp = await ac.post(
            "/api/v1/auth/refresh", json={"refresh_token": old_refresh}
        )
        assert refresh_resp.status_code == 200
        new_tokens = refresh_resp.json()
        assert new_tokens["refresh_token"] != old_refresh

        # Single-use rotation check: old refresh token must be invalidated
        stale_resp = await ac.post(
            "/api/v1/auth/refresh", json={"refresh_token": old_refresh}
        )
        assert stale_resp.status_code == 401


@pytest.mark.asyncio
async def test_totp_flow():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # Register user
        reg_resp = await ac.post(
            "/api/v1/auth/register",
            json={
                "email": "totp.user@example.com",
                "password": "Password789!",
                "role": "patient",
            },
        )
        user_id = reg_resp.json()["user_id"]

        # TOTP setup
        setup_resp = await ac.post(f"/api/v1/auth/totp/setup?user_id={user_id}")
        assert setup_resp.status_code == 200
        setup_data = setup_resp.json()
        secret = setup_data["secret"]
        assert len(secret) == 32

        # Generate valid 6-digit TOTP code
        totp = pyotp.TOTP(secret)
        current_code = totp.now()

        # Verify and enable
        verify_resp = await ac.post(
            "/api/v1/auth/totp/verify",
            json={"user_id": user_id, "code": current_code},
        )
        assert verify_resp.status_code == 200
        assert verify_resp.json()["verified"] is True

        # Login without TOTP should require TOTP
        login_resp = await ac.post(
            "/api/v1/auth/login",
            json={
                "email": "totp.user@example.com",
                "password": "Password789!",
            },
        )
        assert login_resp.status_code == 200
        assert login_resp.json()["requires_totp"] is True

        # Login with valid TOTP succeeds
        login_with_totp = await ac.post(
            "/api/v1/auth/login",
            json={
                "email": "totp.user@example.com",
                "password": "Password789!",
                "totp_code": totp.now(),
            },
        )
        assert login_with_totp.status_code == 200
        assert login_with_totp.json()["requires_totp"] is False


@pytest.mark.asyncio
async def test_sync_outbox_idempotency_and_cursor():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        op1 = {
            "operation_id": "00000000-0000-0000-0000-000000000010",
            "entity_type": "observation",
            "entity_id": "00000000-0000-0000-0000-000000000011",
            "operation": "INSERT",
            "payload": {"name": "HbA1c", "value": 6.2},
            "client_timestamp": "2026-09-22T00:00:00Z",
        }
        op2 = {
            "operation_id": "00000000-0000-0000-0000-000000000020",
            "entity_type": "document",
            "entity_id": "00000000-0000-0000-0000-000000000021",
            "operation": "INSERT",
            "payload": {"type": "lab", "sha256": "abc123hash"},
            "client_timestamp": "2026-09-22T00:00:00Z",
        }

        # First push
        push_resp = await ac.post(
            "/api/v1/sync/push", json={"operations": [op1, op2]}
        )
        assert push_resp.status_code == 200
        data = push_resp.json()
        assert data["processed_count"] == 2
        seq1 = data["server_seq"]

        # Duplicate push with same operation_ids: must be ignored (idempotency)
        dup_push = await ac.post(
            "/api/v1/sync/push", json={"operations": [op1, op2]}
        )
        assert dup_push.status_code == 200
        assert dup_push.json()["processed_count"] == 0

        # Pull from seq 0
        pull_resp = await ac.get("/api/v1/sync/pull?cursor=0")
        assert pull_resp.status_code == 200
        pull_data = pull_resp.json()
        assert len(pull_data["changes"]) >= 2

        # Pull from current seq: should yield zero new changes
        empty_pull = await ac.get(f"/api/v1/sync/pull?cursor={seq1}")
        assert empty_pull.status_code == 200
        assert len(empty_pull.json()["changes"]) == 0


@pytest.mark.asyncio
async def test_consent_creation_and_instant_revocation():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        grant_payload = {
            "grantor_profile_id": "00000000-0000-0000-0000-000000000100",
            "grantee_doctor_id": "00000000-0000-0000-0000-000000000200",
            "scopes": ["SCOPE_LAB_REPORTS", "SCOPE_PRESCRIPTIONS"],
            "duration_hours": 12,
        }
        create_resp = await ac.post("/api/v1/consents", json=grant_payload)
        assert create_resp.status_code == 201
        consent_data = create_resp.json()
        cid = consent_data["id"]

        # Verify active consent check
        assert consent_service.has_active_consent(
            grantor_profile_id="00000000-0000-0000-0000-000000000100",
            grantee_doctor_id="00000000-0000-0000-0000-000000000200",
            scope="SCOPE_LAB_REPORTS",
        )
        assert not consent_service.has_active_consent(
            grantor_profile_id="00000000-0000-0000-0000-000000000100",
            grantee_doctor_id="00000000-0000-0000-0000-000000000200",
            scope="SCOPE_IMAGING",
        )

        # Immediate revocation (<5s per NN-6)
        revoke_resp = await ac.post(f"/api/v1/consents/{cid}/revoke")
        assert revoke_resp.status_code == 200
        assert revoke_resp.json()["revoked"] is True

        # Now consent check must be False
        assert not consent_service.has_active_consent(
            grantor_profile_id="00000000-0000-0000-0000-000000000100",
            grantee_doctor_id="00000000-0000-0000-0000-000000000200",
            scope="SCOPE_LAB_REPORTS",
        )


@pytest.mark.asyncio
async def test_encrypted_backup_and_tamper_rejection():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # Register user
        reg_resp = await ac.post(
            "/api/v1/auth/register",
            json={
                "email": "backup.user@example.com",
                "password": "BackupPassword123!",
                "role": "patient",
            },
        )
        user_id = reg_resp.json()["user_id"]

        # Export backup
        export_resp = await ac.post(f"/api/v1/backup/export?user_id={user_id}")
        assert export_resp.status_code == 200
        envelope = export_resp.json()
        assert "data_hash" in envelope
        assert envelope["version"] == 1

        # Restore valid backup
        restore_resp = await ac.post("/api/v1/backup/restore", json=envelope)
        assert restore_resp.status_code == 200
        assert restore_resp.json()["restored"] is True

        # Tamper with payload (modify data without updating data_hash)
        tampered_envelope = dict(envelope)
        tampered_envelope["payload"] = {
            "profiles": [{"id": "tampered_id", "name": "Hacker"}]
        }
        tampered_resp = await ac.post(
            "/api/v1/backup/restore", json=tampered_envelope
        )
        assert tampered_resp.status_code == 400
        assert "Tampered backup" in tampered_resp.json()["detail"]
