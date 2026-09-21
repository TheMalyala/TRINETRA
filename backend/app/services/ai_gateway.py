import abc
from typing import Any, Dict, List, Optional

from pydantic import BaseModel

from app.core.config import settings
from app.services.ai_guardrails import AIGuardrails
from app.services.knowledge_service import knowledge_service
from app.services.pii_scrubber import PIIScrubber
from app.services.red_flag_triage import RedFlagTriage


class AICitation(BaseModel):
    source_id: str
    source_name: str
    url: Optional[str] = None
    reference_text: Optional[str] = None


class AIQueryResponse(BaseModel):
    response_text: str
    is_emergency: bool = False
    emergency_numbers: List[str] = []
    disclaimer_included: bool = True
    intent: str
    citations: List[AICitation] = []
    questions_for_doctor: List[str] = []


class AIProviderInterface(abc.ABC):
    @abc.abstractmethod
    async def generate(
        self, prompt: str, context: Optional[str] = None
    ) -> Dict[str, Any]:
        """Generates AI response returning text and citations."""
        pass


class MockAIProvider(AIProviderInterface):
    """High-fidelity grounded deterministic provider for offline/testing/dev environments."""

    async def generate(
        self, prompt: str, context: Optional[str] = None
    ) -> Dict[str, Any]:
        # If context is available, formulate grounded educational explanation
        if context:
            return {
                "text": (
                    f"Based on grounded medical references: {context} "
                    "This information helps you understand your health records "
                    "and prepare questions for your doctor."
                ),
                "citations": ["src_curated_glossary"],
            }

        return {
            "text": (
                "Trinetra provides educational explanations of health records and biomarkers. "
                "Consult your doctor for personalized clinical advice."
            ),
            "citations": ["src_curated_glossary"],
        }


class OllamaProvider(AIProviderInterface):
    """Adapter for local Ollama instances (Apache-2 models) per ADR 006."""

    def __init__(self, base_url: str, model_name: str):
        self.base_url = base_url.rstrip("/")
        self.model_name = model_name

    async def generate(
        self, prompt: str, context: Optional[str] = None
    ) -> Dict[str, Any]:
        import httpx

        system_prompt = (
            "You are Netra, an educational health-records assistant for the Trinetra platform. "
            "You never diagnose, prescribe, or change drug dosages. "
            "Explain lab observations and medical terms simply and accurately. "
            "Always include questions for the user to ask their doctor."
        )

        user_content = (
            f"Context: {context}\n\nQuery: {prompt}" if context else prompt
        )

        async with httpx.AsyncClient(timeout=30.0) as client:
            resp = await client.post(
                f"{self.base_url}/api/chat",
                json={
                    "model": self.model_name,
                    "messages": [
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": user_content},
                    ],
                    "stream": False,
                },
            )
            resp.raise_for_status()
            data = resp.json()
            return {
                "text": data.get("message", {}).get("content", ""),
                "citations": ["src_medlineplus"],
            }


class AIGateway:
    """Central AI Gateway implementing ADR 006, NN-1, NN-3, NN-8, NN-9, and NN-11."""

    DISCLAIMER = (
        "\n\nDisclaimer: Trinetra provides educational explanations, "
        "not a clinical diagnosis or treatment prescription. "
        "Always consult a qualified medical professional."
    )

    def __init__(self):
        # Configure active provider
        if settings.AI_PROVIDER == "ollama":
            self.provider: AIProviderInterface = OllamaProvider(
                base_url=settings.OLLAMA_BASE_URL,
                model_name=settings.AI_MODEL,
            )
        else:
            self.provider = MockAIProvider()

    async def process_query(
        self, prompt: str, language: str = "en"
    ) -> AIQueryResponse:
        # STEP 1: Red-Flag Emergency Triage Intercept (NN-11)
        triage_result = RedFlagTriage.evaluate(prompt)
        if triage_result.is_emergency:
            return AIQueryResponse(
                response_text=triage_result.guidance or "",
                is_emergency=True,
                emergency_numbers=triage_result.emergency_numbers,
                disclaimer_included=False,
                intent="TRIGGER_EMERGENCY_INTERCEPT",
                citations=[
                    AICitation(
                        source_id="src_curated_glossary",
                        source_name="National Emergency Protocols (112/108)",
                    )
                ],
                questions_for_doctor=[],
            )

        # STEP 2: Safety Guardrails Pre-Generation Check (NN-1)
        guardrail_result = AIGuardrails.check_user_intent(prompt)
        if guardrail_result.is_refusal:
            citations = [
                AICitation(
                    source_id=sid,
                    source_name=knowledge_service.sources.get(
                        sid, AICitation(source_id=sid, source_name=sid)
                    ).source_name
                    if sid in knowledge_service.sources
                    else sid,
                )
                for sid in guardrail_result.source_ids
            ]
            return AIQueryResponse(
                response_text=(guardrail_result.safe_response or "")
                + self.DISCLAIMER,
                is_emergency=False,
                emergency_numbers=[],
                disclaimer_included=True,
                intent=guardrail_result.intent,
                citations=citations,
                questions_for_doctor=guardrail_result.questions_for_doctor,
            )

        # STEP 3: Pre-Inference PHI Redaction Filter (NN-8, NN-9)
        scrubbed_prompt, _ = PIIScrubber.scrub(prompt)

        # STEP 4: Grounded Knowledge Context Retrieval (NN-3)
        retrieved_context: Optional[str] = None
        matched_citations: List[AICitation] = []
        questions_for_doctor: List[str] = []
        intent = "GENERAL_EDUCATIONAL"

        # Check for medical term matches
        term_match = knowledge_service.get_term(scrubbed_prompt)
        if term_match:
            intent = "DECODE_TERM"
            retrieved_context = (
                f"{term_match.term}: {term_match.one_liner} "
                f"Detailed Explanation: {term_match.plain_explanation}"
            )
            src_info = knowledge_service.sources.get(term_match.source_id)
            matched_citations.append(
                AICitation(
                    source_id=term_match.source_id,
                    source_name=src_info.source_name
                    if src_info
                    else term_match.source_id,
                    reference_text=term_match.one_liner,
                )
            )
            questions_for_doctor.append(term_match.what_to_ask_doctor)

        # Check for drug matches
        if not term_match:
            drug_matches = knowledge_service.search_drugs(scrubbed_prompt)
            if drug_matches:
                intent = "EXPLAIN_DRUG"
                first_drug = drug_matches[0]
                retrieved_context = (
                    f"Brand Name: {first_drug.brand_name}. "
                    f"Generic Name (Active Ingredient): {first_drug.generic_name}. "
                    f"Therapeutic Class: {first_drug.therapeutic_class}. "
                    f"Forms: {', '.join(first_drug.common_forms)}."
                )
                src_info = knowledge_service.sources.get(first_drug.source_id)
                matched_citations.append(
                    AICitation(
                        source_id=first_drug.source_id,
                        source_name=src_info.source_name
                        if src_info
                        else first_drug.source_id,
                        reference_text=f"{first_drug.brand_name} ({first_drug.generic_name})",
                    )
                )
                questions_for_doctor.extend(
                    [
                        (
                            f"Why was {first_drug.brand_name} ({first_drug.generic_name}) "
                            "chosen for my treatment plan?"
                        ),
                        "What potential side effects or dietary precautions should I be aware of?",
                        "How long should I take this medication before we reassess?",
                    ]
                )

        # Fallback citation if none matched
        if not matched_citations:
            matched_citations.append(
                AICitation(
                    source_id="src_curated_glossary",
                    source_name="Trinetra Curated Medical Glossary",
                )
            )

        # STEP 5: Provider Execution
        try:
            raw_result = await self.provider.generate(
                prompt=scrubbed_prompt, context=retrieved_context
            )
            output_text = raw_result.get("text", "")
        except Exception:
            # Safe offline / failure fallback
            if retrieved_context:
                output_text = (
                    f"Knowledge lookup: {retrieved_context}\n\n"
                    "Please discuss these details with your healthcare provider."
                )
            else:
                output_text = (
                    "Trinetra can help explain lab tests and medical terminology. "
                    "Please ask about a specific lab biomarker or medication."
                )

        # STEP 6: Post-Generation Output Guardrail Validation
        valid, violation_reason = AIGuardrails.validate_output(
            text=output_text,
            citations=[c.source_id for c in matched_citations],
        )
        if not valid:
            # Replace violating output with safe educational fallback
            output_text = (
                "Educational summary: Medical indicators require clinical review. "
                "Please review these findings with your doctor."
            )

        return AIQueryResponse(
            response_text=output_text + self.DISCLAIMER,
            is_emergency=False,
            emergency_numbers=[],
            disclaimer_included=True,
            intent=intent,
            citations=matched_citations,
            questions_for_doctor=questions_for_doctor,
        )


ai_gateway = AIGateway()
