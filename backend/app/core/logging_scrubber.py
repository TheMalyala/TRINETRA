import logging
import re

# Regular expressions for Indian IDs and PII
# Order matters: check longer/prefixed patterns before generic digit sequences
ABHA_REGEX = re.compile(r"\b[0-9]{2}[-\s]?[0-9]{4}[-\s]?[0-9]{4}[-\s]?[0-9]{4}\b")
PHONE_REGEX = re.compile(r"(?:\+91[-\s]?|\b)[6-9]\d{9}\b")
AADHAAR_REGEX = re.compile(r"(?<!\+)(?<!\d)[2-9]{1}[0-9]{3}[-\s]?[0-9]{4}[-\s]?[0-9]{4}(?!\d)")
EMAIL_REGEX = re.compile(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,7}\b")


def scrub_pii(text: str) -> str:
    """Replaces sensitive Indian PII and identifiers with redacted tokens."""
    if not isinstance(text, str):
        return text

    scrubbed = EMAIL_REGEX.sub("[REDACTED_EMAIL]", text)
    scrubbed = ABHA_REGEX.sub("[REDACTED_ABHA]", scrubbed)
    scrubbed = PHONE_REGEX.sub("[REDACTED_PHONE]", scrubbed)
    scrubbed = AADHAAR_REGEX.sub("[REDACTED_AADHAAR]", scrubbed)
    return scrubbed


class SensitiveDataScrubberFilter(logging.Filter):
    """Logging filter that redacts PII from all log records."""

    def filter(self, record: logging.LogRecord) -> bool:
        if isinstance(record.msg, str):
            record.msg = scrub_pii(record.msg)
        if record.args:
            if isinstance(record.args, dict):
                record.args = {
                    k: scrub_pii(v) if isinstance(v, str) else v
                    for k, v in record.args.items()
                }
            elif isinstance(record.args, tuple):
                record.args = tuple(
                    scrub_pii(arg) if isinstance(arg, str) else arg
                    for arg in record.args
                )
        return True


def setup_logging():
    """Applies the sensitive data scrubber filter to the root logger."""
    root_logger = logging.getLogger()
    scrubber = SensitiveDataScrubberFilter()
    root_logger.addFilter(scrubber)
    for handler in root_logger.handlers:
        handler.addFilter(scrubber)
