from pydantic import BaseModel, Field, field_validator


class ProductReviewInput(BaseModel):
    review_id: str | None = Field(default=None, max_length=100)
    title: str | None = Field(default=None, max_length=200)
    body: str = Field(..., min_length=10, max_length=2000)
    rating: int | None = Field(default=None, ge=1, le=5)
    verified_purchase: bool = False

    @field_validator("body")
    @classmethod
    def normalize_body(cls, value: str) -> str:
        normalized = " ".join(value.split())
        if len(normalized) < 10:
            raise ValueError("review body must contain meaningful content")
        return normalized


class ReviewSummarizationRequest(BaseModel):
    product_name: str = Field(..., min_length=2, max_length=160)
    category_name: str | None = Field(default=None, max_length=120)
    reviews: list[ProductReviewInput] = Field(..., min_length=3, max_length=100)


class ReviewSummarizationResponse(BaseModel):
    product_name: str
    review_count: int
    pros: list[str] = Field(default_factory=list)
    cons: list[str] = Field(default_factory=list)
    common_complaints: list[str] = Field(default_factory=list)
    good_for: list[str] = Field(default_factory=list)
    avoid_for: list[str] = Field(default_factory=list)
    grounding_notes: list[str] = Field(default_factory=list)
    prompt_version: str
