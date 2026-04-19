from fastapi import APIRouter

from app.api.v1.routes import documents, health, nutrients, recommendations, reviews, semantic_search, vectors
from app.core.config import get_settings


settings = get_settings()

api_router = APIRouter()
v1_router = APIRouter(prefix=settings.api_prefix)

v1_router.include_router(health.router)
v1_router.include_router(documents.router)
v1_router.include_router(nutrients.router)
v1_router.include_router(recommendations.router)
v1_router.include_router(reviews.router)
v1_router.include_router(semantic_search.router)
v1_router.include_router(vectors.router)

api_router.include_router(v1_router)
