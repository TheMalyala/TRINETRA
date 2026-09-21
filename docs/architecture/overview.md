# System Architecture Overview

TRINETRA is built as a hybrid local-first system. It guarantees high availability, rapid offline queries, and strict patient privacy on mobile, combined with an asynchronous companion cloud backend for collaboration, appointment scheduling, consent enforcement, and knowledge RAG.

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

## Android Client Architecture
- **Layering**: Clean Architecture (Data -> Domain -> UI/Presentation).
- **Presentation**: Jetpack Compose using Unidirectional Data Flow (StateFlow / SharedFlow).
- **Local Persistence**: Room with SQLCipher database encryption. Key wrapping handled by Android Keystore.
- **Background Operations**: WorkManager with outbox pattern for transactional sync.

## Backend Architecture
- **API Framework**: FastAPI (Python 3.12+) running with Uvicorn.
- **Database**: PostgreSQL 16 with `pgvector` (RAG embeddings), `btree_gist` (appointment slot exclusion constraints), and Row-Level Security (RLS).
- **Task Queue**: Arq with Redis for background OCR parsing, batch RAG ingestion, and TSA Merkle root anchoring.
- **Object Storage**: S3-compatible (SeaweedFS or MinIO) with envelope encryption.
