import json
from pathlib import Path

import pytest
from httpx import ASGITransport, AsyncClient

from app.main import app
from app.services.ai_gateway import ai_gateway
from app.services.knowledge_service import knowledge_service
from app.services.pii_scrubber import PIIScrubber
from app.services.red_flag_triage import RedFlagTriage


@pytest.fixture
def golden_refusals():
    repo_root = Path(__file__).resolve().parents[2]
    eval_path = repo_root / "knowledge" / "evals" / "golden_refusals.json"
    with open(eval_path, "r", encoding="utf-8") as f:
        return json.load(f)


@pytest.mark.asyncio
async def test_golden_refusals_suite(golden_refusals):
    """Evaluates 100% compliance with golden refusal tests (NN-1, NN-11)."""
    for item in golden_refusals:
        prompt = item["prompt"]
        expected_behavior = item["expected_behavior"]
        must_not_contain = item.get("must_not_contain", [])
        must_contain = item.get("must_contain", [])

        resp = await ai_gateway.process_query(prompt)
        text_lower = resp.response_text.lower()

        if expected_behavior == "TRIGGER_EMERGENCY_INTERCEPT":
            assert resp.is_emergency is True, f"Failed emergency trigger for {item['id']}"
            assert "112" in resp.emergency_numbers
            assert "108" in resp.emergency_numbers

        for phrase in must_not_contain:
            assert phrase.lower() not in text_lower, (
                f"Violation in {item['id']}: output contained forbidden phrase '{phrase}'"
            )

        for phrase in must_contain:
            assert phrase.lower() in text_lower, (
                f"Violation in {item['id']}: output missing required phrase '{phrase}'"
            )


def test_pii_scrubber():
    """Validates pre-inference scrubbing of Indian identifiers (NN-8, NN-9)."""
    raw_text = (
        "Patient Ramesh (Aadhaar: 4321 8765 0987, ABHA: 14-5678-9012-3456) "
        "can be reached at +91 9876543210 or ramesh.kumar@example.com."
    )

    scrubbed, count = PIIScrubber.scrub(raw_text)

    assert "[AADHAAR_REDACTED]" in scrubbed
    assert "[ABHA_REDACTED]" in scrubbed
    assert "[PHONE_REDACTED]" in scrubbed
    assert "[EMAIL_REDACTED]" in scrubbed
    assert "4321" not in scrubbed
    assert "9876543210" not in scrubbed
    assert "ramesh.kumar@example.com" not in scrubbed
    assert count == 4


def test_red_flag_triage_patterns():
    """Validates emergency symptom classification (NN-11)."""
    chest_pain = "I have sudden crushing chest pain radiating to left jaw."
    result = RedFlagTriage.evaluate(chest_pain)
    assert result.is_emergency is True
    assert "112" in result.emergency_numbers
    assert "108" in result.emergency_numbers

    normal_query = "What does an HbA1c of 5.8% mean?"
    result_norm = RedFlagTriage.evaluate(normal_query)
    assert result_norm.is_emergency is False


def test_knowledge_service_queries():
    """Validates retrieval of registered glossary terms and drug formulations."""
    term = knowledge_service.get_term("HbA1c")
    assert term is not None
    assert term.source_id == "src_medlineplus"
    assert "blood sugar" in term.one_liner.lower()

    drug = knowledge_service.get_drug_by_brand("Augmentin")
    assert drug is not None
    assert drug.source_id == "src_openfda"
    assert "Amoxicillin" in drug.generic_name

    sources = knowledge_service.get_registered_sources()
    assert len(sources) >= 5
    source_ids = {s.source_id for s in sources}
    assert "src_medlineplus" in source_ids
    assert "src_openfda" in source_ids
    assert "src_loinc" in source_ids


@pytest.mark.asyncio
async def test_api_decode_term_endpoint():
    """Validates /api/v1/ai/decode-term endpoint."""
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        resp = await ac.post("/api/v1/ai/decode-term?term=dyspnoea")
        assert resp.status_code == 200
        data = resp.json()
        assert data["term"] == "dyspnoea"
        assert data["source_id"] == "src_medlineplus"
        assert "breath" in data["plain_explanation"].lower()


@pytest.mark.asyncio
async def test_api_explain_report_endpoint():
    """Validates /api/v1/ai/explain-report endpoint without diagnostic claims."""
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        payload = {
            "document_id": "00000000-0000-0000-0000-000000000001",
            "observations": [
                {
                    "name": "HbA1c",
                    "value": 7.8,
                    "unit": "%",
                    "ref_low": 4.0,
                    "ref_high": 5.6,
                }
            ],
        }
        resp = await ac.post("/api/v1/ai/explain-report", json=payload)
        assert resp.status_code == 200
        data = resp.json()
        assert len(data["blocks"]) == 1
        assert "7.8 % is currently above the reference range" in data["blocks"][0]["text"]
        assert "src_medlineplus" in data["blocks"][0]["cites"]
        assert data["disclaimer_included"] is True


@pytest.mark.asyncio
async def test_api_knowledge_endpoints():
    """Validates /api/v1/knowledge/drugs and /sources endpoints."""
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        drugs_resp = await ac.get("/api/v1/knowledge/drugs?q=glycomet")
        assert drugs_resp.status_code == 200
        drugs = drugs_resp.json()
        assert len(drugs) >= 1
        assert drugs[0]["generic_name"] == "Metformin Hydrochloride"

        sources_resp = await ac.get("/api/v1/knowledge/sources")
        assert sources_resp.status_code == 200
        sources = sources_resp.json()
        assert any(s["source_id"] == "src_medlineplus" for s in sources)
