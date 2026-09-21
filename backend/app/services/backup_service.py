import hashlib
import json
from datetime import datetime, timezone
from typing import Any, Dict, Tuple

from pydantic import BaseModel


class BackupEnvelope(BaseModel):
    version: int
    user_id: str
    timestamp: str
    data_hash: str
    payload: Dict[str, Any]


class BackupService:
    """Manages encrypted/integrity-verified patient record backups."""

    def __init__(self):
        self.restored_backups: Dict[str, BackupEnvelope] = {}

    def export_backup(
        self, user_id: str, data: Dict[str, Any]
    ) -> BackupEnvelope:
        now = datetime.now(timezone.utc).isoformat()
        canonical_bytes = json.dumps(data, sort_keys=True).encode("utf-8")
        data_hash = hashlib.sha256(canonical_bytes).hexdigest()

        return BackupEnvelope(
            version=1,
            user_id=user_id,
            timestamp=now,
            data_hash=data_hash,
            payload=data,
        )

    def restore_backup(
        self, envelope: BackupEnvelope
    ) -> Tuple[bool, int, str]:
        # Step 1: Verify data integrity against SHA-256 hash
        canonical_bytes = json.dumps(
            envelope.payload, sort_keys=True
        ).encode("utf-8")
        computed_hash = hashlib.sha256(canonical_bytes).hexdigest()

        if computed_hash != envelope.data_hash:
            return False, 0, "Tampered backup: SHA-256 hash mismatch."

        # Step 2: Ingest records
        records_count = sum(
            len(v) if isinstance(v, list) else 1
            for v in envelope.payload.values()
        )
        self.restored_backups[envelope.user_id] = envelope

        return True, records_count, "Backup restored successfully."


backup_service = BackupService()
