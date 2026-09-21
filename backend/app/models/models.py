import uuid
from datetime import date, datetime, timezone
from typing import Any, Dict, List, Optional

from sqlalchemy import (
    JSON,
    Boolean,
    Date,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base, TimestampMixin


class User(Base, TimestampMixin):
    __tablename__ = "users"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    email: Mapped[str] = mapped_column(
        String(255), unique=True, index=True, nullable=False
    )
    pw_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    totp_secret_enc: Mapped[Optional[str]] = mapped_column(
        String(255), nullable=True
    )
    totp_enabled: Mapped[bool] = mapped_column(Boolean, default=False)
    role: Mapped[str] = mapped_column(String(50), default="patient")
    status: Mapped[str] = mapped_column(String(50), default="active")

    profiles: Mapped[List["Profile"]] = relationship(
        "Profile", back_populates="owner", cascade="all, delete-orphan"
    )


class Profile(Base, TimestampMixin):
    __tablename__ = "profiles"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
    )
    relationship: Mapped[str] = mapped_column(String(50), default="self")
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    dob: Mapped[Optional[date]] = mapped_column(Date, nullable=True)
    sex: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    blood_group: Mapped[Optional[str]] = mapped_column(
        String(10), nullable=True
    )

    owner: Mapped["User"] = relationship("User", back_populates="profiles")


class Doctor(Base, TimestampMixin):
    __tablename__ = "doctors"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    user_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="SET NULL"),
        nullable=True,
    )
    display_name: Mapped[str] = mapped_column(String(255), nullable=False)
    registration_no: Mapped[str] = mapped_column(String(100), nullable=False)
    council: Mapped[str] = mapped_column(String(100), nullable=False)
    verification_status: Mapped[str] = mapped_column(
        String(50), default="unverified"
    )
    clinic_name: Mapped[Optional[str]] = mapped_column(
        String(255), nullable=True
    )
    phone: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    email: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)


class Document(Base, TimestampMixin):
    __tablename__ = "documents"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    profile_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    encounter_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        UUID(as_uuid=True), nullable=True
    )
    doctor_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        UUID(as_uuid=True), nullable=True
    )
    type: Mapped[str] = mapped_column(String(50), nullable=False)
    sha256: Mapped[str] = mapped_column(String(64), nullable=False)
    storage_ref: Mapped[Optional[str]] = mapped_column(
        String(512), nullable=True
    )
    ocr_text: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    version: Mapped[int] = mapped_column(Integer, default=1)


class Observation(Base, TimestampMixin):
    __tablename__ = "observations"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    profile_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    document_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    loinc_code: Mapped[Optional[str]] = mapped_column(
        String(20), nullable=True, index=True
    )
    name_as_printed: Mapped[str] = mapped_column(String(255), nullable=False)
    value_num: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    unit_ucum: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    ref_low: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    ref_high: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    observed_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True), nullable=True
    )


class AdviceEntryModel(Base, TimestampMixin):
    __tablename__ = "advice_entries"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    profile_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    doctor_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False
    )
    body: Mapped[str] = mapped_column(Text, nullable=False)
    origin: Mapped[str] = mapped_column(String(50), default="self_reported")
    signature: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    prev_hash: Mapped[str] = mapped_column(String(64), nullable=False)
    hash: Mapped[str] = mapped_column(String(64), nullable=False)
    version: Mapped[int] = mapped_column(Integer, default=1)


class ConsentModel(Base, TimestampMixin):
    __tablename__ = "consents"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    grantor_profile_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    grantee_doctor_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    scopes: Mapped[List[str]] = mapped_column(JSON, default=list)
    starts_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
    )
    expires_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False
    )
    revoked_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True), nullable=True
    )


class SyncOperationModel(Base, TimestampMixin):
    __tablename__ = "sync_operations"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    operation_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), unique=True, index=True, nullable=False
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    entity_type: Mapped[str] = mapped_column(String(50), nullable=False)
    entity_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False
    )
    operation: Mapped[str] = mapped_column(String(20), nullable=False)
    payload: Mapped[Dict[str, Any]] = mapped_column(JSON, nullable=False)
    server_seq: Mapped[int] = mapped_column(
        Integer, autoincrement=True, index=True
    )
    client_timestamp: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False
    )
