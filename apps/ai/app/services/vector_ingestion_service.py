from __future__ import annotations

import hashlib
import math
import uuid
from collections.abc import Iterable

from app.core.config import get_settings
from app.repositories.vector_store_repository import FileVectorStoreRepository
from app.schemas.vectors import (
    IngestedDocumentSummary,
    VectorChunkRecord,
    VectorDocumentInput,
    VectorIngestionRequest,
    VectorIngestionResponse,
    VectorSourceType,
)


EMBEDDING_DIMENSION = 24


def ingest_documents(request: VectorIngestionRequest) -> VectorIngestionResponse:
    settings = get_settings()
    repository = FileVectorStoreRepository(settings.vector_store_file_path)

    ingested_documents: list[IngestedDocumentSummary] = []
    total_chunks_written = 0

    for document in request.documents:
        chunks = _build_chunk_records(document)

        if request.replace_existing:
            repository.replace_document_chunks(
                source_type=document.source_type,
                source_id=document.metadata.source_id,
                chunks=chunks,
            )
        else:
            repository.append_document_chunks(chunks)

        ingested_documents.append(
            IngestedDocumentSummary(
                source_id=document.metadata.source_id,
                source_type=document.source_type,
                chunk_count=len(chunks),
                version=document.metadata.version,
            )
        )
        total_chunks_written += len(chunks)

    return VectorIngestionResponse(
        ingested_documents=ingested_documents,
        total_chunks_written=total_chunks_written,
        vector_dimension=EMBEDDING_DIMENSION,
        target_store=settings.vector_store,
        notes=[
            "This pipeline uses deterministic placeholder embeddings for local development.",
            "Replace the embedding function and repository implementation when wiring pgvector or Chroma.",
            "Chunk metadata is stored with every vector record to support filtered retrieval and selective reindexing.",
        ],
    )


def _build_chunk_records(document: VectorDocumentInput) -> list[VectorChunkRecord]:
    chunks = _chunk_document(document)
    chunk_records: list[VectorChunkRecord] = []

    for index, chunk_text in enumerate(chunks):
        chunk_records.append(
            VectorChunkRecord(
                chunk_id=f"{document.source_type.value}:{document.metadata.source_id}:{index}:{uuid.uuid4().hex[:8]}",
                source_type=document.source_type,
                source_id=document.metadata.source_id,
                chunk_index=index,
                text=chunk_text,
                embedding=_generate_embedding(chunk_text),
                metadata=_build_chunk_metadata(document, index),
            )
        )

    return chunk_records


def _chunk_document(document: VectorDocumentInput) -> list[str]:
    if document.source_type == VectorSourceType.PRODUCT_DESCRIPTION:
        return _chunk_by_sentence_windows(document.text, window_size=2, max_chunk_length=320)
    if document.source_type == VectorSourceType.INGREDIENT_METADATA:
        return _chunk_by_semicolon_or_sentence(document.text, max_chunk_length=280)
    if document.source_type == VectorSourceType.REVIEW_SUMMARY:
        return _chunk_by_sentence_windows(document.text, window_size=1, max_chunk_length=260)
    if document.source_type == VectorSourceType.EDUCATIONAL_NUTRIENT_NOTE:
        return _chunk_by_sentence_windows(document.text, window_size=2, max_chunk_length=300)

    return [document.text]


def _chunk_by_sentence_windows(
    text: str,
    window_size: int,
    max_chunk_length: int,
) -> list[str]:
    sentences = _split_sentences(text)
    if not sentences:
        return [text]

    chunks: list[str] = []
    current_window: list[str] = []

    for sentence in sentences:
        candidate = " ".join(current_window + [sentence]).strip()
        if current_window and (
            len(current_window) >= window_size or len(candidate) > max_chunk_length
        ):
            chunks.append(" ".join(current_window).strip())
            current_window = [sentence]
            continue

        current_window.append(sentence)

    if current_window:
        chunks.append(" ".join(current_window).strip())

    return chunks


def _chunk_by_semicolon_or_sentence(text: str, max_chunk_length: int) -> list[str]:
    parts = [
        part.strip()
        for part in text.replace("|", ";").split(";")
        if part.strip()
    ]
    if not parts:
        return _chunk_by_sentence_windows(text, window_size=1, max_chunk_length=max_chunk_length)

    chunks: list[str] = []
    current = ""
    for part in parts:
        candidate = f"{current}; {part}".strip("; ").strip()
        if current and len(candidate) > max_chunk_length:
            chunks.append(current)
            current = part
        else:
            current = candidate

    if current:
        chunks.append(current)

    return chunks


def _split_sentences(text: str) -> list[str]:
    normalized = text.replace("?", ".").replace("!", ".")
    return [
        sentence.strip()
        for sentence in normalized.split(".")
        if sentence.strip()
    ]


def _generate_embedding(text: str) -> list[float]:
    buckets = [0.0] * EMBEDDING_DIMENSION
    tokens = _tokenize(text)

    for token in tokens:
        digest = hashlib.sha256(token.encode("utf-8")).digest()
        for index in range(EMBEDDING_DIMENSION):
            signed_component = 1 if digest[index] % 2 == 0 else -1
            buckets[index] += signed_component * ((digest[index] / 255.0) + 0.25)

    norm = math.sqrt(sum(value * value for value in buckets)) or 1.0
    return [round(value / norm, 6) for value in buckets]


def _tokenize(text: str) -> Iterable[str]:
    sanitized = "".join(character.lower() if character.isalnum() else " " for character in text)
    return [token for token in sanitized.split() if token]


def _build_chunk_metadata(document: VectorDocumentInput, chunk_index: int) -> dict[str, str | int | list[str] | None]:
    metadata = document.metadata
    return {
        "source_id": metadata.source_id,
        "source_type": document.source_type.value,
        "title": metadata.title,
        "category": metadata.category,
        "brand": metadata.brand,
        "nutrient_name": metadata.nutrient_name,
        "ingredient_name": metadata.ingredient_name,
        "review_count": metadata.review_count,
        "tags": metadata.tags,
        "version": metadata.version,
        "updated_at": metadata.updated_at.isoformat() if metadata.updated_at else None,
        "chunk_index": chunk_index,
    }
