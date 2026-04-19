from pydantic import BaseModel, Field, field_validator


class DocumentAnalysisRequest(BaseModel):
    document_id: str | None = Field(default=None, max_length=120)
    document_type: str | None = Field(default=None, max_length=80)
    extracted_text: str = Field(..., min_length=40, max_length=30000)
    source_format: str | None = Field(default=None, max_length=40)

    @field_validator("extracted_text")
    @classmethod
    def normalize_text(cls, value: str) -> str:
        normalized = " ".join(value.split())
        if len(normalized) < 40:
            raise ValueError("extracted_text must contain enough content for grounded analysis")
        return normalized


class ImportantTermExplanation(BaseModel):
    term: str
    explanation: str


class DocumentAnalysisResponse(BaseModel):
    document_id: str | None = None
    detected_document_type: str
    simplified_summary: str
    important_terms: list[ImportantTermExplanation] = Field(default_factory=list)
    supplement_relevant_observations: list[str] = Field(default_factory=list)
    disclaimer: str
    grounding_notes: list[str] = Field(default_factory=list)
    prompt_version: str
