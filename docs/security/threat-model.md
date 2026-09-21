# Threat Model (STRIDE-lite)

| Threat Category | Specific Threat | Impact | Technical Control in Trinetra |
|---|---|---|---|
| **Spoofing** | Doctor impersonation / fake medical credentials | High | Verification status flag defaults to `unverified`; admin manual credential verification before badge; ECDSA signed advice. |
| **Tampering** | Alteration of medical advice or lab history | Critical | SHA-256 hash chaining of Advice Ledger and audit records; Room DB tamper detection; TSA Merkle anchoring. |
| **Repudiation** | Doctor denies giving advice or patient denies acknowledging | High | Non-deletable advice ledger; cryptographic signature verification; timestamped acknowledgment records. |
| **Information Disclosure** | Extraction of local database from stolen device | Critical | SQLCipher AES-256 database encryption; keys wrapped by Android Keystore hardware; `FLAG_SECURE` prevents recents capture; `allowBackup="false"`. |
| **Information Disclosure** | Cross-patient record access via backend API | Critical | PostgreSQL Row-Level Security (RLS) enforcing consent scopes; zero data returned if consent is missing or expired. |
| **Denial of Service** | Flooding backend or brute-forcing accounts | Medium | Rate limiting on authentication routes; Argon2id memory limits; exponential lockout on repeated biometric/PIN failures. |
| **Elevation of Privilege** | Normal user accesses admin verification endpoints | High | Strict role-based access control (RBAC) enforced in FastAPI dependencies. |
| **Adversarial Injection** | Prompt injection inside scanned document images | Medium | Scanned OCR text passed strictly as typed JSON data fields, never as prompt instructions; tool-less LLM prompt templates. |
