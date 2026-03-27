package com.ecotrack.app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Converts raw CO₂ savings into relatable real-world analogies.
 *
 * Conversion sources:
 * - 1 tree absorbs ~22 kg CO₂/year (EPA)
 * - 1 gallon of gasoline = 8.89 kg CO₂ → 1 mile ≈ 0.35 kg CO₂ (average car)
 * - Average US home ≈ 30 kWh/day → ~12 kg CO₂/day
 */
public final class EquivalencyTranslator {

    private EquivalencyTranslator() { /* Prevent instantiation */ }

    // Conversion constants
    private static final double CO2_PER_TREE_PER_YEAR = 22.0;   // kg
    private static final double CO2_PER_MILE_DRIVEN = 0.35;      // kg
    private static final double CO2_PER_HOME_PER_DAY = 12.0;     // kg

    /**
     * Simple data holder for a single equivalency.
     */
    public static class Equivalency {
        private final String icon;
        private final String description;
        private final double value;

        public Equivalency(String icon, String description, double value) {
            this.icon = icon;
            this.description = description;
            this.value = value;
        }

        public String getIcon() { return icon; }
        public String getDescription() { return description; }
        public double getValue() { return value; }
    }

    /**
     * Translate a CO₂ savings amount into relatable analogies.
     *
     * @param co2Kg total CO₂ saved in kilograms
     * @return list of Equivalency objects (always 3 entries)
     */
    public static List<Equivalency> translate(double co2Kg) {
        List<Equivalency> equivalencies = new ArrayList<>();

        double trees = co2Kg / CO2_PER_TREE_PER_YEAR;
        equivalencies.add(new Equivalency(
                "🌳",
                String.format(Locale.US, "Planted %.1f trees", trees),
                trees));

        double milesNotDriven = co2Kg / CO2_PER_MILE_DRIVEN;
        equivalencies.add(new Equivalency(
                "🚗",
                String.format(Locale.US, "%.0f miles not driven", milesNotDriven),
                milesNotDriven));

        double homeDays = co2Kg / CO2_PER_HOME_PER_DAY;
        equivalencies.add(new Equivalency(
                "💡",
                String.format(Locale.US, "Powered %.1f homes for a day", homeDays),
                homeDays));

        return equivalencies;
    }
}
