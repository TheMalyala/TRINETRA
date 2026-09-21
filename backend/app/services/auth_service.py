import uuid
from typing import Dict, Optional, Tuple

from pydantic import BaseModel, EmailStr

from app.core.security import (
    create_access_token,
    create_refresh_token,
    generate_totp_secret,
    get_password_hash,
    verify_password,
    verify_totp,
)


class UserRecord(BaseModel):
    id: str
    email: str
    pw_hash: str
    role: str
    totp_secret: Optional[str] = None
    totp_enabled: bool = False
    status: str = "active"


class ProfileRecord(BaseModel):
    id: str
    owner_user_id: str
    relationship: str
    name: str
    dob: Optional[str] = None
    sex: Optional[str] = None
    blood_group: Optional[str] = None


class AuthService:
    """Authentication and session management implementing Argon2id, JWT, and TOTP."""

    def __init__(self):
        # In-memory storage for test/dev/offline environments
        self.users: Dict[str, UserRecord] = {}  # email -> UserRecord
        self.users_by_id: Dict[str, UserRecord] = {}  # id -> UserRecord
        self.profiles: Dict[str, ProfileRecord] = {}  # id -> ProfileRecord
        self.refresh_tokens: Dict[str, str] = {}  # token -> user_id

    def register(
        self, email: EmailStr, password: str, role: str
    ) -> Tuple[UserRecord, str, str]:
        email_clean = email.strip().lower()
        if email_clean in self.users:
            raise ValueError("Email already registered")

        user_id = str(uuid.uuid4())
        hashed = get_password_hash(password)
        user = UserRecord(
            id=user_id,
            email=email_clean,
            pw_hash=hashed,
            role=role,
            totp_enabled=False,
        )
        self.users[email_clean] = user
        self.users_by_id[user_id] = user

        # Auto-create primary "self" profile
        prof_id = str(uuid.uuid4())
        self.profiles[prof_id] = ProfileRecord(
            id=prof_id,
            owner_user_id=user_id,
            relationship="self",
            name=email_clean.split("@")[0].capitalize(),
        )

        access_token = create_access_token(subject=user_id)
        refresh_token = create_refresh_token(subject=user_id)
        self.refresh_tokens[refresh_token] = user_id

        return user, access_token, refresh_token

    def login(
        self, email: EmailStr, password: str, totp_code: Optional[str] = None
    ) -> Tuple[UserRecord, str, str, bool]:
        email_clean = email.strip().lower()
        user = self.users.get(email_clean)
        if not user or not verify_password(password, user.pw_hash):
            raise ValueError("Invalid email or password")

        # Check TOTP if enabled
        if user.totp_enabled:
            if not totp_code or not verify_totp(user.totp_secret or "", totp_code):
                return user, "", "", True  # requires TOTP

        access_token = create_access_token(subject=user.id)
        refresh_token = create_refresh_token(subject=user.id)
        self.refresh_tokens[refresh_token] = user.id

        return user, access_token, refresh_token, False

    def refresh(self, old_refresh_token: str) -> Tuple[str, str]:
        user_id = self.refresh_tokens.pop(old_refresh_token, None)
        if not user_id or user_id not in self.users_by_id:
            raise ValueError("Invalid or expired refresh token")

        new_access_token = create_access_token(subject=user_id)
        new_refresh_token = create_refresh_token(subject=user_id)
        self.refresh_tokens[new_refresh_token] = user_id
        return new_access_token, new_refresh_token

    def setup_totp(self, user_id: str) -> Tuple[str, str]:
        user = self.users_by_id.get(user_id)
        if not user:
            raise ValueError("User not found")

        secret = generate_totp_secret()
        user.totp_secret = secret
        uri = f"otpauth://totp/Trinetra:{user.email}?secret={secret}&issuer=Trinetra"
        return secret, uri

    def verify_and_enable_totp(self, user_id: str, code: str) -> bool:
        user = self.users_by_id.get(user_id)
        if not user or not user.totp_secret:
            return False

        if verify_totp(user.totp_secret, code):
            user.totp_enabled = True
            return True
        return False

    def get_profiles(self, user_id: str) -> list[ProfileRecord]:
        return [
            p for p in self.profiles.values() if p.owner_user_id == user_id
        ]

    def create_profile(
        self, user_id: str, relationship: str, name: str
    ) -> ProfileRecord:
        prof_id = str(uuid.uuid4())
        record = ProfileRecord(
            id=prof_id,
            owner_user_id=user_id,
            relationship=relationship,
            name=name,
        )
        self.profiles[prof_id] = record
        return record


auth_service = AuthService()
