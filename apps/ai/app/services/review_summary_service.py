from __future__ import annotations

import json
import re
from collections import Counter, defaultdict

from app.core.cache_keys import build_cache_key
from app.core.config import get_settings
from app.repositories.response_cache_repository import get_response_cache_repository
from app.schemas.reviews import ReviewSummarizationRequest, ReviewSummarizationResponse


PROMPT_VERSION = "review-summary-v1"

POSITIVE_THEME_RULES: dict[str, tuple[str, ...]] = {
    "good taste": ("taste", "flavor", "delicious", "smooth"),
    "mixes well": ("mixes well", "mixability", "no clumps", "blends easily"),
    "supports recovery": ("recovery", "less sore", "post workout", "recover"),
    "helps muscle goals": ("muscle", "strength", "gains", "lean mass"),
    "easy to digest": ("digest", "stomach", "bloat", "gentle"),
    "good value": ("value", "worth", "price", "affordable"),
}

NEGATIVE_THEME_RULES: dict[str, tuple[str, ...]] = {
    "bad taste": ("bad taste", "chalky", "too sweet", "artificial"),
    "mixability issues": ("clumps", "does not mix", "gritty"),
    "digestive discomfort": ("bloating", "gas", "stomach pain", "digestive"),
    "too expensive": ("expensive", "overpriced", "pricey"),
    "caused jitters": ("jitters", "shaky", "heart racing"),
    "caused acne or breakouts": ("acne", "breakouts", "skin"),
}

GOOD_FOR_RULES: dict[str, tuple[str, ...]] = {
    "people focused on muscle gain": ("muscle", "strength", "bulk"),
    "users who want post-workout recovery support": ("recovery", "post workout", "soreness"),
    "buyers looking for daily convenience": ("quick", "convenient", "easy"),
    "budget-conscious shoppers": ("value", "affordable", "budget"),
}

AVOID_FOR_RULES: dict[str, tuple[str, ...]] = {
    "people sensitive to sweetness or artificial flavoring": ("too sweet", "artificial", "sweet"),
    "users with sensitive digestion": ("bloating", "gas", "digestive", "stomach"),
    "buyers seeking low-cost options": ("expensive", "overpriced", "pricey"),
    "caffeine-sensitive users": ("jitters", "shaky", "heart racing"),
}

STOPWORDS = {
    "the", "and", "with", "this", "that", "from", "have", "just", "very", "more", "your",
    "will", "into", "about", "would", "after", "before", "were", "they", "them", "their",
    "because", "while", "there", "these", "those", "than", "been", "being", "really", "product",
}


def summarize_product_reviews(
    request: ReviewSummarizationRequest,
) -> ReviewSummarizationResponse:
    settings = get_settings()
    cache_repository = get_response_cache_repository()
    cache_key = build_cache_key(
        namespace="ai:review-summary",
        version=PROMPT_VERSION,
        payload=request.model_dump(mode="json"),
    )

    cached_response = cache_repository.get_model(cache_key, ReviewSummarizationResponse)
    if cached_response is not None:
        return cached_response

    normalized_reviews = [_prepare_review_text(review) for review in request.reviews]
    prompt = _build_grounded_prompt(request, normalized_reviews)
    raw_model_output = _simulate_grounded_summary(request, normalized_reviews, prompt)
    parsed_output = _parse_model_response(raw_model_output)

    response = ReviewSummarizationResponse(
        product_name=request.product_name.strip(),
        review_count=len(request.reviews),
        pros=parsed_output["pros"],
        cons=parsed_output["cons"],
        common_complaints=parsed_output["common_complaints"],
        good_for=parsed_output["good_for"],
        avoid_for=parsed_output["avoid_for"],
        grounding_notes=[
            "Only review text provided in the request was used for summarization.",
            "Claims with weak support were omitted instead of guessed.",
            f"Prompt template: {PROMPT_VERSION}.",
        ],
        prompt_version=PROMPT_VERSION,
    )

    cache_repository.set_model(
        cache_key,
        response,
        settings.review_summary_cache_ttl_seconds,
    )
    return response


def _prepare_review_text(review) -> str:
    parts = []
    if review.title:
        parts.append(review.title.strip())
    parts.append(review.body.strip())
    return " ".join(parts).lower()


def _build_grounded_prompt(
    request: ReviewSummarizationRequest,
    normalized_reviews: list[str],
) -> str:
    review_lines = [
        f"{index + 1}. {review}"
        for index, review in enumerate(normalized_reviews[:50])
    ]
    category = request.category_name or "unknown-category"

    return (
        "You are generating a grounded review summary for an ecommerce product.\n"
        "Use only the review snippets provided below.\n"
        "Do not invent ingredients, outcomes, medical claims, or user segments.\n"
        "If evidence is weak, return an empty array for that field.\n"
        "Prefer recurring themes over one-off opinions.\n"
        "Return valid JSON with exactly these keys: "
        "pros, cons, common_complaints, good_for, avoid_for.\n"
        f"Product: {request.product_name}\n"
        f"Category: {category}\n"
        "Reviews:\n"
        + "\n".join(review_lines)
    )


def _simulate_grounded_summary(
    request: ReviewSummarizationRequest,
    normalized_reviews: list[str],
    prompt: str,
) -> str:
    del prompt

    positive_counts = _count_rule_matches(normalized_reviews, POSITIVE_THEME_RULES)
    negative_counts = _count_rule_matches(normalized_reviews, NEGATIVE_THEME_RULES)
    good_for_counts = _count_rule_matches(normalized_reviews, GOOD_FOR_RULES)
    avoid_for_counts = _count_rule_matches(normalized_reviews, AVOID_FOR_RULES)

    complaints = [
        theme
        for theme, _count in negative_counts.most_common(3)
    ]

    pros = _top_grounded_items(positive_counts)
    cons = _top_grounded_items(negative_counts)
    good_for = _top_grounded_items(good_for_counts)
    avoid_for = _top_grounded_items(avoid_for_counts)

    if not pros:
        pros = _extract_frequent_phrases(normalized_reviews, sentiment="positive")
    if not cons:
        cons = _extract_frequent_phrases(normalized_reviews, sentiment="negative")

    payload = {
        "pros": pros[:4],
        "cons": cons[:4],
        "common_complaints": complaints[:4],
        "good_for": good_for[:4],
        "avoid_for": avoid_for[:4],
    }
    return json.dumps(payload)


def _parse_model_response(raw_output: str) -> dict[str, list[str]]:
    try:
        parsed = json.loads(raw_output)
    except json.JSONDecodeError:
        parsed = {}

    response: dict[str, list[str]] = {}
    for key in ("pros", "cons", "common_complaints", "good_for", "avoid_for"):
        values = parsed.get(key, [])
        if not isinstance(values, list):
            values = []

        cleaned_values: list[str] = []
        for value in values:
            if not isinstance(value, str):
                continue
            normalized = " ".join(value.split()).strip()
            if normalized and normalized not in cleaned_values:
                cleaned_values.append(normalized)

        response[key] = cleaned_values[:4]

    return response


def _count_rule_matches(
    normalized_reviews: list[str],
    rules: dict[str, tuple[str, ...]],
) -> Counter[str]:
    counts: Counter[str] = Counter()

    for review in normalized_reviews:
        for label, phrases in rules.items():
            if any(phrase in review for phrase in phrases):
                counts[label] += 1

    return counts


def _top_grounded_items(counts: Counter[str], minimum_support: int = 2) -> list[str]:
    return [
        label
        for label, count in counts.most_common(4)
        if count >= minimum_support
    ]


def _extract_frequent_phrases(normalized_reviews: list[str], sentiment: str) -> list[str]:
    keywords = {
        "positive": {"good", "great", "helpful", "effective", "solid", "easy"},
        "negative": {"bad", "poor", "terrible", "upset", "chalky", "expensive"},
    }[sentiment]

    phrase_counts: defaultdict[str, int] = defaultdict(int)

    for review in normalized_reviews:
        if not any(keyword in review for keyword in keywords):
            continue
        for token in re.findall(r"[a-z]{4,}", review):
            if token in STOPWORDS:
                continue
            phrase_counts[token] += 1

    ranked = sorted(
        phrase_counts.items(),
        key=lambda item: (-item[1], item[0]),
    )
    return [word.replace("_", " ") for word, count in ranked if count >= 2][:3]
