from fastapi import APIRouter

from app.schemas.vectors import SemanticSearchRequest, SemanticSearchResponse
from app.services.semantic_search_service import semantic_search


router = APIRouter(prefix="/search", tags=["Semantic Search"])


@router.post("/semantic", response_model=SemanticSearchResponse)
def semantic_search_route(request: SemanticSearchRequest) -> SemanticSearchResponse:
    return semantic_search(request)
