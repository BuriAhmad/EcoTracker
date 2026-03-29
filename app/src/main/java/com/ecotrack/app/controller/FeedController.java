package com.ecotrack.app.controller;

import com.ecotrack.app.model.ActivityLog;
import com.ecotrack.app.model.FeedItem;
import com.ecotrack.app.model.User;
import com.ecotrack.app.repository.FeedRepository;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.Constants;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Orchestrates social feed operations:
 * posting feed items, paginated retrieval, reactions.
 */
public class FeedController {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    public FeedController() {
        this.feedRepository = new FeedRepository();
        this.userRepository = new UserRepository();
    }

    // ── Callback ─────────────────────────────────────────────────────────

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ── Post to Feed ─────────────────────────────────────────────────────

    /**
     * Post an activity log to the social feed. Respects anonymous-on-feed setting.
     * Fire-and-forget — no callback needed.
     */
    public void postToFeed(String userId, ActivityLog log) {
        userRepository.getUserDocument(userId).addOnSuccessListener(doc -> {
            if (doc == null || !doc.exists()) {
                // User doc not found — post with default info
                postFeedItemWithUser(userId, null, log);
                return;
            }
            User user = doc.toObject(User.class);
            postFeedItemWithUser(userId, user, log);
        }).addOnFailureListener(e -> {
            // Couldn't fetch user — still post the feed item with defaults
            postFeedItemWithUser(userId, null, log);
        });
    }

    /**
     * Build a FeedItem from user info (nullable) and activity log, then save it.
     */
    private void postFeedItemWithUser(String userId, User user, ActivityLog log) {
        FeedItem item = new FeedItem();
        item.setUserId(userId);

        if (user != null && user.isAnonymousOnFeed()) {
            item.setAnonymous(true);
            item.setDisplayName("Anonymous Hero");
            item.setDepartment("");
            item.setAvatarUrl("");
        } else if (user != null) {
            item.setAnonymous(false);
            item.setDisplayName(user.getDisplayName() != null ? user.getDisplayName() : "User");
            item.setDepartment(user.getDepartment() != null ? user.getDepartment() : "");
            item.setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        } else {
            item.setAnonymous(false);
            item.setDisplayName("User");
            item.setDepartment("");
            item.setAvatarUrl("");
        }

        item.setActivityType(log.getActivityType());
        item.setActivityDescription(formatDescription(log.getActivityType(),
                log.getQuantity(), log.getUnit()));
        item.setQuantity(log.getQuantity());
        item.setUnit(log.getUnit());
        item.setCo2Saved(log.getCo2Saved());
        item.setPointsEarned(log.getPointsEarned());

        // Initialize reaction map with all emojis at 0
        HashMap<String, Long> reactions = new HashMap<>();
        reactions.put("\uD83C\uDF31", 0L); // 🌱
        reactions.put("\uD83D\uDC9A", 0L); // 💚
        reactions.put("\uD83C\uDF89", 0L); // 🎉
        reactions.put("\uD83D\uDC4F", 0L); // 👏
        item.setReactions(reactions);
        item.setTimestamp(Timestamp.now()); // Explicit timestamp for immediate query visibility

        feedRepository.postFeedItem(item);
    }

    // ── Retrieve Feed ────────────────────────────────────────────────────

    /**
     * Load the first page of feed items.
     */
    public void getFeedPage(DataCallback<QuerySnapshot> callback) {
        feedRepository.getFeedPage(Constants.PAGE_SIZE_FEED)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onError("Couldn't load feed: " + e.getMessage()));
    }

    /**
     * Load the next page of feed items after the given cursor.
     */
    public void getNextFeedPage(DocumentSnapshot lastVisible,
                                DataCallback<QuerySnapshot> callback) {
        feedRepository.getFeedPageAfter(lastVisible, Constants.PAGE_SIZE_FEED)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onError("Couldn't load more: " + e.getMessage()));
    }

    // ── Reactions ────────────────────────────────────────────────────────

    /**
     * Atomically increment a reaction on a feed item.
     */
    public void addReaction(String feedItemId, String emoji, DataCallback<Void> callback) {
        feedRepository.incrementReaction(feedItemId, emoji)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("Couldn't react: " + e.getMessage()));
    }

    // ── Today's Count (for banner) ───────────────────────────────────────

    public void getTodayFeedCount(DataCallback<Integer> callback) {
        feedRepository.getTodayFeedCount()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Build a human-readable description for a logged activity.
     */
    public static String formatDescription(String activityType, double quantity, String unit) {
        if (activityType == null) return "Logged an activity";
        String q = String.format(Locale.US, "%.1f", quantity);
        switch (activityType) {
            case Constants.ACTIVITY_BIKING:
                return "Biked " + q + " km to campus";
            case Constants.ACTIVITY_WALKING:
                return "Walked " + q + " km";
            case Constants.ACTIVITY_RECYCLING:
                return "Recycled " + q + " kg";
            case Constants.ACTIVITY_WATER_SAVE:
                return "Saved " + q + " L of water";
            case Constants.ACTIVITY_ENERGY_SAVING:
                return "Saved " + q + " kWh of energy";
            case Constants.ACTIVITY_PLASTIC_FREE:
                return q + " plastic-free action(s)";
            case Constants.ACTIVITY_COMPOSTING:
                return "Composted " + q + " kg";
            case Constants.ACTIVITY_REUSE_CUP:
                return "Used reusable cup " + q + " time(s)";
            case Constants.ACTIVITY_MEATLESS_MEAL:
                return q + " meatless meal(s)";
            case Constants.ACTIVITY_PUBLIC_TRANSIT:
                return "Took public transit " + q + " km";
            default:
                return "Logged " + q + " " + (unit != null ? unit : "units");
        }
    }
}
