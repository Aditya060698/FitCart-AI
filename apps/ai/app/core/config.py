from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "FitCart AI Service"
    app_env: str = "local"
    app_host: str = "0.0.0.0"
    app_port: int = 8000
    app_debug: bool = True

    api_prefix: str = "/api/v1"
    spring_api_base_url: str = "http://localhost:8080"

    openai_api_key: str = "replace_me"
    vector_store: str = "pgvector"
    vector_store_file_path: str = "data/vector_store.json"
    cache_enabled: bool = True
    redis_url: str = "redis://localhost:6379/0"
    nutrient_explanation_cache_ttl_seconds: int = 21600
    review_summary_cache_ttl_seconds: int = 3600

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
