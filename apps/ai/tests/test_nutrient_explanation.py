from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_nutrient_explanation_endpoint_returns_grounded_educational_shape() -> None:
    response = client.post(
        "/api/v1/nutrients/explain",
        json={
            "nutrient_name": "Magnesium Glycinate",
            "user_question": "Benefits of magnesium glycinate?",
            "user_goal": "recovery support",
            "metadata": {
                "nutrient_type": "mineral form",
                "aliases": ["magnesium bisglycinate"],
                "short_description": "Magnesium glycinate is a chelated form of magnesium commonly used in supplement products.",
                "possible_benefits": [
                    "muscle function support",
                    "nerve function support",
                    "general wellness support"
                ],
                "product_categories": ["minerals", "sleep support", "recovery"],
                "warnings": [
                    "Some users may still want to review tolerance and medication context with a professional."
                ],
                "beginner_relevance": "Beginners often encounter it as a general wellness or recovery-support ingredient."
            }
        },
    )

    assert response.status_code == 200

    payload = response.json()
    assert payload["nutrient_name"] == "Magnesium Glycinate"
    assert payload["possible_benefits"][0] == "muscle function support"
    assert payload["commonly_found_in"][0] == "minerals"
    assert "educational only" in payload["disclaimer"].lower()
    assert payload["prompt_version"] == "nutrient-explainer-v2"
