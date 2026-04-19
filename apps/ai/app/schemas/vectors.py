from __future__ import annotations

from datetime import datetime
from enum import Enum

from pydantic import BaseModel, Field, field_validator


class VectorSourceType(str, Enum):
    PRODUCT_DESCRIPTION = "product_description"
    INGREDIENT_METADATA = "ingredient_metadata"
    REVIEW_SUMMARY = "review_summary"
    EDUCATIONAL_NUTRIENT_NOTE = "educational_nutrient_note"


class VectorDocumentMetadata(BaseModel):
    source_id: str = Field(..., min_length=1, max_length=120)
    title: str | None = Field(default=None, max_length=200)
    category: str | None = Field(default=None, max_length=120)
    brand: str | None = Field(default=None, max_length=120)
    nutrient_name: str | None = Field(default=None, max_length=120)
    ingredient_name: str | None = Field(default=None, max_length=120)
    review_count: int | None = Field(default=None, ge=0)
    tags: list[str] = Field(default_factory=list, max_length=20)
    version: int = Field(default=1, ge=1)
    updated_at: datetime | None = None

    @field_validator("tags")
    @classmethod
    def normalize_tags(cls, values: list[str]) -> list[str]:
        normalized_values: list[str] = []
        for value in values:
            normalized = " ".join(value.split()).strip().lower()
            if normalized and normalized not in normalized_values:
                normalized_values.append(normalized)
        return normalized_values


class VectorDocumentInput(BaseModel):
    source_type: VectorSourceType
    metadata: VectorDocumentMetadata
    text: str = Field(..., min_length=20, max_length=15000)

    @field_validator("text")
    @classmethod
    def normalize_text(cls, value: str) -> str:
        normalized = " ".join(value.split())
        if len(normalized) < 20:
            raise ValueError("document text must contain meaningful content")
        return normalized


class VectorChunkRecord(BaseModel):
    chunk_id: str
    source_type: VectorSourceType
    source_id: str
    chunk_index: int
    text: str
    embedding: list[float]
    metadata: dict[str, str | int | list[str] | None]


class VectorIngestionRequest(BaseModel):
    documents: list[VectorDocumentInput] = Field(..., min_length=1, max_length=50)
    replace_existing: bool = True


class IngestedDocumentSummary(BaseModel):
    source_id: str
    source_type: VectorSourceType
    chunk_count: int
    version: int


class VectorIngestionResponse(BaseModel):
    ingested_documents: list[IngestedDocumentSummary]
    total_chunks_written: int
    vector_dimension: int
    target_store: str
    notes: list[str] = Field(default_factory=list)


class SemanticSearchRequest(BaseModel):
    query: str = Field(..., min_length=3, max_length=300)
    top_k: int = Field(default=5, ge=1, le=20)
    source_types: list[VectorSourceType] = Field(
        default_factory=lambda: [
            VectorSourceType.PRODUCT_DESCRIPTION,
            VectorSourceType.REVIEW_SUMMARY,
        ]
    )
    category: str | None = Field(default=None, max_length=120)
    brand: str | None = Field(default=None, max_length=120)
    tags: list[str] = Field(default_factory=list, max_length=10)

    @field_validator("query")
    @classmethod
    def normalize_query(cls, value: str) -> str:
        return " ".join(value.split()).strip()

    @field_validator("tags")
    @classmethod
    def normalize_search_tags(cls, values: list[str]) -> list[str]:
        normalized_values: list[str] = []
        for value in values:
            normalized = " ".join(value.split()).strip().lower()
            if normalized and normalized not in normalized_values:
                normalized_values.append(normalized)
        return normalized_values


class SemanticSearchResult(BaseModel):
    source_id: str
    title: str | None = None
    category: str | None = None
    brand: str | None = None
    tags: list[str] = Field(default_factory=list)
    matched_source_types: list[VectorSourceType] = Field(default_factory=list)
    score: float
    evidence_chunks: list[str] = Field(default_factory=list)


class SemanticSearchResponse(BaseModel):
    query: str
    results: list[SemanticSearchResult]
    total_candidates_considered: int
    notes: list[str] = Field(default_factory=list)
