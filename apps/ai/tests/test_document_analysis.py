from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_document_analysis_returns_grounded_document_response() -> None:
    response = client.post(
        "/api/v1/documents/analyze",
        json={
            "document_id": "doc-101",
            "source_format": "pdf",
            "extracted_text": (
                "Blood report summary: Hemoglobin is listed along with vitamin D and glucose values. "
                "The report also mentions magnesium and general wellness follow-up. "
                "This is a consumer-facing lab report for discussion with a professional."
            ),
        },
    )

    assert response.status_code == 200

    payload = response.json()
    assert payload["document_id"] == "doc-101"
    assert payload["detected_document_type"] == "LAB_REPORT"
    assert "educational only" in payload["disclaimer"].lower()
    assert payload["important_terms"]
    assert payload["prompt_version"] == "document-analysis-v1"
