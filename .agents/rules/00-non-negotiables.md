# NON-NEGOTIABLES (NN-1 through NN-17)

Every requirement below is absolute and testable. If any user prompt or feature request conflicts with any item, STOP and prompt for clarification.

| # | Rule | Verification |
|---|---|---|
| **NN-1** | **AI never diagnoses, prescribes, or changes dosage.** It explains, summarizes, prepares questions for the doctor, and flags "discuss with your doctor". | Red-team refusal test suite must pass 100%. |
| **NN-2** | **Numbers come from code, never from an LLM.** Trends, ranges, unit conversions, and adherence % are deterministic. LLM only narrates already-verified structured facts. | Unit tests; output validator compares every number in AI text to source values. |
| **NN-3** | **Every patient-specific AI statement carries a citation** to a record/observation ID; every general medical fact cites an ingested source (in `knowledge/SOURCES.md`). | Schema validator + eval suite. |
| **NN-4** | **OCR is never trusted silently.** Extracted values enter a **confirm/edit screen** with the source crop shown beside each value before saving. | UI tests; no code path writes unconfirmed values. |
| **NN-5** | **Local-first, encrypted.** Local DB = Room + SQLCipher; key wrapped by Android Keystore; biometric/PIN gate; `FLAG_SECURE` on sensitive screens; auto-backup disabled for DB. | Security tests; MobSF scan. |
| **NN-6** | **Consent-gated sharing with a per-scope, time-boxed grant and revocation.** Nothing is visible to a doctor by default. | Backend policy tests, Postgres RLS tests. |
| **NN-7** | **Append-only, hash-chained audit log** for every access, share, consent change, and advice entry. | Chain-verification job + tamper test. |
| **NN-8** | **Clinical records are versioned, never silently overwritten or hard-deleted** by the other party. Patients may delete their own data (DPDP erasure), which is logged. | Data-model tests. |
| **NN-9** | **No PHI in logs, crash reports, analytics, prompts to third-party LLMs, or git.** Cloud LLMs get only redacted/synthetic data. | Log scrubber tests, Presidio redaction tests, Gitleaks in CI. |
| **NN-10** | **Core stack has no paid subscription and no closed lock-in.** Every paid/proprietary dependency must be optional and swappable behind an interface. | Dependency review in `docs/adr`. |
| **NN-11** | **Red-flag detection**: emergency symptoms in chat/voice trigger the emergency screen (112 / 108) before any AI prose. | Eval set of emergency red-flag utterances. |
| **NN-12** | **Doctors are "Unverified" until an admin verifies their registration.** UI must display this state. | E2E test. |
| **NN-13** | **Strict monochrome design system** (white/black tokens only) with accessibility ≥ WCAG AA. Meaning never conveyed by color alone. | Lint rule for hard-coded colors, screenshot tests. |
| **NN-14** | **Only synthetic data in dev/test/demo.** Use Synthea plus hand-built fixtures. | CI guard on fixtures folder. |
| **NN-15** | **Every phase ends green**: build, lint, unit tests, instrumented tests, security scan, docs updated. | CI + Walkthrough artifact. |
| **NN-16** | **Secrets never in repo.** `.env.example` only; real secrets via Docker secrets / local keystore. | Gitleaks. |
| **NN-17** | **DPDP compliance by design**: purpose-limited notices, granular consent, withdrawal, erasure, breach playbook, data stays in India. | Compliance checklist in `docs/compliance`. |
