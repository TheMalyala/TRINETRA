import re
from typing import List, Optional

from pydantic import BaseModel


class EmergencyTriageResult(BaseModel):
    is_emergency: bool
    emergency_numbers: List[str]
    title: Optional[str] = None
    guidance: Optional[str] = None
    matched_flags: List[str] = []


class RedFlagTriage:
    """Triage engine implementing NN-11.

    Red-flag emergency symptoms trigger 112/108 routing before any AI prose is generated.
    """

    # High-recall emergency patterns
    EMERGENCY_PATTERNS = [
        # Cardiac & Ischemic
        (
            r"\b(crushing|severe|tight|pressure|squeezing)\b.*\b(chest|heart)\b",
            "Acute Coronary Syndrome / Chest Pain",
        ),
        (
            r"\b(chest\s*pain|chest\s*tightness|chest\s*pressure)\b",
            "Severe Chest Pain",
        ),
        (
            r"\b(radiat(ing|es)?\s*(to)?\s*(my|the)?\s*(left\s*arm|left\s*jaw|jaw|neck|back))\b",
            "Referred Cardiac Pain",
        ),
        (r"\bheart\s*attack\b", "Suspected Myocardial Infarction"),
        # Respiratory failure
        (
            r"\b(cannot\s*breathe|can\'?t\s*breathe|unable\s*to\s*breathe|gasping\s*for\s*(air|breath)|choking|severe\s*breathlessness|suffocating)\b",
            "Acute Respiratory Compromise",
        ),
        (r"\b(turning\s*blue|cyanosis)\b", "Severe Hypoxia"),
        # Neurological / Stroke
        (
            r"\b(face\s*droop(ing)?|facial\s*droop|slurred\s*speech|sudden\s*weakness|sudden\s*numbness|paralysis)\b",
            "Suspected Stroke / Neurological Emergency",
        ),
        (
            r"\b(loss\s*of\s*consciousness|passed\s*out|unresponsive|fainted\s*and\s*not\s*waking)\b",
            "Loss of Consciousness",
        ),
        # Severe Hemorrhage
        (
            r"\b(coughing\s*(up\s*)?blood|vomiting\s*blood|uncontrolled\s*bleeding|severe\s*hemorrhage)\b",
            "Severe Hemorrhage",
        ),
        # Poisoning & Anaphylaxis
        (
            r"\b(drank\s*poison|swallowed\s*pesticide|swallowed\s*poison|overdose|anaphylaxis|throat\s*closing\s*up)\b",
            "Acute Poisoning / Anaphylaxis",
        ),
    ]

    @classmethod
    def evaluate(cls, text: str) -> EmergencyTriageResult:
        if not text:
            return EmergencyTriageResult(is_emergency=False, emergency_numbers=[])

        lowered = text.lower()
        matched_flags = []

        for pattern, label in cls.EMERGENCY_PATTERNS:
            if re.search(pattern, lowered):
                matched_flags.append(label)

        if matched_flags:
            return EmergencyTriageResult(
                is_emergency=True,
                emergency_numbers=["112", "108"],
                title="Immediate Emergency Action Required",
                guidance=(
                    "EMERGENCY: The symptoms you described indicate a potential medical emergency. "
                    "Please immediately call 112 (National Emergency Helpline) or 108 (Ambulance) "
                    "or go to the nearest hospital emergency department right now. "
                    "Do not take unprescribed painkillers or wait for online responses."
                ),
                matched_flags=matched_flags,
            )

        return EmergencyTriageResult(is_emergency=False, emergency_numbers=[])
