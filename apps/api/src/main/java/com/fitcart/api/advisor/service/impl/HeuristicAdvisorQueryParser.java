package com.fitcart.api.advisor.service.impl;

import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;
import com.fitcart.api.advisor.service.AdvisorQueryParser;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HeuristicAdvisorQueryParser implements AdvisorQueryParser {

    private static final Pattern UNDER_BUDGET_PATTERN = Pattern.compile("\\bunder\\s+(\\d+(?:\\.\\d+)?)\\b");
    private static final Pattern MAX_BUDGET_PATTERN = Pattern.compile("\\b(?:below|max)\\s+(\\d+(?:\\.\\d+)?)\\b");

    @Override
    public ParsedAdvisorIntentResponse parse(String query) {
        String normalizedQuery = normalize(query);
        Set<String> preferenceSignals = new LinkedHashSet<>();
        Set<String> requiredDietaryFlags = new LinkedHashSet<>();

        String categoryHint = extractCategoryHint(normalizedQuery);
        String goal = extractGoal(normalizedQuery);
        BigDecimal budget = extractBudget(normalizedQuery);
        BigDecimal maxSugarGrams = extractSugarConstraint(normalizedQuery, preferenceSignals);

        if (normalizedQuery.contains("high protein")) {
            requiredDietaryFlags.add("high protein");
            preferenceSignals.add("high protein");
        }
        if (normalizedQuery.contains("beginner")) {
            preferenceSignals.add("beginner-friendly");
        }
        if (normalizedQuery.contains("tasty") || normalizedQuery.contains("flavor")) {
            preferenceSignals.add("taste");
        }
        if (goal != null && !goal.isBlank()) {
            preferenceSignals.add(goal);
        }
        if (categoryHint != null && !categoryHint.isBlank()) {
            preferenceSignals.add(categoryHint);
        }

        return new ParsedAdvisorIntentResponse(
                normalizedQuery,
                categoryHint,
                goal,
                budget,
                maxSugarGrams,
                preferenceSignals,
                requiredDietaryFlags
        );
    }

    private String extractCategoryHint(String normalizedQuery) {
        if (normalizedQuery.contains("whey")) {
            return "whey";
        }
        if (normalizedQuery.contains("creatine")) {
            return "creatine";
        }
        if (normalizedQuery.contains("magnesium")) {
            return "magnesium";
        }
        if (normalizedQuery.contains("omega")) {
            return "omega";
        }
        if (normalizedQuery.contains("protein")) {
            return "protein";
        }
        return null;
    }

    private String extractGoal(String normalizedQuery) {
        if (normalizedQuery.contains("muscle gain") || normalizedQuery.contains("build muscle")) {
            return "muscle gain";
        }
        if (normalizedQuery.contains("cutting") || normalizedQuery.contains("fat loss")) {
            return "cutting";
        }
        if (normalizedQuery.contains("sleep")) {
            return "sleep support";
        }
        if (normalizedQuery.contains("recovery")) {
            return "recovery";
        }
        if (normalizedQuery.contains("energy")) {
            return "energy";
        }
        return null;
    }

    private BigDecimal extractBudget(String normalizedQuery) {
        for (Pattern pattern : new Pattern[]{UNDER_BUDGET_PATTERN, MAX_BUDGET_PATTERN}) {
            Matcher matcher = pattern.matcher(normalizedQuery);
            if (matcher.find()) {
                return new BigDecimal(matcher.group(1));
            }
        }
        return null;
    }

    private BigDecimal extractSugarConstraint(String normalizedQuery, Set<String> preferenceSignals) {
        if (normalizedQuery.contains("low sugar")) {
            preferenceSignals.add("low sugar");
            return BigDecimal.valueOf(4);
        }
        if (normalizedQuery.contains("no sugar") || normalizedQuery.contains("zero sugar")) {
            preferenceSignals.add("zero sugar");
            return BigDecimal.ZERO;
        }
        return null;
    }

    private String normalize(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }
}
