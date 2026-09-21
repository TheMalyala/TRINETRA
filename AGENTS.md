# TRINETRA — agent rules
Product: Android health-records app (Kotlin/Compose) + FastAPI backend. Read docs/PRD.md and docs/adr/ before planning.

## Hard rules (never violate; if a task conflicts, STOP and ask)
1. AI never diagnoses/prescribes/changes doses. It explains, summarises, prepares questions.
2. Numbers come from deterministic code, never an LLM. LLM text is validated against source values.
3. Every patient-specific AI statement cites a record id; every medical fact cites an ingested source with a licence in knowledge/SOURCES.md.
4. OCR output is never saved without the user's confirm step (source crop shown beside value).
5. Local-first: Room + SQLCipher, Keystore-wrapped key, biometric/PIN lock, FLAG_SECURE on sensitive screens, allowBackup=false.
6. Access to another party's data requires an active consent grant (scoped, time-boxed, revocable); enforce in API and Postgres RLS.
7. Audit log and advice ledger are append-only and hash-chained. Records are versioned; never silently overwritten.
8. No PHI in logs, crash reports, analytics, git, or prompts to non-local LLMs. Only synthetic data in dev/test/demo (synthetic-data/).
9. No paid/closed dependency in the core. Anything proprietary must sit behind an interface and be optional.
10. Strict monochrome design tokens only (see docs/design/design-system.md). Never convey meaning by colour alone. WCAG AA.
11. Secrets never in the repo. Use .env.example + Docker secrets.
12. Doctors are "Unverified" until an admin verifies them. Prescription *issuing* is out of scope until legal review.

## Working method
- Contract-first: update docs/api/openapi.yaml before backend/Android API code; generate clients.
- For each phase: (a) write an Implementation Plan artifact and WAIT for approval; (b) small commits; (c) tests with the code; (d) run ./gradlew lint detekt test and pytest; (e) update docs + ADRs; (f) Walkthrough artifact with evidence; (g) stop at the phase gate.
- Use Context7 for library docs; do not invent APIs. If unsure of a version or licence, say so and verify.
- Never mock away safety logic in tests. Never weaken a validator to make a test pass.
- Prefer boring, well-known libraries. Explain any new dependency in an ADR (licence, cost, alternatives).

## Conventions
Android: multi-module, MVVM/UDF, Hilt, coroutines/Flow, no business logic in composables, Compose stability annotations where needed, no hard-coded strings/colours (resources + tokens), Indian-language ready (string resources, RTL-safe not required).
Backend: FastAPI, typed Pydantic v2, SQLAlchemy 2, Alembic migrations only, services layer, RLS tests for every policy.
Testing: JUnit5/MockK/Turbine, Compose UI tests, screenshot tests, pytest + Testcontainers, promptfoo evals for AI.
