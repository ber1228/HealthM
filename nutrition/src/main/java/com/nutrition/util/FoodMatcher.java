package com.nutrition.util;

import com.nutrition.entity.Food;

import java.util.List;

public class FoodMatcher {

    private static final double THRESHOLD = 0.5;

    /**
     * Match an AI-identified food name against the foods table.
     * Returns the best matching Food entity or null if no match above threshold.
     */
    public static Food findBestMatch(String aiFoodName, List<Food> allFoods) {
        if (aiFoodName == null || aiFoodName.trim().isEmpty() || allFoods == null || allFoods.isEmpty()) {
            return null;
        }

        String normalized = aiFoodName.trim();
        Food bestMatch = null;
        double bestScore = 0;

        for (Food food : allFoods) {
            double score = similarity(normalized, food.getName());
            if (score > bestScore) {
                bestScore = score;
                bestMatch = food;
            }
        }

        return bestScore >= THRESHOLD ? bestMatch : null;
    }

    /**
     * Calculate string similarity (0.0 to 1.0) between two Chinese food names.
     * Uses containment check + Levenshtein distance.
     */
    public static double similarity(String a, String b) {
        if (a == null || b == null) return 0;
        String na = a.trim().toLowerCase();
        String nb = b.trim().toLowerCase();

        if (na.equals(nb)) return 1.0;
        if (na.isEmpty() || nb.isEmpty()) return 0;

        // Containment check: if one contains the other
        if (na.contains(nb) || nb.contains(na)) {
            int minLen = Math.min(na.length(), nb.length());
            int maxLen = Math.max(na.length(), nb.length());
            return (double) minLen / maxLen;
        }

        // Levenshtein distance based similarity
        int maxLen = Math.max(na.length(), nb.length());
        int distance = levenshteinDistance(na, nb);
        return 1.0 - (double) distance / maxLen;
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}
