from fastapi import APIRouter

from app.schemas.reviews import ReviewSummarizationRequest, ReviewSummarizationResponse
from app.services.review_summary_service import summarize_product_reviews


router = APIRouter(prefix="/reviews", tags=["Reviews"])


@router.post("/summarize", response_model=ReviewSummarizationResponse)
def summarize_reviews_route(
    request: ReviewSummarizationRequest,
) -> ReviewSummarizationResponse:
    return summarize_product_reviews(request)
