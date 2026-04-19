from fastapi import APIRouter

from app.schemas.nutrients import NutrientExplanationRequest, NutrientExplanationResponse
from app.services.nutrient_service import explain_nutrient


router = APIRouter(prefix="/nutrients", tags=["Nutrients"])


@router.post("/explain", response_model=NutrientExplanationResponse)
def explain_nutrient_route(request: NutrientExplanationRequest) -> NutrientExplanationResponse:
    return explain_nutrient(request)
