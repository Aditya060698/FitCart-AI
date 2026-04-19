from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_semantic_search_returns_ranked_documents() -> None:
    client.post(
        "/api/v1/vectors/ingest",
        json={
            "replace_existing": True,
            "documents": [
                {
                    "source_type": "product_description",
                    "metadata": {
                        "source_id": "protein-1",
                        "title": "Lean Whey Isolate",
                        "category": "Protein Powder",
                        "brand": "FitCart",
                        "tags": ["protein", "low sugar", "cutting"],
                        "version": 1
                    },
                    "text": "Lean Whey Isolate is a low sugar protein designed for cutting and post-workout recovery."
                },
                {
                    "source_type": "product_description",
                    "metadata": {
                        "source_id": "creatine-1",
                        "title": "Starter Creatine",
                        "category": "Creatine",
                        "brand": "FitCart",
                        "tags": ["creatine", "beginner"],
                        "version": 1
                    },
                    "text": "Starter Creatine is a simple beginner creatine product for strength and muscle-support goals."
                }
            ]
        },
    )

    response = client.post(
        "/api/v1/search/semantic",
        json={
            "query": "low sugar protein for cutting",
            "top_k": 2
        },
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["results"][0]["source_id"] == "protein-1"
    assert payload["results"][0]["score"] > 0
    assert payload["total_candidates_considered"] >= 2
