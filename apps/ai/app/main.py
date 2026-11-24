from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.router import api_router
from app.core.config import get_settings


settings = get_settings()

app = FastAPI(
    title=settings.app_name,
    version="0.1.0",
    debug=settings.app_debug,
    description="AI service for FitCart AI",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.normalized_web_allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router)


@app.get("/health", tags=["Health"])
def root_health() -> dict[str, str]:
    return {
        "status": "ok",
        "service": "fitcart-ai-service",
        "environment": settings.app_env,
    }
