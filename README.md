# TRINETRA (त्रिनेत्र)

> **The eye that sees what the body hides.**  
> *Nothing hidden. Nothing forgotten. Nothing unaccountable.*

Trinetra is an open-source, privacy-first, local-first Android health-records application paired with an asynchronous FastAPI companion backend.

---

## Key Features

- **Records Vault**: Organized by doctor, specialization, date, and document type. Indexed locally with SQLite FTS5.
- **Document Scanner & OCR**: ML Kit on-device scan with a strict **confirm-before-save** requirement (NN-4).
- **Prescription Explainer & Jargon Decoder**: Plain-language, grounded explanations with citations to licensed open sources (MedlinePlus, openFDA, LOINC).
- **Deterministic Trend Analytics**: Pure Kotlin analytics engine (NN-2) calculating biomarker trends without LLM hallucinations.
- **Doctor Care Circle & Advice Ledger**: Append-only, SHA-256 hash-chained record of all medical advice received.
- **Safety First**: Red-flag detection triggers instant emergency response (112/108) before any AI prose.
- **Monochrome Design**: High-contrast, calm, clinical white/black design system complying with WCAG AA.

---

## Architecture Overview

```
                          ┌────────────────────────── ANDROID (Kotlin, Compose) ──────────────────────────┐
                          │  UI features ── ViewModels ── UseCases ── Repositories                        │
                          │        │                                       │                              │
                          │  CameraX/ML Kit scan → OCR → Parser → CONFIRM  Room+SQLCipher (source of truth│
                          │  Charts (Vico) ◄── Trend engine (pure Kotlin)  for private data)             │
                          │  STT/TTS (on-device)   WorkManager (sync, reminders)   Keystore/Tink          │
                          └───────────────┬───────────────────────────────────────────────┬───────────────┘
                                  HTTPS/TLS1.3 + WSS                                      │ encrypted backup
                          ┌───────────────▼──────────────── BACKEND (FastAPI) ────────────▼───────────────┐
                          │ Auth │ Consent/Policy │ Records │ Chat(WS) │ Appointments │ Audit │ AI Gateway │
                          │            Postgres 16 (+RLS, pgvector, btree_gist)   Redis   Object store    │
                          │  Workers (Arq): OCR fallback, RAG ingest, hash-chain anchoring, notifications  │
                          └───────────────┬─────────────────────────────────────────────┬─────────────────┘
                                          │                                             │
                               LLM providers (adapter)                        Knowledge store (pgvector)
                        Ollama local (Apache-2 models) │ optional cloud       MedlinePlus · openFDA · LOINC ·
                        (synthetic/redacted data only)                        ICD-11 · own curated glossary
```

---

## Repository Structure

- `android/`: Android multi-module application (Kotlin 2.x, Jetpack Compose, Room + SQLCipher, Hilt).
- `backend/`: FastAPI backend (Python 3.12+, SQLAlchemy 2, Alembic, PostgreSQL 16 with pgvector & RLS, Redis, Arq).
- `docs/`: Architecture Decision Records (ADRs), PRD, compliance guidelines, threat models, and OpenAPI specifications.
- `knowledge/`: Grounded medical knowledge registry, curated term glossaries, and evaluation datasets.
- `infra/`: Docker Compose configurations for local development and deployment.
- `synthetic-data/`: Synthetic test datasets generated using Synthea (strictly no real PHI).

---

## Quickstart

### Prerequisites
- **Android**: Android Studio Koala / Ladybug or newer, JDK 17 or 21, Android SDK 34+.
- **Backend**: Python 3.12+, Docker & Docker Compose.

### Running Backend Locally
```bash
cd infra
docker compose -f docker-compose.dev.yml up -d
cd ../backend
python -m venv .venv
# On Windows: .venv\Scripts\activate
# On Linux/macOS: source .venv/bin/activate
pip install -e ".[dev]"
pytest
uvicorn app.main:app --reload --port 8000
```

### Health Endpoints
- Live: `http://localhost:8000/healthz`
- Ready: `http://localhost:8000/readyz`
- OpenAPI Docs: `http://localhost:8000/docs`

---

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.
