import logging

from app.core.logging_scrubber import SensitiveDataScrubberFilter, scrub_pii


def test_scrub_pii():
    sample_text = (
        "Patient with Aadhaar 2345-6789-0123 and ABHA 12-3456-7890-1234 "
        "called from +91-9876543210 or email test.patient@example.com"
    )
    scrubbed = scrub_pii(sample_text)

    assert "2345-6789-0123" not in scrubbed
    assert "[REDACTED_AADHAAR]" in scrubbed
    assert "12-3456-7890-1234" not in scrubbed
    assert "[REDACTED_ABHA]" in scrubbed
    assert "9876543210" not in scrubbed
    assert "[REDACTED_PHONE]" in scrubbed
    assert "test.patient@example.com" not in scrubbed
    assert "[REDACTED_EMAIL]" in scrubbed


def test_filter_redacts_log_record():
    record = logging.LogRecord(
        name="test",
        level=logging.INFO,
        pathname="test.py",
        lineno=10,
        msg="Contact patient at +919876543210",
        args=(),
        exc_info=None,
    )
    f = SensitiveDataScrubberFilter()
    assert f.filter(record) is True
    assert "[REDACTED_PHONE]" in record.msg
    assert "9876543210" not in record.msg
