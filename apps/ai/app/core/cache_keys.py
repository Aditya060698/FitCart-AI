from __future__ import annotations

import hashlib
import json


def build_cache_key(namespace: str, version: str, payload: dict) -> str:
    canonical_payload = json.dumps(payload, sort_keys=True, separators=(",", ":"))
    digest = hashlib.sha256(canonical_payload.encode("utf-8")).hexdigest()
    return f"fitcart:{namespace}:{version}:{digest}"
