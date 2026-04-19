from __future__ import annotations

from app.schemas.documents import (
    DocumentAnalysisRequest,
    DocumentAnalysisResponse,
    ImportantTermExplanation,
)


PROMPT_VERSION = "document-analysis-v1"

DISCLAIMER = (
    "This analysis is educational only. It does not diagnose conditions, confirm clinical meaning, "
    "or tell you what treatment or supplements you personally should take."
)

TERM_EXPLANATIONS: dict[str, str] = {
    "hemoglobin": "Hemoglobin is a blood-related marker commonly discussed in lab reports.",
    "vitamin d": "Vitamin D is commonly mentioned in wellness and nutrition contexts.",
    "b12": "Vitamin B12 is a nutrient often discussed in blood and nutrition assessments.",
    "protein": "Protein is commonly discussed in diet plans, recovery goals, and fitness assessments.",
    "magnesium": "Magnesium is a mineral often discussed in wellness and recovery contexts.",
    "cholesterol": "Cholesterol is a lipid-related marker commonly found in blood reports.",
    "glucose": "Glucose is a blood-sugar-related term often found in lab reports.",
    "creatinine": "Creatinine is a lab marker commonly included in blood chemistry panels.",
    "calcium": "Calcium is a mineral often discussed in nutrition and bone-health contexts.",
    "omega 3": "Omega-3 refers to fatty acids often discussed in diet and wellness contexts.",
    "whey": "Whey usually refers to a protein source commonly used in supplement products.",
    "sleep": "Sleep-related language often indicates general wellness or recovery goals rather than a diagnosis.",
    "recovery": "Recovery refers to post-exercise or general wellness support, depending on context.",
}

OBSERVATION_RULES: dict[str, str] = {
    "protein": "The document mentions protein-related content that may connect to general product categories such as protein or recovery support.",
    "vitamin d": "The document references vitamin D-related content, which may be educationally relevant to general wellness supplement browsing.",
    "magnesium": "The document mentions magnesium-related content, which may connect to general wellness or recovery-support categories.",
    "omega 3": "The document includes omega-3-related language that may be relevant to general wellness browsing categories.",
    "whey": "The document references whey or protein language that may connect to muscle-support or recovery product categories.",
    "diet plan": "The document appears to contain diet-planning content that may relate to nutrition, protein, or meal-support browsing.",
    "fitness assessment": "The document appears to discuss fitness evaluation signals that may be educationally relevant to performance or recovery product browsing.",
}


def analyze_document(request: DocumentAnalysisRequest) -> DocumentAnalysisResponse:
    normalized_text = request.extracted_text.lower()
    prompt = _build_grounded_prompt(request)
    del prompt

    detected_document_type = _detect_document_type(request, normalized_text)
    simplified_summary = _build_simplified_summary(detected_document_type, normalized_text)
    important_terms = _extract_important_terms(normalized_text)
    observations = _extract_supplement_observations(normalized_text)

    return DocumentAnalysisResponse(
        document_id=request.document_id,
        detected_document_type=detected_document_type,
        simplified_summary=simplified_summary,
        important_terms=important_terms,
        supplement_relevant_observations=observations,
        disclaimer=DISCLAIMER,
        grounding_notes=[
            "The analysis used only the extracted text provided in the request.",
            "This workflow is designed for educational explanation and category-level relevance, not diagnosis.",
            f"Prompt template: {PROMPT_VERSION}.",
        ],
        prompt_version=PROMPT_VERSION,
    )


def _build_grounded_prompt(request: DocumentAnalysisRequest) -> str:
    return (
        "You are analyzing extracted text from a user-uploaded health or fitness document.\n"
        "Use only the extracted text provided.\n"
        "Do not diagnose, confirm deficiencies, recommend treatment, or prescribe supplements.\n"
        "Explain the content in plain language and mention only general nutrition or supplement relevance.\n"
        "If the text is ambiguous, say less rather than overclaiming.\n"
        f"Document type hint: {request.document_type or 'unknown'}\n"
        f"Source format: {request.source_format or 'unknown'}\n"
        f"Extracted text: {request.extracted_text[:4000]}\n"
    )


def _detect_document_type(request: DocumentAnalysisRequest, normalized_text: str) -> str:
    if request.document_type and request.document_type.strip():
        return request.document_type.strip().upper()

    if any(token in normalized_text for token in ("hemoglobin", "glucose", "cholesterol", "creatinine")):
        return "LAB_REPORT"
    if any(token in normalized_text for token in ("meal plan", "diet plan", "calories", "macros")):
        return "DIET_PLAN"
    if any(token in normalized_text for token in ("doctor", "recommendation", "prescribed")):
        return "DOCTOR_RECOMMENDATION"
    if any(token in normalized_text for token in ("fitness assessment", "body fat", "strength test")):
        return "FITNESS_ASSESSMENT"
    if any(token in normalized_text for token in ("vitamin", "mineral", "nutrition test")):
        return "NUTRITION_REPORT"
    return "UNKNOWN_DOCUMENT"


def _build_simplified_summary(detected_document_type: str, normalized_text: str) -> str:
    summary_parts = [f"This appears to be a {detected_document_type.lower().replace('_', ' ')}."]

    if detected_document_type == "LAB_REPORT":
        summary_parts.append(
            "The extracted text looks like a report containing lab-style markers or blood-related terms."
        )
    elif detected_document_type == "DIET_PLAN":
        summary_parts.append(
            "The text appears to describe food intake, meal planning, or nutrition structure."
        )
    elif detected_document_type == "FITNESS_ASSESSMENT":
        summary_parts.append(
            "The document appears to describe fitness or body-composition-related evaluation content."
        )
    else:
        summary_parts.append(
            "The text contains health, nutrition, or fitness-related content that can be explained in plain language."
        )

    matched_terms = [term for term in TERM_EXPLANATIONS if term in normalized_text][:3]
    if matched_terms:
        summary_parts.append(
            f"Notable topics mentioned include {', '.join(matched_terms)}."
        )

    summary_parts.append(
        "This summary is intended to simplify the document, not to provide medical interpretation."
    )
    return " ".join(summary_parts)


def _extract_important_terms(normalized_text: str) -> list[ImportantTermExplanation]:
    explanations: list[ImportantTermExplanation] = []

    for term, explanation in TERM_EXPLANATIONS.items():
        if term in normalized_text:
            explanations.append(
                ImportantTermExplanation(
                    term=term,
                    explanation=explanation,
                )
            )

    return explanations[:6]


def _extract_supplement_observations(normalized_text: str) -> list[str]:
    observations: list[str] = []
    for token, observation in OBSERVATION_RULES.items():
        if token in normalized_text:
            observations.append(observation)

    if not observations:
        observations.append(
            "The extracted text does not provide enough grounded evidence for a strong nutrition or supplement observation."
        )

    observations.append(
        "Any product relevance here should be treated as category-level educational guidance, not personal medical advice."
    )
    return observations[:5]
