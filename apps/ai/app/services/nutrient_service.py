from __future__ import annotations

from app.core.cache_keys import build_cache_key
from app.core.config import get_settings
from app.repositories.response_cache_repository import get_response_cache_repository
from app.schemas.nutrients import (
    NutrientExplanationRequest,
    NutrientExplanationResponse,
)


PROMPT_VERSION = "nutrient-explainer-v2"

DISCLAIMER = (
    "This explanation is educational only. It does not diagnose conditions, "
    "replace medical advice, or tell you what you personally should take."
)


def explain_nutrient(request: NutrientExplanationRequest) -> NutrientExplanationResponse:
    settings = get_settings()
    cache_repository = get_response_cache_repository()
    cache_key = build_cache_key(
        namespace="ai:nutrient-explanation",
        version=PROMPT_VERSION,
        payload=request.model_dump(mode="json"),
    )

    cached_response = cache_repository.get_model(cache_key, NutrientExplanationResponse)
    if cached_response is not None:
        return cached_response

    nutrient = request.nutrient_name.strip()
    prompt = _build_grounded_prompt(request)
    del prompt

    response = NutrientExplanationResponse(
        nutrient_name=nutrient,
        answer_title=_build_answer_title(request.user_question, nutrient),
        direct_answer=_build_direct_answer(request),
        what_it_is=_build_what_it_is(request),
        possible_benefits=request.metadata.possible_benefits[:5],
        commonly_found_in=request.metadata.product_categories[:5],
        considerations=_build_considerations(request),
        beginner_note=_build_beginner_note(request),
        disclaimer=DISCLAIMER,
        grounding_notes=[
            "Answer grounded only in nutrient metadata provided by the calling service.",
            "No diagnosis, deficiency assessment, or dosage recommendation was performed.",
            f"Prompt template: {PROMPT_VERSION}.",
        ],
        prompt_version=PROMPT_VERSION,
    )

    cache_repository.set_model(
        cache_key,
        response,
        settings.nutrient_explanation_cache_ttl_seconds,
    )
    return response


def _build_grounded_prompt(request: NutrientExplanationRequest) -> str:
    metadata = request.metadata

    return (
        "You are an educational nutrition assistant for an ecommerce platform.\n"
        "Use only the structured nutrient metadata provided below.\n"
        "Do not infer diseases, deficiencies, personal suitability, or dosage.\n"
        "Do not use prescriptive language such as 'you should take'.\n"
        "If the metadata is missing a fact, say less rather than inventing details.\n"
        "Respond in a grounded, beginner-friendly way.\n"
        f"User question: {request.user_question}\n"
        f"Nutrient: {request.nutrient_name}\n"
        f"Nutrient type: {metadata.nutrient_type or 'unknown'}\n"
        f"Aliases: {', '.join(metadata.aliases) or 'none'}\n"
        f"Description: {metadata.short_description or 'not provided'}\n"
        f"Possible benefits: {', '.join(metadata.possible_benefits) or 'none'}\n"
        f"Product categories: {', '.join(metadata.product_categories) or 'none'}\n"
        f"Warnings: {', '.join(metadata.warnings) or 'none'}\n"
        f"Beginner relevance: {metadata.beginner_relevance or 'not provided'}\n"
        f"User goal: {request.user_goal or 'not provided'}\n"
        f"Context: {request.context or 'not provided'}\n"
    )


def _build_answer_title(user_question: str, nutrient_name: str) -> str:
    normalized_question = user_question.strip().lower()

    if normalized_question.startswith("what is"):
        return f"What Is {nutrient_name}?"
    if "benefit" in normalized_question or "help with" in normalized_question:
        return f"Potential Benefits of {nutrient_name}"
    if "beginner" in normalized_question:
        return f"{nutrient_name} for Beginners"
    return f"{nutrient_name} Overview"


def _build_direct_answer(request: NutrientExplanationRequest) -> str:
    metadata = request.metadata
    nutrient = request.nutrient_name.strip()
    description = metadata.short_description or f"{nutrient} is a supplement-related ingredient."

    if _question_mentions_beginner_usefulness(request.user_question):
        if metadata.beginner_relevance:
            return (
                f"{nutrient} can be relevant for beginners depending on their goals. "
                f"{metadata.beginner_relevance}"
            )
        return (
            f"{nutrient} may be relevant for beginners when it matches their goals, "
            "but this answer is educational rather than personal advice."
        )

    if _question_mentions_benefits(request.user_question):
        if metadata.possible_benefits:
            benefit_preview = ", ".join(metadata.possible_benefits[:3])
            return (
                f"{nutrient} is commonly associated with {benefit_preview}. "
                "These are educational benefit associations, not guaranteed outcomes."
            )
        return (
            f"{nutrient} may have category-level relevance, but the provided metadata does not list "
            "specific benefit associations."
        )

    return description


def _build_what_it_is(request: NutrientExplanationRequest) -> str:
    metadata = request.metadata
    nutrient = request.nutrient_name.strip()

    parts: list[str] = []
    if metadata.nutrient_type:
        parts.append(f"{nutrient} is categorized here as a {metadata.nutrient_type}.")
    if metadata.short_description:
        parts.append(metadata.short_description)
    if metadata.aliases:
        parts.append(f"It may also appear under names such as {', '.join(metadata.aliases[:3])}.")

    if not parts:
        parts.append(
            f"{nutrient} is treated as a supplement-related nutrient or ingredient in FitCart AI."
        )

    return " ".join(parts)


def _build_considerations(request: NutrientExplanationRequest) -> list[str]:
    considerations = list(request.metadata.warnings[:4])

    if not considerations:
        considerations.append(
            "Personal suitability can depend on health context, medications, and sensitivity."
        )

    considerations.append(
        "This response does not evaluate whether the ingredient is appropriate for your medical needs."
    )
    return considerations[:5]


def _build_beginner_note(request: NutrientExplanationRequest) -> str:
    if request.metadata.beginner_relevance:
        return request.metadata.beginner_relevance

    if request.metadata.product_categories:
        return (
            f"For beginners, {request.nutrient_name.strip()} is usually understood in the context of "
            f"products such as {', '.join(request.metadata.product_categories[:2])}."
        )

    return (
        f"For beginners, {request.nutrient_name.strip()} should be understood as a general educational topic, "
        "not as a personal recommendation."
    )


def _question_mentions_benefits(user_question: str) -> bool:
    normalized_question = user_question.strip().lower()
    return "benefit" in normalized_question or "help with" in normalized_question


def _question_mentions_beginner_usefulness(user_question: str) -> bool:
    normalized_question = user_question.strip().lower()
    return "beginner" in normalized_question or "useful" in normalized_question
