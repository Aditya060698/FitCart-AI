from app.schemas.recommendations import (
    RecommendationAnalysisRequest,
    RecommendationAnalysisResponse,
)


def analyze_recommendation_request(
        request: RecommendationAnalysisRequest,
) -> RecommendationAnalysisResponse:
    constraints: list[str] = []

    if request.budget is not None:
        constraints.append(f"budget<={request.budget}")
    if request.dietary_preferences:
        constraints.append(f"dietary_preferences={','.join(request.dietary_preferences)}")
    if request.exclusions:
        constraints.append(f"exclusions={','.join(request.exclusions)}")

    suggested_categories = _map_goal_to_categories(request.goal)

    return RecommendationAnalysisResponse(
        interpreted_goal=request.goal.strip().lower(),
        suggested_categories=suggested_categories,
        applied_constraints=constraints,
        reasoning_summary=(
            "This placeholder analysis translates user intent into category-level guidance. "
            "Later it will support Spring Boot by interpreting natural-language requests, "
            "running retrieval over FitCart knowledge, and returning structured AI analysis."
        ),
        confidence="medium",
    )


def _map_goal_to_categories(goal: str) -> list[str]:
    normalized_goal = goal.strip().lower()

    if "muscle" in normalized_goal or "strength" in normalized_goal:
        return ["protein", "creatine"]
    if "recovery" in normalized_goal:
        return ["protein", "electrolytes"]
    if "energy" in normalized_goal:
        return ["pre-workout", "electrolytes"]

    return ["multivitamins", "protein"]
