import uuid
from datetime import datetime, timedelta, timezone
from typing import Dict, List, Optional

from pydantic import BaseModel


class ConsentRecord(BaseModel):
    id: str
    grantor_profile_id: str
    grantee_doctor_id: str
    scopes: List[str]
    starts_at: str
    expires_at: str
    revoked_at: Optional[str] = None


class ConsentService:
    """Manages scoped, time-boxed, revocable consent grants per NN-6."""

    def __init__(self):
        self.consents: Dict[str, ConsentRecord] = {}

    def create_grant(
        self,
        grantor_profile_id: str,
        grantee_doctor_id: str,
        scopes: List[str],
        duration_hours: int = 24,
    ) -> ConsentRecord:
        cid = str(uuid.uuid4())
        now = datetime.now(timezone.utc)
        expires = now + timedelta(hours=duration_hours)

        record = ConsentRecord(
            id=cid,
            grantor_profile_id=grantor_profile_id,
            grantee_doctor_id=grantee_doctor_id,
            scopes=scopes,
            starts_at=now.isoformat(),
            expires_at=expires.isoformat(),
            revoked_at=None,
        )
        self.consents[cid] = record
        return record

    def has_active_consent(
        self, grantor_profile_id: str, grantee_doctor_id: str, scope: str
    ) -> bool:
        now = datetime.now(timezone.utc)
        for consent in self.consents.values():
            if (
                consent.grantor_profile_id == grantor_profile_id
                and consent.grantee_doctor_id == grantee_doctor_id
            ):
                if consent.revoked_at is not None:
                    continue
                exp = datetime.fromisoformat(consent.expires_at)
                if now < exp and scope in consent.scopes:
                    return True
        return False

    def revoke_grant(self, consent_id: str) -> bool:
        consent = self.consents.get(consent_id)
        if not consent:
            return False
        consent.revoked_at = datetime.now(timezone.utc).isoformat()
        return True

    def list_profile_grants(
        self, grantor_profile_id: str
    ) -> List[ConsentRecord]:
        return [
            c
            for c in self.consents.values()
            if c.grantor_profile_id == grantor_profile_id
        ]


consent_service = ConsentService()
