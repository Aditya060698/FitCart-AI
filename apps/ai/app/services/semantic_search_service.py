from __future__ import annotations

import math
from collections import defaultdict

from app.core.config import get_settings
from app.repositories.vector_store_repository import FileVectorStoreRepository
from app.schemas.vectors import (
    SemanticSearchRequest,
    SemanticSearchResponse,
    SemanticSearchResult,
    VectorChunkRecord,
    VectorSourceType,
)
from app.services.vector_ingestion_service import _generate_embedding


def semantic_search(request: SemanticSearchRequest) -> SemanticSearchResponse:
    settings = get_settings()
    repository = FileVectorStoreRepository(settings.vector_store_file_path)
    chunks = repository.read_all_chunks()

    filtered_chunks = [
        chunk
        for chunk in chunks
        if _matches_filters(chunk, request)
    ]

    query_embedding = _generate_embedding(request.query)
    scored_chunks = [
        (chunk, _score_chunk(chunk, request.query, query_embedding))
        for chunk in filtered_chunks
    ]
    scored_chunks = [
        (chunk, score)
        for chunk, score in scored_chunks
        if score > 0
    ]

    aggregated_results = _aggregate_document_results(scored_chunks)
    ranked_results = sorted(
        aggregated_results,
        key=lambda result: result.score,
        reverse=True,
    )[:request.top_k]

    return SemanticSearchResponse(
        query=request.query,
        results=ranked_results,
        total_candidates_considered=len(filtered_chunks),
        notes=[
            "Semantic retrieval is best used alongside structured filtering, not as a replacement for it.",
            "Scores combine vector similarity with lightweight lexical and metadata boosts.",
            "Document results are aggregated from chunk-level evidence so Spring Boot can re-rank or intersect them with SQL filters.",
        ],
    )


def _matches_filters(chunk: VectorChunkRecord, request: SemanticSearchRequest) -> bool:
    if chunk.source_type not in request.source_types:
        return False

    metadata = chunk.metadata
    if request.category and metadata.get("category") != request.category:
        return False
    if request.brand and metadata.get("brand") != request.brand:
        return False

    if request.tags:
        chunk_tags = metadata.get("tags") or []
        if not isinstance(chunk_tags, list):
            return False
        normalized_chunk_tags = [str(tag).lower() for tag in chunk_tags]
        if not any(tag in normalized_chunk_tags for tag in request.tags):
            return False

    return True


def _score_chunk(
    chunk: VectorChunkRecord,
    query: str,
    query_embedding: list[float],
) -> float:
    similarity = _cosine_similarity(query_embedding, chunk.embedding)
    lexical_boost = _lexical_overlap_boost(query, chunk.text)
    metadata_boost = _metadata_boost(query, chunk.metadata)
    return round((similarity * 0.8) + lexical_boost + metadata_boost, 6)


def _cosine_similarity(left: list[float], right: list[float]) -> float:
    if not left or not right or len(left) != len(right):
        return 0.0

    numerator = sum(a * b for a, b in zip(left, right, strict=False))
    left_norm = math.sqrt(sum(value * value for value in left))
    right_norm = math.sqrt(sum(value * value for value in right))
    denominator = left_norm * right_norm
    if denominator == 0:
        return 0.0
    return numerator / denominator


def _lexical_overlap_boost(query: str, chunk_text: str) -> float:
    query_tokens = set(_tokenize_for_search(query))
    chunk_tokens = set(_tokenize_for_search(chunk_text))
    if not query_tokens or not chunk_tokens:
        return 0.0

    overlap = len(query_tokens.intersection(chunk_tokens))
    return min(overlap * 0.03, 0.15)


def _metadata_boost(query: str, metadata: dict[str, str | int | list[str] | None]) -> float:
    query_tokens = set(_tokenize_for_search(query))
    score = 0.0

    title = metadata.get("title")
    category = metadata.get("category")
    tags = metadata.get("tags")

    for field_value, weight in ((title, 0.04), (category, 0.03)):
        if isinstance(field_value, str):
            field_tokens = set(_tokenize_for_search(field_value))
            if query_tokens.intersection(field_tokens):
                score += weight

    if isinstance(tags, list):
        tag_tokens = set(str(tag).lower() for tag in tags)
        if query_tokens.intersection(tag_tokens):
            score += 0.05

    return min(score, 0.12)


def _aggregate_document_results(
    scored_chunks: list[tuple[VectorChunkRecord, float]],
) -> list[SemanticSearchResult]:
    grouped: dict[str, list[tuple[VectorChunkRecord, float]]] = defaultdict(list)
    for chunk, score in scored_chunks:
        grouped[chunk.source_id].append((chunk, score))

    results: list[SemanticSearchResult] = []
    for source_id, items in grouped.items():
        ordered_items = sorted(items, key=lambda item: item[1], reverse=True)
        top_scores = [score for _chunk, score in ordered_items[:3]]
        top_chunks = [chunk.text for chunk, _score in ordered_items[:2]]
        representative_chunk = ordered_items[0][0]
        metadata = representative_chunk.metadata
        matched_source_types = list({
            chunk.source_type
            for chunk, _score in ordered_items
        })

        results.append(
            SemanticSearchResult(
                source_id=source_id,
                title=metadata.get("title") if isinstance(metadata.get("title"), str) else None,
                category=metadata.get("category") if isinstance(metadata.get("category"), str) else None,
                brand=metadata.get("brand") if isinstance(metadata.get("brand"), str) else None,
                tags=[str(tag) for tag in metadata.get("tags", [])] if isinstance(metadata.get("tags"), list) else [],
                matched_source_types=matched_source_types,
                score=round(sum(top_scores) / len(top_scores), 6),
                evidence_chunks=top_chunks,
            )
        )

    return results


def _tokenize_for_search(text: str) -> list[str]:
    normalized = "".join(character.lower() if character.isalnum() else " " for character in text)
    return [token for token in normalized.split() if token]
