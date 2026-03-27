package com.ecotrack.app.util;

/**
 * Computes a composite 0–100 Eco-Score from normalized environmental metrics.
 *
 * Formula: score = (0.4 × co2_norm + 0.25 × waste_norm + 0.2 × water_norm + 0.15 × streak_norm) × 100
 *
 * Normalization caps (MAX values):
 *   CO₂   = 500 kg
 *   Waste  = 200 kg
 *   Water  = 5000 L
 *   Streak = 60 days
 */
public final class EcoScoreCalculator {

    private EcoScoreCalculator() { /* Prevent instantiation */ }

    // Normalization caps — tuned so early users see visible progress
    private static final double MAX_CO2 = 50.0;     // 50 kg CO₂
    private static final double MAX_WASTE = 20.0;    // 20 kg waste
    private static final double MAX_WATER = 500.0;   // 500 L water
    private static final double MAX_STREAK = 14.0;   // 14-day streak

    /**
     * Calculate a composite eco-score from the user's cumulative metrics.
     *
     * @param co2Total    total CO₂ saved in kg
     * @param wasteTotal  total waste diverted in kg
     * @param waterTotal  total water saved in litres
     * @param streak      current streak in days
     * @return eco-score as an integer 0–100
     */
    public static int calculateEcoScore(double co2Total, double wasteTotal,
                                         double waterTotal, int streak) {
        double co2Norm = normalize(co2Total, MAX_CO2);
        double wasteNorm = normalize(wasteTotal, MAX_WASTE);
        double waterNorm = normalize(waterTotal, MAX_WATER);
        double streakNorm = normalize(streak, MAX_STREAK);

        double score = (Constants.ECO_WEIGHT_CO2 * co2Norm
                + Constants.ECO_WEIGHT_WASTE * wasteNorm
                + Constants.ECO_WEIGHT_WATER * waterNorm
                + Constants.ECO_WEIGHT_STREAK * streakNorm) * 100.0;

        return (int) Math.round(Math.min(score, 100));
    }

    /**
     * Normalize a value to [0, 1] by capping at max.
     */
    private static double normalize(double value, double max) {
        if (value <= 0 || max <= 0) return 0;
        return Math.min(value / max, 1.0);
    }

    /**
     * Returns a text label for the given eco-score.
     */
    public static String getLevel(int ecoScore) {
        if (ecoScore >= 80) return "Eco Champion";
        if (ecoScore >= 60) return "Eco Warrior";
        if (ecoScore >= 40) return "Eco Advocate";
        if (ecoScore >= 20) return "Eco Starter";
        return "Eco Newcomer";
    }
}
