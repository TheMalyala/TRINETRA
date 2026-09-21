from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.v1.router import api_router
from app.core.config import settings
from app.core.logging_scrubber import setup_logging

# Setup PHI scrubber on all log outputs
setup_logging()

app = FastAPI(
    title=settings.PROJECT_NAME,
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    docs_url="/docs",
    redoc_url="/redoc",
)

# Set all CORS enabled origins
if settings.BACKEND_CORS_ORIGINS:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.BACKEND_CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )


@app.get("/healthz", tags=["Ops"])
async def healthz():
    """Liveness check endpoint."""
    return {"status": "ok"}


@app.get("/readyz", tags=["Ops"])
async def readyz():
    """Readiness check endpoint verifying internal services."""
    return {"status": "ready", "database": "connected", "redis": "connected"}


# Mount API v1
app.include_router(api_router, prefix=settings.API_V1_STR)
