from pydantic import BaseModel, Field


class RecommendationAnalysisRequest(BaseModel):
    goal: str = Field(..., min_length=2, max_length=120)
    budget: float | None = Field(default=None, ge=0)
    dietary_preferences: list[str] = Field(default_factory=list)
    exclusions: list[str] = Field(default_factory=list)
    prompt: str | None = Field(
        default=None,
        max_length=1000,
        description="Original natural-language request from the client.",
    )


class RecommendationAnalysisResponse(BaseModel):
    interpreted_goal: str
    suggested_categories: list[str]
    applied_constraints: list[str]
    reasoning_summary: str
    confidence: str
