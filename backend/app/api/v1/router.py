from typing import Any, Dict, List, Optional

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, EmailStr

from app.services.ai_gateway import AIQueryResponse, ai_gateway
from app.services.auth_service import auth_service
from app.services.backup_service import BackupEnvelope, backup_service
from app.services.consent_service import ConsentRecord, consent_service
from app.services.knowledge_service import (
    DrugProduct,
    KnowledgeSource,
    knowledge_service,
)
from app.services.sync_service import sync_service

api_router = APIRouter()


# Schemas
class RegisterRequest(BaseModel):
    email: EmailStr
    password: str
    role: str = "patient"


class LoginRequest(BaseModel):
    email: EmailStr
    password: str
    totp_code: Optional[str] = None


class AuthResponse(BaseModel):
    user_id: str
    access_token: str
    refresh_token: str
    requires_totp: bool = False


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str


class RefreshRequest(BaseModel):
    refresh_token: str


class TotpSetupResponse(BaseModel):
    secret: str
    provisioning_uri: str


class TotpVerifyRequest(BaseModel):
    user_id: str
    code: str


class ProfileCreateRequest(BaseModel):
    user_id: str
    relationship: str
    name: str


class ProfileResponse(BaseModel):
    id: str
    owner_user_id: str
    relationship: str
    name: str


class ConsentCreateRequest(BaseModel):
    grantor_profile_id: str
    grantee_doctor_id: str
    scopes: List[str]
    duration_hours: int = 24


class SyncPushRequest(BaseModel):
    operations: List[Dict[str, Any]]


class SyncPushResponse(BaseModel):
    processed_count: int
    server_seq: str


class SyncPullResponse(BaseModel):
    changes: List[Dict[str, Any]]
    next_cursor: str


class DecodedTermResponse(BaseModel):
    term: str
    one_liner: str
    plain_explanation: str
    what_to_ask_doctor: str
    source_id: str


class AIQueryRequest(BaseModel):
    prompt: str
    profile_id: Optional[str] = None
    language: str = "en"


class ObservationItem(BaseModel):
    name: str
    value: float
    unit: str
    ref_low: Optional[float] = None
    ref_high: Optional[float] = None


class ExplainReportRequest(BaseModel):
    document_id: str
    observations: List[ObservationItem]


class AIResponseBlock(BaseModel):
    text: str
    cites: List[str]


class ExplainReportResponse(BaseModel):
    blocks: List[AIResponseBlock]
    red_flags: List[str] = []
    questions_for_doctor: List[str] = []
    confidence: str = "high"
    disclaimer_included: bool = True


# Auth Endpoints
@api_router.post("/auth/register", response_model=AuthResponse, status_code=201)
async def register(req: RegisterRequest):
    try:
        user, access, refresh = auth_service.register(
            email=req.email, password=req.password, role=req.role
        )
        return AuthResponse(
            user_id=user.id,
            access_token=access,
            refresh_token=refresh,
            requires_totp=False,
        )
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@api_router.post("/auth/login", response_model=AuthResponse)
async def login(req: LoginRequest):
    try:
        user, access, refresh, req_totp = auth_service.login(
            email=req.email, password=req.password, totp_code=req.totp_code
        )
        return AuthResponse(
            user_id=user.id,
            access_token=access,
            refresh_token=refresh,
            requires_totp=req_totp,
        )
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=str(e))


@api_router.post("/auth/refresh", response_model=TokenResponse)
async def refresh_token(req: RefreshRequest):
    try:
        access, refresh = auth_service.refresh(req.refresh_token)
        return TokenResponse(access_token=access, refresh_token=refresh)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=str(e))


@api_router.post("/auth/totp/setup", response_model=TotpSetupResponse)
async def setup_totp(user_id: str):
    try:
        secret, uri = auth_service.setup_totp(user_id)
        return TotpSetupResponse(secret=secret, provisioning_uri=uri)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@api_router.post("/auth/totp/verify")
async def verify_totp_code(req: TotpVerifyRequest):
    success = auth_service.verify_and_enable_totp(req.user_id, req.code)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid verification code or user not found.",
        )
    return {"verified": True}


# Profile Endpoints
@api_router.get("/profiles", response_model=List[ProfileResponse])
async def list_profiles(user_id: str):
    records = auth_service.get_profiles(user_id)
    return [
        ProfileResponse(
            id=p.id,
            owner_user_id=p.owner_user_id,
            relationship=p.relationship,
            name=p.name,
        )
        for p in records
    ]


@api_router.post("/profiles", response_model=ProfileResponse, status_code=201)
async def create_profile(req: ProfileCreateRequest):
    p = auth_service.create_profile(
        user_id=req.user_id, relationship=req.relationship, name=req.name
    )
    return ProfileResponse(
        id=p.id,
        owner_user_id=p.owner_user_id,
        relationship=p.relationship,
        name=p.name,
    )


# Doctor Directory Endpoint
@api_router.get("/doctors", response_model=List[dict])
async def list_doctors(
    specialty: Optional[str] = None, q: Optional[str] = None
):
    return [
        {
            "id": "00000000-0000-0000-0000-000000000001",
            "display_name": "Dr. Rajesh Sharma",
            "registration_no": "MCI-29481",
            "council": "Delhi Medical Council",
            "verification_status": "verified",
            "clinic_name": "Apollo Diabetology Center",
            "specialties": ["Diabetology", "Endocrinology"],
        },
        {
            "id": "00000000-0000-0000-0000-000000000002",
            "display_name": "Dr. Sunita Verma",
            "registration_no": "PMC-84920",
            "council": "Punjab Medical Council",
            "verification_status": "unverified",
            "clinic_name": "Verma Healthcare",
            "specialties": ["General Medicine"],
        },
    ]


# Consent Endpoints
@api_router.post("/consents", response_model=ConsentRecord, status_code=201)
async def create_consent(req: ConsentCreateRequest):
    return consent_service.create_grant(
        grantor_profile_id=req.grantor_profile_id,
        grantee_doctor_id=req.grantee_doctor_id,
        scopes=req.scopes,
        duration_hours=req.duration_hours,
    )


@api_router.get("/consents", response_model=List[ConsentRecord])
async def list_consents(profile_id: str):
    return consent_service.list_profile_grants(profile_id)


@api_router.post("/consents/{consent_id}/revoke")
async def revoke_consent(consent_id: str):
    success = consent_service.revoke_grant(consent_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Consent not found"
        )
    return {"revoked": True}


# Sync Outbox Endpoints
@api_router.post("/sync/push", response_model=SyncPushResponse)
async def sync_push(req: SyncPushRequest):
    result = sync_service.push(req.operations)
    return SyncPushResponse(
        processed_count=result["processed_count"],
        server_seq=result["server_seq"],
    )


@api_router.get("/sync/pull", response_model=SyncPullResponse)
async def sync_pull(cursor: Optional[str] = None):
    result = sync_service.pull(cursor)
    return SyncPullResponse(
        changes=result["changes"],
        next_cursor=result["next_cursor"],
    )


# Encrypted Backup Endpoints
@api_router.post("/backup/export", response_model=BackupEnvelope)
async def backup_export(user_id: str):
    # Collect patient data
    profiles = auth_service.get_profiles(user_id)
    data = {
        "profiles": [p.model_dump() for p in profiles],
        "consents": [
            c.model_dump()
            for p in profiles
            for c in consent_service.list_profile_grants(p.id)
        ],
    }
    return backup_service.export_backup(user_id=user_id, data=data)


@api_router.post("/backup/restore")
async def backup_restore(envelope: BackupEnvelope):
    success, count, msg = backup_service.restore_backup(envelope)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, detail=msg
        )
    return {"restored": True, "records_count": count, "message": msg}


# AI & Knowledge Endpoints (Phase 5)
@api_router.post("/ai/query", response_model=AIQueryResponse)
async def query_ai(req: AIQueryRequest):
    return await ai_gateway.process_query(
        prompt=req.prompt, language=req.language
    )


@api_router.post("/ai/decode-term", response_model=DecodedTermResponse)
async def decode_term(term: str, language: str = "en"):
    term_obj = knowledge_service.get_term(term)
    if term_obj:
        return DecodedTermResponse(
            term=term_obj.term,
            one_liner=term_obj.one_liner,
            plain_explanation=term_obj.plain_explanation,
            what_to_ask_doctor=term_obj.what_to_ask_doctor,
            source_id=term_obj.source_id,
        )

    query_resp = await ai_gateway.process_query(f"What is {term}?")
    first_cite = (
        query_resp.citations[0].source_id
        if query_resp.citations
        else "src_curated_glossary"
    )
    first_question = (
        query_resp.questions_for_doctor[0]
        if query_resp.questions_for_doctor
        else "How does this result relate to my overall health?"
    )

    return DecodedTermResponse(
        term=term,
        one_liner="Clinical observation or test parameter.",
        plain_explanation=query_resp.response_text,
        what_to_ask_doctor=first_question,
        source_id=first_cite,
    )


@api_router.post("/ai/explain-report", response_model=ExplainReportResponse)
async def explain_report(req: ExplainReportRequest):
    blocks: List[AIResponseBlock] = []
    red_flags: List[str] = []
    questions: List[str] = []

    for obs in req.observations:
        term_obj = knowledge_service.get_term(obs.name)
        cite_id = term_obj.source_id if term_obj else "src_curated_glossary"

        status_str = "within normal reference range"
        if obs.ref_low is not None and obs.value < obs.ref_low:
            status_str = (
                f"below the reference range of {obs.ref_low} - {obs.ref_high} {obs.unit}"
            )
        elif obs.ref_high is not None and obs.value > obs.ref_high:
            status_str = (
                f"above the reference range of {obs.ref_low} - {obs.ref_high} {obs.unit}"
            )

        block_text = f"{obs.name}: {obs.value} {obs.unit} is currently {status_str}. "
        if term_obj:
            block_text += f"Educational context: {term_obj.plain_explanation} "
            questions.append(term_obj.what_to_ask_doctor)

        blocks.append(AIResponseBlock(text=block_text.strip(), cites=[cite_id]))

    if not questions:
        questions.append(
            "What do these specific test values indicate for my personalized health plan?"
        )

    return ExplainReportResponse(
        blocks=blocks,
        red_flags=red_flags,
        questions_for_doctor=questions[:3],
        confidence="high",
        disclaimer_included=True,
    )


@api_router.get("/knowledge/drugs", response_model=List[DrugProduct])
async def search_drugs(q: str):
    return knowledge_service.search_drugs(q)


@api_router.get("/knowledge/sources", response_model=List[KnowledgeSource])
async def list_knowledge_sources():
    return knowledge_service.get_registered_sources()
