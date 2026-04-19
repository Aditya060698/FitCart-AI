from __future__ import annotations

import json
from pathlib import Path

from app.schemas.vectors import VectorChunkRecord, VectorSourceType


class FileVectorStoreRepository:
    def __init__(self, storage_path: str) -> None:
        self._storage_path = Path(storage_path)
        self._storage_path.parent.mkdir(parents=True, exist_ok=True)

    def replace_document_chunks(
        self,
        source_type: VectorSourceType,
        source_id: str,
        chunks: list[VectorChunkRecord],
    ) -> None:
        existing_chunks = self._read_all_chunks()
        retained_chunks = [
            chunk
            for chunk in existing_chunks
            if not (
                chunk.source_type == source_type
                and chunk.source_id == source_id
            )
        ]
        retained_chunks.extend(chunks)
        self._write_all_chunks(retained_chunks)

    def append_document_chunks(self, chunks: list[VectorChunkRecord]) -> None:
        existing_chunks = self._read_all_chunks()
        existing_chunks.extend(chunks)
        self._write_all_chunks(existing_chunks)

    def read_all_chunks(self) -> list[VectorChunkRecord]:
        return self._read_all_chunks()

    def _read_all_chunks(self) -> list[VectorChunkRecord]:
        if not self._storage_path.exists():
            return []

        payload = json.loads(self._storage_path.read_text(encoding="utf-8"))
        return [VectorChunkRecord.model_validate(item) for item in payload]

    def _write_all_chunks(self, chunks: list[VectorChunkRecord]) -> None:
        payload = [chunk.model_dump(mode="json") for chunk in chunks]
        self._storage_path.write_text(
            json.dumps(payload, indent=2),
            encoding="utf-8",
        )
