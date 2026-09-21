# Backend Architecture Rules

1. **Framework**: FastAPI with Python 3.12+, Pydantic v2 schemas, and SQLAlchemy 2 async engine.
2. **Contract-First**: Always update `docs/api/openapi.yaml` before writing backend route or schema code.
3. **Database & Migrations**:
   - PostgreSQL 16 with `pgvector` and `btree_gist` extensions.
   - All schema changes must go through Alembic migrations.
   - Row-Level Security (RLS) policies must be tested using dedicated integration tests.
4. **Services Layer**:
   - Routers handle HTTP parsing, dependency injection, and status codes.
   - All business logic lives in `app/services/`.
5. **No PHI in Logs**:
   - Every log message must pass through `logging_scrubber.py` to ensure zero Aadhaar, phone, email, or health identifiers leak into stdout/stderr.
6. **Background Tasks**:
   - Long-running tasks (OCR fallback, embeddings ingest, hash-chain anchoring) run asynchronously via Arq with Redis.
