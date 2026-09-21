import re
from typing import Tuple


class PIIScrubber:
    """Pre-inference redaction filter for Indian health data (NN-8, NN-9).

    Ensures zero Protected Health Information or direct patient identifiers
    are forwarded to remote LLMs or logged in plain text.
    """

    # Aadhaar: 12 digits (often 4-4-4)
    AADHAAR_REGEX = re.compile(r"\b[2-9]\d{3}\s?\d{4}\s?\d{4}\b")

    # ABHA ID: 14 digits with hyphens (e.g., 14-1234-5678-9012)
    ABHA_REGEX = re.compile(r"\b\d{2}-\d{4}-\d{4}-\d{4}\b")

    # Indian Mobile Phone: 10 digits starting with 6-9, optional +91 / 0
    PHONE_REGEX = re.compile(r"(?:\+91[\-\s]?|91[\-\s]?|0)?[6-9]\d{4}[\-\s]?\d{5}\b")

    # Email Addresses
    EMAIL_REGEX = re.compile(
        r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b"
    )

    # Indian PIN Code (6 digits)
    PIN_REGEX = re.compile(r"\b[1-9][0-9]{2}\s?[0-9]{3}\b")

    @classmethod
    def scrub(cls, text: str) -> Tuple[str, int]:
        """Redacts sensitive identifiers from text.

        Returns tuple of (scrubbed_text, count_of_redactions).
        """
        if not text:
            return "", 0

        redacted_count = 0
        scrubbed = text

        # Redact ABHA
        scrubbed, count = cls.ABHA_REGEX.subn("[ABHA_REDACTED]", scrubbed)
        redacted_count += count

        # Redact Aadhaar
        scrubbed, count = cls.AADHAAR_REGEX.subn("[AADHAAR_REDACTED]", scrubbed)
        redacted_count += count

        # Redact Email
        scrubbed, count = cls.EMAIL_REGEX.subn("[EMAIL_REDACTED]", scrubbed)
        redacted_count += count

        # Redact Phone
        scrubbed, count = cls.PHONE_REGEX.subn("[PHONE_REDACTED]", scrubbed)
        redacted_count += count

        return scrubbed, redacted_count
