from __future__ import annotations

import json
import time
from dataclasses import dataclass
from threading import Lock
from typing import TypeVar

from pydantic import BaseModel

from app.core.config import get_settings

try:
    from redis import Redis
    from redis.exceptions import RedisError
except ImportError:  # pragma: no cover
    Redis = None

    class RedisError(Exception):
        pass


ModelT = TypeVar("ModelT", bound=BaseModel)


@dataclass
class _InMemoryEntry:
    payload: str
    expires_at: float


class ResponseCacheRepository:
    def __init__(self) -> None:
        settings = get_settings()
        self._enabled = settings.cache_enabled
        self._memory_cache: dict[str, _InMemoryEntry] = {}
        self._lock = Lock()
        self._redis_client = self._build_redis_client(settings.redis_url) if self._enabled else None

    def get_model(self, key: str, model_type: type[ModelT]) -> ModelT | None:
        if not self._enabled:
            return None

        payload = self._get_payload(key)
        if payload is None:
            return None

        return model_type.model_validate_json(payload)

    def set_model(self, key: str, value: BaseModel, ttl_seconds: int) -> None:
        if not self._enabled or ttl_seconds <= 0:
            return

        payload = value.model_dump_json()
        if self._redis_client is not None:
            try:
                self._redis_client.set(name=key, value=payload, ex=ttl_seconds)
                return
            except RedisError:
                pass

        with self._lock:
            self._memory_cache[key] = _InMemoryEntry(
                payload=payload,
                expires_at=time.time() + ttl_seconds,
            )

    def _get_payload(self, key: str) -> str | None:
        if self._redis_client is not None:
            try:
                payload = self._redis_client.get(key)
                if payload is None:
                    return None
                return payload.decode("utf-8") if isinstance(payload, bytes) else str(payload)
            except RedisError:
                pass

        with self._lock:
            entry = self._memory_cache.get(key)
            if entry is None:
                return None
            if entry.expires_at <= time.time():
                self._memory_cache.pop(key, None)
                return None
            return entry.payload

    def _build_redis_client(self, redis_url: str):
        if Redis is None:
            return None

        try:
            client = Redis.from_url(redis_url, decode_responses=False)
            client.ping()
            return client
        except RedisError:
            return None


_response_cache_repository: ResponseCacheRepository | None = None


def get_response_cache_repository() -> ResponseCacheRepository:
    global _response_cache_repository

    if _response_cache_repository is None:
        _response_cache_repository = ResponseCacheRepository()

    return _response_cache_repository
