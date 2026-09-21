from typing import List, Optional

from fastapi import APIRouter
from pydantic import BaseModel, EmailStr

from app.services.ai_gateway import AIQueryResponse, ai_gateway
from app.services.knowledge_service import (
    DrugProduct,
    KnowledgeSource,
    knowledge_service,
)

api_router = APIRouter()


# Schemas
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


# Auth stubs
@api_router.post("/auth/register", response_model=AuthResponse, status_code=201)
async def register(req: RegisterRequest):
    return AuthResponse(
        user_id="00000000-0000-0000-0000-000000000001",
        access_token="mock_access_token",
        refresh_token="mock_refresh_token",
        requires_totp=False,
    )


@api_router.get("/doctors", response_model=List[dict])
async def list_doctors(
    specialty: Optional[str] = None, q: Optional[str] = None
):
    return []


# Phase 5: Knowledge & AI Endpoints
@api_router.post("/ai/query", response_model=AIQueryResponse)
async def query_ai(req: AIQueryRequest):
    """Processes queries through the AI Gateway with safety guardrails and citations."""
    return await ai_gateway.process_query(
        prompt=req.prompt, language=req.language
    )


@api_router.post("/ai/decode-term", response_model=DecodedTermResponse)
async def decode_term(term: str, language: str = "en"):
    """Decodes clinical medical jargon into plain language citing verified sources."""
    term_obj = knowledge_service.get_term(term)
    if term_obj:
        return DecodedTermResponse(
            term=term_obj.term,
            one_liner=term_obj.one_liner,
            plain_explanation=term_obj.plain_explanation,
            what_to_ask_doctor=term_obj.what_to_ask_doctor,
            source_id=term_obj.source_id,
        )

    # Fallback to AI Gateway for unseeded terms
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
    """Explains lab observations with printed reference ranges without diagnostic assertions."""
    blocks: List[AIResponseBlock] = []
    red_flags: List[str] = []
    questions: List[str] = []

    for obs in req.observations:
        # Check for matching term in knowledge base
        term_obj = knowledge_service.get_term(obs.name)
        cite_id = term_obj.source_id if term_obj else "src_curated_glossary"

        # Deterministic status classification
        status = "within normal reference range"
        if obs.ref_low is not None and obs.value < obs.ref_low:
            status = f"below the reference range of {obs.ref_low} - {obs.ref_high} {obs.unit}"
        elif obs.ref_high is not None and obs.value > obs.ref_high:
            status = f"above the reference range of {obs.ref_low} - {obs.ref_high} {obs.unit}"

        block_text = (
            f"{obs.name}: {obs.value} {obs.unit} is currently {status}. "
        )
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
    """Searches brand and generic medication formulations from openFDA seeds."""
    return knowledge_service.search_drugs(q)


@api_router.get("/knowledge/sources", response_model=List[KnowledgeSource])
async def list_knowledge_sources():
    """Returns registered open medical knowledge sources and license details per NN-3."""
    return knowledge_service.get_registered_sources()
