from fastapi import APIRouter

from app.core.config import get_settings
from app.schemas.health import HealthResponse


router = APIRouter(prefix="/health", tags=["Health"])
settings = get_settings()


@router.get("", response_model=HealthResponse)
def health_check() -> HealthResponse:
    return HealthResponse(
        status="ok",
        service="fitcart-ai-service",
        environment=settings.app_env,
    )
