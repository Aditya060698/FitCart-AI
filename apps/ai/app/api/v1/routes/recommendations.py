from fastapi import APIRouter

from app.schemas.recommendations import RecommendationAnalysisRequest, RecommendationAnalysisResponse
from app.services.recommendation_service import analyze_recommendation_request


router = APIRouter(prefix="/recommendations", tags=["Recommendations"])


@router.post("/analyze", response_model=RecommendationAnalysisResponse)
def analyze_recommendation_route(
        request: RecommendationAnalysisRequest,
) -> RecommendationAnalysisResponse:
    return analyze_recommendation_request(request)
