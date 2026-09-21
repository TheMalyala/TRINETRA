import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_healthz(async_client: AsyncClient):
    response = await async_client.get("/healthz")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


@pytest.mark.asyncio
async def test_readyz(async_client: AsyncClient):
    response = await async_client.get("/readyz")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ready"
    assert data["database"] == "connected"
    assert data["redis"] == "connected"
