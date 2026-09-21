import hashlib
import json
import uuid
from datetime import datetime, timezone
from typing import Any, Dict, List, Optional

from pydantic import BaseModel

from app.services.consent_service import consent_service


class MessageRecord(BaseModel):
    id: str
    conversation_id: str
    sender_id: str
    sender_role: str  # "patient" or "doctor"
    body: str
    attachments: List[Dict[str, Any]] = []
    promoted_to_advice_id: Optional[str] = None
    created_at: str


class ConversationRecord(BaseModel):
    id: str
    profile_id: str
    participant_doctor_id: str
    last_activity: str


class AdviceEntryRecord(BaseModel):
    id: str
    profile_id: str
    doctor_id: str
    body: str
    origin: str  # "doctor_signed" or "self_reported"
    signature: Optional[str] = None
    prev_hash: str
    hash: str
    version: int = 1


class ChatService:
    """Manages immutable, consent-scoped doctor-patient messaging and Advice promotion (ADR 004)."""

    def __init__(self):
        self.conversations: Dict[str, ConversationRecord] = {}
        self.messages: Dict[str, List[MessageRecord]] = {}  # conversation_id -> messages
        self.advice_ledgers: Dict[str, List[AdviceEntryRecord]] = {}  # profile_id -> entries

    def create_conversation(
        self, profile_id: str, participant_doctor_id: str
    ) -> ConversationRecord:
        # Check active consent (NN-6)
        has_consent = consent_service.has_active_consent(
            grantor_profile_id=profile_id,
            grantee_doctor_id=participant_doctor_id,
            scope="SCOPE_ADVICE_HISTORY",
        ) or consent_service.has_active_consent(
            grantor_profile_id=profile_id,
            grantee_doctor_id=participant_doctor_id,
            scope="SCOPE_LAB_REPORTS",
        )
        if not has_consent:
            raise PermissionError(
                "Cannot start conversation: active consent grant not found or expired."
            )

        cid = str(uuid.uuid4())
        now = datetime.now(timezone.utc).isoformat()
        conv = ConversationRecord(
            id=cid,
            profile_id=profile_id,
            participant_doctor_id=participant_doctor_id,
            last_activity=now,
        )
        self.conversations[cid] = conv
        self.messages[cid] = []
        return conv

    def list_conversations(
        self, profile_id: Optional[str] = None
    ) -> List[ConversationRecord]:
        if profile_id:
            return [
                c
                for c in self.conversations.values()
                if c.profile_id == profile_id
            ]
        return list(self.conversations.values())

    def get_conversation(self, conversation_id: str) -> Optional[ConversationRecord]:
        return self.conversations.get(conversation_id)

    def send_message(
        self,
        conversation_id: str,
        sender_id: str,
        sender_role: str,
        body: str,
        attachments: Optional[List[Dict[str, Any]]] = None,
    ) -> MessageRecord:
        conv = self.conversations.get(conversation_id)
        if not conv:
            raise ValueError("Conversation not found")

        # Re-verify active consent at time of message dispatch (NN-6)
        has_consent = consent_service.has_active_consent(
            grantor_profile_id=conv.profile_id,
            grantee_doctor_id=conv.participant_doctor_id,
            scope="SCOPE_ADVICE_HISTORY",
        ) or consent_service.has_active_consent(
            grantor_profile_id=conv.profile_id,
            grantee_doctor_id=conv.participant_doctor_id,
            scope="SCOPE_LAB_REPORTS",
        )
        if not has_consent:
            raise PermissionError(
                "Message blocked: patient consent grant has been revoked or expired."
            )

        mid = str(uuid.uuid4())
        now = datetime.now(timezone.utc).isoformat()
        msg = MessageRecord(
            id=mid,
            conversation_id=conversation_id,
            sender_id=sender_id,
            sender_role=sender_role,
            body=body,
            attachments=attachments or [],
            promoted_to_advice_id=None,
            created_at=now,
        )

        # Append-only (immutability per ADR 004, NN-7)
        self.messages[conversation_id].append(msg)
        conv.last_activity = now
        return msg

    def get_messages(self, conversation_id: str) -> List[MessageRecord]:
        return self.messages.get(conversation_id, [])

    def promote_to_advice(
        self,
        conversation_id: str,
        message_id: str,
        signature: Optional[str] = None,
    ) -> AdviceEntryRecord:
        conv = self.conversations.get(conversation_id)
        if not conv:
            raise ValueError("Conversation not found")

        msg = next(
            (m for m in self.messages.get(conversation_id, []) if m.id == message_id),
            None,
        )
        if not msg:
            raise ValueError("Message not found in conversation")

        if msg.promoted_to_advice_id:
            raise ValueError("Message is already promoted to Advice Ledger")

        # Retrieve profile ledger to link previous hash
        ledger = self.advice_ledgers.setdefault(conv.profile_id, [])
        prev_hash = ledger[-1].hash if ledger else ("0" * 64)

        adv_id = str(uuid.uuid4())
        body_dict = {
            "id": adv_id,
            "profile_id": conv.profile_id,
            "doctor_id": conv.participant_doctor_id,
            "body": msg.body,
            "origin": "doctor_signed",
            "prev_hash": prev_hash,
            "version": 1,
        }

        # Canonical SHA-256 hash chaining matching verify-chain.sh
        canonical_json = json.dumps(body_dict, sort_keys=True)
        computed_hash = hashlib.sha256(
            (prev_hash + canonical_json).encode("utf-8")
        ).hexdigest()

        advice_entry = AdviceEntryRecord(
            id=adv_id,
            profile_id=conv.profile_id,
            doctor_id=conv.participant_doctor_id,
            body=msg.body,
            origin="doctor_signed",
            signature=signature or "mock_ecdsa_sig_verified",
            prev_hash=prev_hash,
            hash=computed_hash,
            version=1,
        )

        ledger.append(advice_entry)
        msg.promoted_to_advice_id = adv_id
        return advice_entry

    def get_advice_entries(self, profile_id: str) -> List[AdviceEntryRecord]:
        return self.advice_ledgers.get(profile_id, [])


chat_service = ChatService()
