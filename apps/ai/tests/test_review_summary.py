from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_review_summary_endpoint_returns_grounded_shape() -> None:
    response = client.post(
        "/api/v1/reviews/summarize",
        json={
            "product_name": "FitCart Whey Protein",
            "category_name": "Protein Powder",
            "reviews": [
                {
                    "title": "Great recovery shake",
                    "body": "Great flavor and mixes well. Helped my recovery after workouts.",
                    "rating": 5,
                    "verified_purchase": True,
                },
                {
                    "title": "Solid but too sweet",
                    "body": "Mixes easily and works well for muscle recovery, but it tastes too sweet.",
                    "rating": 4,
                    "verified_purchase": True,
                },
                {
                    "title": "Upset my stomach",
                    "body": "The protein quality seems fine but I had bloating and stomach discomfort.",
                    "rating": 2,
                    "verified_purchase": False,
                },
                {
                    "title": "Good value",
                    "body": "Affordable for the amount you get and the taste is good.",
                    "rating": 4,
                    "verified_purchase": True,
                },
            ],
        },
    )

    assert response.status_code == 200

    payload = response.json()
    assert payload["product_name"] == "FitCart Whey Protein"
    assert payload["review_count"] == 4
    assert "mixes well" in payload["pros"]
    assert "digestive discomfort" in payload["cons"]
    assert payload["prompt_version"] == "review-summary-v1"
