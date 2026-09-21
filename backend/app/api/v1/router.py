from typing import List, Optional

from fastapi import APIRouter
from pydantic import BaseModel, EmailStr

api_router = APIRouter()


# Schema stubs matching OpenAPI v0
class RegisterRequest(BaseModel):
    email: EmailStr
    password: str
    role: str


class AuthResponse(BaseModel):
    user_id: str
    access_token: str
    refresh_token: str
    requires_totp: bool = False


class DecodedTermResponse(BaseModel):
    term: str
    one_liner: str
    plain_explanation: str
    what_to_ask_doctor: str
    source_id: str


@api_router.post("/auth/register", response_model=AuthResponse, status_code=201)
async def register(req: RegisterRequest):
    return AuthResponse(
        user_id="00000000-0000-0000-0000-000000000001",
        access_token="mock_access_token",
        refresh_token="mock_refresh_token",
        requires_totp=False,
    )


@api_router.get("/doctors", response_model=List[dict])
async def list_doctors(specialty: Optional[str] = None, q: Optional[str] = None):
    return []


@api_router.post("/ai/decode-term", response_model=DecodedTermResponse)
async def decode_term(term: str, language: str = "en"):
    return DecodedTermResponse(
        term=term,
        one_liner="A common laboratory biomarker indicating long-term blood glucose regulation.",
        plain_explanation=(
            "This test measures average blood sugar levels over the past 2 to 3 months."
        ),
        what_to_ask_doctor="How does my current result align with our target management plan?",
        source_id="src_medlineplus_hba1c",
    )
