package com.fitcart.api.ranking.util;

public final class RankingScoreUtils {

    private RankingScoreUtils() {
    }

    public static double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }

    public static double round(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }
}
