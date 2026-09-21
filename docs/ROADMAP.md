# TRINETRA — Roadmap & Phase Gates

| Phase | Name | Focus Areas | Gate Requirements |
|---|---|---|---|
| **0** | **Foundation** | Monorepo, Gradle multi-module, Version Catalog, CI, Detekt, FastAPI skeleton, Docker Compose, OpenAPI v0, ADRs | Builds, tests pass, Docker healthy, CI green. |
| **1** | **Design System & Shell** | Monochrome design tokens, typography, custom components, medical icon set, navigation shell, app-lock | Hardcoded color lint clean, screenshot tests pass. |
| **2** | **Secure Local Core** | Room + SQLCipher, Keystore, Biometric lock, profiles, doctor cards, Advice Ledger hash-chain, Vault CRUD + FTS5 | DB unreadable without key, tamper detection tests. |
| **3** | **Scanner & OCR** | ML Kit scan & OCR, parser, LOINC matcher, confirm UI, duplicate detection, bills capture | ≥95% extraction on synthetic test set; NN-4 test. |
| **4** | **Analytics** | Pure Kotlin trend engine, Vico charts, panels, medication-change overlays, visit-prep PDF | 100% unit test coverage on trend rules. |
| **5** | **Knowledge & AI** | Ingest pipeline, pgvector, RAG, AI Gateway, Presidio redaction, guardrails, Netra text assistant | Eval thresholds met (100% refusal on diagnosis). |
| **6** | **Backend & Sync** | Auth (Argon2id, JWT, TOTP), sync outbox, encrypted backup/restore, RLS policies, logging scrubber | API contract tests, RLS tests pass. |
| **7** | **Messenger** | WS chat, attachments, message→Advice promotion, offline outbox, notifications | Delivery/ordering tests pass; no hard delete. |
| **8** | **Appointments** | Doctor availability rules (RRULE), slot booking with exclusion constraints, reminders | Concurrency test (no double-booking). |
| **9** | **Consent & Audit** | Consent manager, RLS scopes, activity feed, audit chain daily anchoring | Revocation < 5s; tamper test pass. |
| **10** | **Voice** | STT/TTS abstractions, voice agent, red-flag first pipeline | Latency budget met; red-flag recall ≥ 99%. |
| **11** | **Hardening** | Threat-model review, Semgrep, ZAP, MobSF, accessibility audit, battery profiling | Zero high/critical security findings. |
| **12** | **Interop & Release** | FHIR R4 export validation, ABHA sandbox, DPDP compliance audit, Play internal release | Release readiness checklist signed off. |
