import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { fetchAdvisorRecommendation } from "../../../lib/api/advisor";
import { getApiErrorMessage } from "../../../lib/api/http";
import {
  fetchUserProfile,
  updateUserProfile,
} from "../../../lib/api/personalization";
import { demoUserReference } from "../../../lib/personalization/user";

export function AdvisorPromptCard() {
  const queryClient = useQueryClient();
  const [query, setQuery] = useState("Best whey under 2500 for muscle gain with low sugar");
  const [minBudget, setMinBudget] = useState("");
  const [maxBudget, setMaxBudget] = useState("2500");
  const [primaryGoal, setPrimaryGoal] = useState("muscle gain");
  const [dietaryPreferences, setDietaryPreferences] = useState("high protein, low sugar");

  const { data: profile } = useQuery({
    queryKey: ["user-profile", demoUserReference],
    queryFn: () => fetchUserProfile(demoUserReference),
  });

  const profileMutation = useMutation({
    mutationFn: (payload) => updateUserProfile(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-profile", demoUserReference] });
    },
  });

  const advisorMutation = useMutation({
    mutationFn: fetchAdvisorRecommendation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["search-history", demoUserReference] });
    },
  });

  const handleProfileSave = () => {
    profileMutation.mutate({
      userReference: demoUserReference,
      minBudget: minBudget ? Number(minBudget) : null,
      maxBudget: maxBudget ? Number(maxBudget) : null,
      primaryGoal,
      dietaryPreferences: dietaryPreferences
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean),
    });
  };

  const handleAnalyze = () => {
    advisorMutation.mutate({
      query,
      topK: 3,
      userReference: demoUserReference,
    });
  };

  const recommendation = advisorMutation.data;
  const advisorErrorMessage = advisorMutation.isError
    ? getApiErrorMessage(advisorMutation.error, "Recommendation is temporarily unavailable.")
    : null;

  return (
    <section className="panel-card">
      <h2 className="text-lg font-semibold text-ink">Goal-first recommendation flow</h2>
      <p className="mt-2 text-sm leading-7 text-slate-600">
        Personalization stays lightweight here: user profile, saved products, and recent queries boost ranking, but the current prompt still leads.
      </p>

      <div className="mt-5 grid gap-3 sm:grid-cols-2">
        <label className="text-sm text-slate-600">
          Min budget
          <input
            value={minBudget}
            onChange={(event) => setMinBudget(event.target.value)}
            placeholder={profile?.minBudget ?? "Optional"}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none"
          />
        </label>
        <label className="text-sm text-slate-600">
          Max budget
          <input
            value={maxBudget}
            onChange={(event) => setMaxBudget(event.target.value)}
            placeholder={profile?.maxBudget ?? "Optional"}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none"
          />
        </label>
        <label className="text-sm text-slate-600">
          Primary goal
          <input
            value={primaryGoal}
            onChange={(event) => setPrimaryGoal(event.target.value)}
            placeholder={profile?.primaryGoal ?? "muscle gain"}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none"
          />
        </label>
        <label className="text-sm text-slate-600">
          Dietary preferences
          <input
            value={dietaryPreferences}
            onChange={(event) => setDietaryPreferences(event.target.value)}
            placeholder={(profile?.dietaryPreferences || []).join(", ")}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none"
          />
        </label>
      </div>

      <div className="mt-4 flex gap-3">
        <button
          type="button"
          onClick={handleProfileSave}
          disabled={profileMutation.isPending}
          className="rounded-full border border-slate-300 px-5 py-3 text-sm font-medium text-slate-700"
        >
          Save profile
        </button>
      </div>

      <textarea
        rows="5"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        placeholder="I want vegetarian protein under 2500 for muscle gain..."
        className="mt-4 w-full rounded-3xl border border-slate-200 bg-white px-4 py-4 text-sm text-slate-700 outline-none"
      />

      <div className="mt-4 flex flex-wrap gap-3">
        <button
          type="button"
          onClick={handleAnalyze}
          disabled={advisorMutation.isPending}
          className="rounded-full bg-ink px-5 py-3 text-sm font-medium text-white"
        >
          {advisorMutation.isPending ? "Analyzing..." : "Analyze request"}
        </button>
        <button
          type="button"
          onClick={() => setQuery("")}
          className="rounded-full border border-slate-300 px-5 py-3 text-sm font-medium text-slate-600"
        >
          Reset
        </button>
      </div>

      {advisorErrorMessage ? (
        <div className="mt-4 rounded-3xl border border-amber-200 bg-amber-50 px-4 py-4">
          <p className="text-sm font-semibold text-amber-900">Advisor temporarily unavailable</p>
          <p className="mt-2 text-sm leading-7 text-amber-800">{advisorErrorMessage}</p>
          <p className="mt-2 text-sm leading-7 text-amber-800">
            Product listing, filters, and comparison still work while the AI layer recovers.
          </p>
        </div>
      ) : null}

      {recommendation ? (
        <div className="mt-6 space-y-4">
          {recommendation.degraded ? (
            <div className="rounded-3xl border border-amber-200 bg-amber-50 px-4 py-4">
              <p className="text-sm font-semibold text-amber-900">Partial fallback active</p>
              <p className="mt-2 text-sm leading-7 text-amber-800">
                {recommendation.notice || "AI reasoning degraded, so FitCart AI is showing structured catalog results instead."}
              </p>
            </div>
          ) : null}

          <div className="rounded-3xl bg-mist px-4 py-4">
            <h3 className="text-sm font-semibold uppercase tracking-[0.16em] text-pine">Grounded answer</h3>
            <p className="mt-2 text-sm leading-7 text-slate-700">{recommendation.answer}</p>
          </div>

          <div className="space-y-3">
            {recommendation.recommendations.map((item) => (
              <article key={item.productId} className="rounded-3xl border border-slate-200 px-4 py-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.16em] text-pine">{item.categoryName}</p>
                    <h3 className="mt-1 text-base font-semibold text-ink">{item.name}</h3>
                    <p className="mt-1 text-sm text-slate-500">{item.brandName}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs uppercase tracking-[0.16em] text-slate-500">Score</p>
                    <p className="text-base font-semibold text-ink">{item.finalScore}</p>
                  </div>
                </div>
                <p className="mt-3 text-sm text-slate-600">{item.reviewSummary}</p>
              </article>
            ))}
          </div>
        </div>
      ) : null}
    </section>
  );
}
