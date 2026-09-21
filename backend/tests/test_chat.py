import hashlib
import json

import pytest
from httpx import ASGITransport, AsyncClient
from starlette.testclient import TestClient

from app.main import app
from app.services.consent_service import consent_service


@pytest.mark.asyncio
async def test_conversation_consent_gated_creation():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        profile_id = "00000000-0000-0000-0000-000000000701"
        doctor_id = "00000000-0000-0000-0000-000000000702"

        # Attempt to start conversation without consent -> 403
        no_consent_resp = await ac.post(
            "/api/v1/conversations",
            json={"profile_id": profile_id, "participant_doctor_id": doctor_id},
        )
        assert no_consent_resp.status_code == 403

        # Grant consent
        consent_service.create_grant(
            grantor_profile_id=profile_id,
            grantee_doctor_id=doctor_id,
            scopes=["SCOPE_ADVICE_HISTORY", "SCOPE_LAB_REPORTS"],
            duration_hours=24,
        )

        # Now starting conversation succeeds -> 201
        create_resp = await ac.post(
            "/api/v1/conversations",
            json={"profile_id": profile_id, "participant_doctor_id": doctor_id},
        )
        assert create_resp.status_code == 201
        conv_data = create_resp.json()
        assert conv_data["profile_id"] == profile_id
        assert conv_data["participant_doctor_id"] == doctor_id


@pytest.mark.asyncio
async def test_messaging_and_advice_promotion():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        profile_id = "00000000-0000-0000-0000-000000000801"
        doctor_id = "00000000-0000-0000-0000-000000000802"

        # Setup consent and conversation
        consent_service.create_grant(
            grantor_profile_id=profile_id,
            grantee_doctor_id=doctor_id,
            scopes=["SCOPE_ADVICE_HISTORY"],
            duration_hours=48,
        )
        conv_resp = await ac.post(
            "/api/v1/conversations",
            json={"profile_id": profile_id, "participant_doctor_id": doctor_id},
        )
        conv_id = conv_resp.json()["id"]

        # Patient sends a message
        msg1_resp = await ac.post(
            f"/api/v1/conversations/{conv_id}/messages",
            json={
                "sender_id": profile_id,
                "sender_role": "patient",
                "body": "Hello Doctor, my morning fasting blood sugar was 132 mg/dL.",
            },
        )
        assert msg1_resp.status_code == 201

        # Doctor replies with clinical instruction
        doc_msg_resp = await ac.post(
            f"/api/v1/conversations/{conv_id}/messages",
            json={
                "sender_id": doctor_id,
                "sender_role": "doctor",
                "body": (
                    "Increase Metformin to 1000mg with breakfast. Log readings daily."
                ),
            },
        )
        assert doc_msg_resp.status_code == 201
        doc_msg = doc_msg_resp.json()
        doc_msg_id = doc_msg["id"]
        assert doc_msg["promoted_to_advice_id"] is None

        # Retrieve messages and verify chronological order
        msgs_resp = await ac.get(f"/api/v1/conversations/{conv_id}/messages")
        assert msgs_resp.status_code == 200
        msgs = msgs_resp.json()
        assert len(msgs) == 2
        assert msgs[0]["sender_role"] == "patient"
        assert msgs[1]["sender_role"] == "doctor"

        # Promote doctor's message to Advice Ledger
        promote_resp = await ac.post(
            f"/api/v1/conversations/{conv_id}/messages/{doc_msg_id}/promote-to-advice",
            json={"signature": "valid_doctor_ecdsa_p256_signature"},
        )
        assert promote_resp.status_code == 201
        advice = promote_resp.json()
        assert advice["origin"] == "doctor_signed"
        assert advice["body"] == doc_msg["body"]
        assert advice["prev_hash"] == "0" * 64

        # Validate hash chain calculation
        body_dict = {
            "id": advice["id"],
            "profile_id": profile_id,
            "doctor_id": doctor_id,
            "body": doc_msg["body"],
            "origin": "doctor_signed",
            "prev_hash": "0" * 64,
            "version": 1,
        }
        canonical = json.dumps(body_dict, sort_keys=True)
        expected_hash = hashlib.sha256(
            (("0" * 64) + canonical).encode("utf-8")
        ).hexdigest()
        assert advice["hash"] == expected_hash

        # Verify double promotion fails
        double_resp = await ac.post(
            f"/api/v1/conversations/{conv_id}/messages/{doc_msg_id}/promote-to-advice"
        )
        assert double_resp.status_code == 400


@pytest.mark.asyncio
async def test_consent_revocation_blocks_chat():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        profile_id = "00000000-0000-0000-0000-000000000901"
        doctor_id = "00000000-0000-0000-0000-000000000902"

        grant = consent_service.create_grant(
            grantor_profile_id=profile_id,
            grantee_doctor_id=doctor_id,
            scopes=["SCOPE_ADVICE_HISTORY"],
            duration_hours=12,
        )
        conv_resp = await ac.post(
            "/api/v1/conversations",
            json={"profile_id": profile_id, "participant_doctor_id": doctor_id},
        )
        conv_id = conv_resp.json()["id"]

        # Revoke consent immediately
        consent_service.revoke_grant(grant.id)

        # Message dispatch must now be rejected with 403 (<5s per NN-6)
        blocked_msg = await ac.post(
            f"/api/v1/conversations/{conv_id}/messages",
            json={
                "sender_id": profile_id,
                "sender_role": "patient",
                "body": "Should I still take this medicine?",
            },
        )
        assert blocked_msg.status_code == 403


def test_websocket_chat_echo():
    client = TestClient(app)
    conv_id = "00000000-0000-0000-0000-000000000999"
    with client.websocket_connect(f"/api/v1/ws/chat/{conv_id}") as websocket:
        websocket.send_text("ping")
        data = websocket.receive_json()
        assert data["type"] == "chat_ping"
        assert data["raw"] == "ping"
