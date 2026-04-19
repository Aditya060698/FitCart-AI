from pydantic import BaseModel, Field, field_validator


class NutrientMetadataInput(BaseModel):
    nutrient_type: str | None = Field(default=None, max_length=80)
    aliases: list[str] = Field(default_factory=list, max_length=10)
    short_description: str | None = Field(default=None, max_length=400)
    possible_benefits: list[str] = Field(default_factory=list, max_length=12)
    product_categories: list[str] = Field(default_factory=list, max_length=10)
    warnings: list[str] = Field(default_factory=list, max_length=10)
    beginner_relevance: str | None = Field(default=None, max_length=300)

    @field_validator("aliases", "possible_benefits", "product_categories", "warnings")
    @classmethod
    def normalize_string_lists(cls, values: list[str]) -> list[str]:
        normalized_values: list[str] = []
        for value in values:
            normalized = " ".join(value.split()).strip()
            if normalized and normalized not in normalized_values:
                normalized_values.append(normalized)
        return normalized_values


class NutrientExplanationRequest(BaseModel):
    nutrient_name: str = Field(..., min_length=2, max_length=100)
    user_question: str = Field(..., min_length=5, max_length=400)
    user_goal: str | None = Field(default=None, max_length=120)
    context: str | None = Field(
        default=None,
        max_length=500,
        description="Optional user-facing context such as recovery, energy support, or daily wellness goals.",
    )
    metadata: NutrientMetadataInput = Field(
        ...,
        description="Structured nutrient facts retrieved by the backend before calling the AI service.",
    )


class NutrientExplanationResponse(BaseModel):
    nutrient_name: str
    answer_title: str
    direct_answer: str
    what_it_is: str
    possible_benefits: list[str] = Field(default_factory=list)
    commonly_found_in: list[str] = Field(default_factory=list)
    considerations: list[str] = Field(default_factory=list)
    beginner_note: str
    disclaimer: str
    grounding_notes: list[str] = Field(default_factory=list)
    prompt_version: str
