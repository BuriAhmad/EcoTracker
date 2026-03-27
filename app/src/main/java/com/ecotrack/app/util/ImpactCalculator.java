package com.ecotrack.app.util;

import com.ecotrack.app.model.ConversionFactor;

/**
 * Stateless utility to compute environmental impact from an activity + conversion factor.
 */
public final class ImpactCalculator {

    private ImpactCalculator() {
        // Prevent instantiation
    }

    /**
     * Calculate the environmental impact for a given quantity and conversion factor.
     *
     * @param quantity The amount logged (e.g., km biked, items recycled)
     * @param factor   The conversion factor for the activity type
     * @return An ImpactResult with co2, water, waste, and points
     */
    public static ImpactResult calculateImpact(double quantity, ConversionFactor factor) {
        if (quantity <= 0 || factor == null) {
            return new ImpactResult(0, 0, 0, 0);
        }

        double co2 = quantity * factor.getCo2PerUnit();
        double water = quantity * factor.getWaterPerUnit();
        double waste = quantity * factor.getWastePerUnit();
        int points = (int) Math.round(quantity * factor.getPointsPerUnit());

        return new ImpactResult(co2, water, waste, points);
    }

    /**
     * Holds the computed impact values for a single activity log.
     */
    public static class ImpactResult {
        private final double co2Saved;
        private final double waterSaved;
        private final double wasteDiverted;
        private final int pointsEarned;

        public ImpactResult(double co2Saved, double waterSaved, double wasteDiverted, int pointsEarned) {
            this.co2Saved = co2Saved;
            this.waterSaved = waterSaved;
            this.wasteDiverted = wasteDiverted;
            this.pointsEarned = pointsEarned;
        }

        public double getCo2Saved() { return co2Saved; }
        public double getWaterSaved() { return waterSaved; }
        public double getWasteDiverted() { return wasteDiverted; }
        public int getPointsEarned() { return pointsEarned; }

        /** Whether this impact has any non-zero values. */
        public boolean hasImpact() {
            return co2Saved > 0 || waterSaved > 0 || wasteDiverted > 0 || pointsEarned > 0;
        }
    }
}
