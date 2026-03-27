package com.ecotrack.app.util;

import com.ecotrack.app.model.ConversionFactor;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * One-time Firestore seeder for conversion factors, campusStats, and badge definitions.
 * Call FirestoreSeeder.seedIfNeeded() from MainActivity on first launch.
 */
public final class FirestoreSeeder {

    private FirestoreSeeder() {}

    /**
     * Seed conversion factors, campus stats, and badge definitions if they don't already exist.
     */
    public static void seedIfNeeded() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if conversion factors already exist
        db.collection(Constants.COLLECTION_CONVERSION_FACTORS)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        seedConversionFactors(db);
                    }
                });

        // Ensure campusStats/aggregate exists
        db.collection(Constants.COLLECTION_CAMPUS_STATS)
                .document(Constants.DOC_CAMPUS_AGGREGATE)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        seedCampusStats(db);
                    }
                });

        // Ensure badge definitions exist
        db.collection(Constants.COLLECTION_BADGE_DEFINITIONS)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        seedBadgeDefinitions(db);
                    }
                });
    }

    private static void seedConversionFactors(FirebaseFirestore db) {
        Object[][] data = {
                {"biking",       0.21, 0.0,   0.0, 15, "km"},
                {"walking",      0.25, 0.0,   0.0, 10, "km"},
                {"recycling",    0.0,  0.0,   0.4, 20, "items"},
                {"water_save",   0.0,  1.0,   0.0,  5, "L"},
                {"energy_saving",0.12, 0.0,   0.0, 12, "hrs"},
                {"plastic_free", 0.0,  0.0,   0.8, 50, "day"},
        };

        for (Object[] row : data) {
            ConversionFactor f = new ConversionFactor(
                    (String) row[0],
                    (double) row[1],
                    (double) row[2],
                    (double) row[3],
                    (int) row[4],
                    (String) row[5]
            );
            db.collection(Constants.COLLECTION_CONVERSION_FACTORS)
                    .document(f.getActivityType())
                    .set(f);
        }
    }

    private static void seedCampusStats(FirebaseFirestore db) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCo2Saved", 0.0);
        stats.put("totalWaterSaved", 0.0);
        stats.put("totalWasteDiverted", 0.0);
        stats.put("totalActivitiesLogged", 0L);
        stats.put("totalUsers", 0L);

        db.collection(Constants.COLLECTION_CAMPUS_STATS)
                .document(Constants.DOC_CAMPUS_AGGREGATE)
                .set(stats);
    }

    private static void seedBadgeDefinitions(FirebaseFirestore db) {
        // { badgeType, name, description, metric, threshold, rarity }
        Object[][] badges = {
                {"first_log",     "First Step",       "Log your first eco activity",        "totalActivities", 1,    "Common"},
                {"streak_7",      "Week Warrior",     "Maintain a 7-day logging streak",    "currentStreak",   7,    "Common"},
                {"streak_30",     "Monthly Master",   "Maintain a 30-day logging streak",   "currentStreak",   30,   "Rare"},
                {"recycle_50",    "Recycling Rookie", "Divert 50 kg of waste",              "totalRecycled",   50,   "Common"},
                {"recycle_200",   "Recycling Pro",    "Divert 200 kg of waste",             "totalRecycled",   200,  "Epic"},
                {"co2_100kg",     "Carbon Crusher",   "Save 100 kg of CO₂ emissions",      "totalCo2",        100,  "Epic"},
                {"water_1000L",   "Water Guardian",   "Save 1000 litres of water",          "totalWater",      1000, "Rare"},
                {"points_5000",   "Eco Champion",     "Earn 5000 total eco points",         "totalPoints",     5000, "Legendary"},
                {"bike_100km",    "Leaf Glider",      "Bike 100 km to reduce emissions",    "totalBikeKm",     100,  "Rare"},
                {"challenges_5",  "Challenge Seeker", "Complete 5 eco challenges",           "challengesCompleted", 5, "Rare"},
        };

        for (Object[] row : badges) {
            Map<String, Object> def = new HashMap<>();
            def.put("badgeType", row[0]);
            def.put("name", row[1]);
            def.put("description", row[2]);
            def.put("metric", row[3]);
            def.put("threshold", ((Number) row[4]).doubleValue());
            def.put("rarity", row[5]);
            def.put("multiplierBonus", 0.0);
            def.put("iconUrl", "");

            db.collection(Constants.COLLECTION_BADGE_DEFINITIONS)
                    .document((String) row[0])
                    .set(def);
        }
    }
}
