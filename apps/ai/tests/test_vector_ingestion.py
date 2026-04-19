from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_vector_ingestion_endpoint_returns_chunked_result() -> None:
    response = client.post(
        "/api/v1/vectors/ingest",
        json={
            "replace_existing": True,
            "documents": [
                {
                    "source_type": "product_description",
                    "metadata": {
                        "source_id": "product-101",
                        "title": "FitCart Whey Isolate",
                        "category": "Protein Powder",
                        "brand": "FitCart",
                        "tags": ["protein", "recovery"],
                        "version": 2
                    },
                    "text": (
                        "FitCart Whey Isolate is designed for post-workout recovery. "
                        "It delivers a lean protein profile with low sugar. "
                        "Users often choose it for muscle-support goals and daily convenience."
                    )
                }
            ]
        },
    )

    assert response.status_code == 200

    payload = response.json()
    assert payload["total_chunks_written"] >= 1
    assert payload["vector_dimension"] == 24
    assert payload["ingested_documents"][0]["source_id"] == "product-101"
