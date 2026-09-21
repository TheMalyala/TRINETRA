# TRINETRA — Product Requirements Document (PRD)

> **Identity:** TRINETRA (त्रिनेत्र) — "The Three-Eyed One"  
> **Tagline:** *The eye that sees what the body hides.*  
> **Micro-voice:** *"Nothing hidden. Nothing forgotten. Nothing unaccountable."*

---

## 1. Problem Statement
Indian healthcare consumers hold highly fragmented paper records, handwritten prescriptions, and scattered diagnostic PDFs across disparate labs, clinics, and hospitals. Patients cannot decode dense medical jargon or interpret lab reference ranges. Crucially, advice given during consultations is often lost, unverified, or unaccountable.

---

## 2. Product Pillars & Principles

1. **Single-Player First**: Zero network dependency for core value. Works immediately for a patient entering their records and self-reporting doctor advice.
2. **Deterministic Computation**: Numbers and trend analytics are calculated strictly in code (pure Kotlin), never hallucinated by LLMs.
3. **Accountability & Integrity**: Tamper-evident, hash-chained Advice Ledger logs who advised what, when, with doctor ECDSA signatures where available.
4. **Safety Net**: Instant red-flag triage on critical symptoms (112/108 routing) before any AI prose is generated.
5. **Monochrome Calm**: High-contrast, austere white/black design system that never conveys clinical meaning through color alone (WCAG AA).

---

## 3. User Personas & Roles

- **Patient (Self)**: Organizes records, decodes jargon, tracks trends, schedules visits.
- **Family Profile**: Manages dependants (children, aging parents). DPDP verifiable parental consent required for under-18s.
- **Doctor (Care Circle)**: Publishes availability, reviews shared records under time-boxed consent, logs signed advice.
- **Admin**: Verifies medical registration certificates before awarding verification badges.

---

## 4. MVP Feature Set (Phases 0–8)

- **F1: Auth & Biometric Lock**: Argon2id, TOTP 2FA, Keystore-bound biometric lock, auto-lock timeout.
- **F2: Profiles & Family**: Dependant switcher, guardian consent logging.
- **F3: Doctor Directory**: Care Circle with specialization taxonomy and verified badges.
- **F4: Advice Ledger**: Chronological log per doctor, hash-chained, versioned, doctor-signed or self-reported.
- **F5: Records Vault**: Linked to doctor, specialty, encounter, date, type. SQLite FTS5 search.
- **F6: Scanner & OCR**: ML Kit Scanner + OCR, table extraction, mandatory confirm screen.
- **F7: Prescription Explainer**: Generic translation, indications, cautions, drug interactions.
- **F8: Jargon Decoder**: Layered definitions (1-liner → plain explanation → what to ask doctor).
- **F9: Trend Analytics**: Pure Kotlin trend engine, Vico charts, reference ranges, medication change overlays.
- **F10: Bills Vault**: Receipt scanning, expense summaries, insurance/tax export pack.
- **F11: Appointments**: Doctor RRULE availability, double-booking prevention via Postgres exclusion constraints.
- **F12: Messenger**: Consent-scoped record sharing, message-to-advice promotion.
- **F13: Assistant "Netra"**: Grounded RAG with strict citations to registered sources.
