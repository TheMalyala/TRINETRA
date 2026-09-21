from typing import AsyncGenerator

from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from app.core.config import settings

engine = create_async_engine(
    settings.SQLALCHEMY_DATABASE_URI, echo=False, future=True, pool_pre_ping=True
)

AsyncSessionLocal = async_sessionmaker(
    bind=engine, class_=AsyncSession, autocommit=False, autoflush=False, expire_on_commit=False
)


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()
