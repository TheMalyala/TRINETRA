from datetime import datetime, timezone
from typing import Any, Dict, List, Optional

from pydantic import BaseModel


class SyncOp(BaseModel):
    operation_id: str
    entity_type: str
    entity_id: str
    operation: str
    payload: Dict[str, Any]
    client_timestamp: str
    server_seq: Optional[int] = None


class SyncService:
    """Offline-first outbox sync engine with idempotency keys and monotonic cursor."""

    def __init__(self):
        self.processed_op_ids: set[str] = set()
        self.operation_log: List[SyncOp] = []
        self.current_seq: int = 0

    def push(self, operations: List[Dict[str, Any]]) -> Dict[str, Any]:
        new_processed = 0

        for op_dict in operations:
            op_id = op_dict.get("operation_id")
            if not op_id:
                continue

            # Idempotency check: Ignore duplicate submissions
            if op_id in self.processed_op_ids:
                continue

            self.current_seq += 1
            op = SyncOp(
                operation_id=op_id,
                entity_type=op_dict.get("entity_type", "unknown"),
                entity_id=op_dict.get("entity_id", ""),
                operation=op_dict.get("operation", "INSERT"),
                payload=op_dict.get("payload", {}),
                client_timestamp=op_dict.get(
                    "client_timestamp", datetime.now(timezone.utc).isoformat()
                ),
                server_seq=self.current_seq,
            )

            self.operation_log.append(op)
            self.processed_op_ids.add(op_id)
            new_processed += 1

        return {
            "processed_count": new_processed,
            "server_seq": str(self.current_seq),
        }

    def pull(self, cursor: Optional[str] = None) -> Dict[str, Any]:
        from_seq = 0
        if cursor and cursor.isdigit():
            from_seq = int(cursor)

        changes = [
            op.model_dump()
            for op in self.operation_log
            if op.server_seq is not None and op.server_seq > from_seq
        ]

        next_cursor = str(self.current_seq)

        return {
            "changes": changes,
            "next_cursor": next_cursor,
        }


sync_service = SyncService()
