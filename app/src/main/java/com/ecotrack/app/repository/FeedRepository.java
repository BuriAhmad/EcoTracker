package com.ecotrack.app.repository;

import com.ecotrack.app.model.FeedItem;
import com.ecotrack.app.util.Constants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

/**
 * Firestore CRUD for the social feed.
 * Collection: socialFeed/{feedItemId}
 */
public class FeedRepository {

    private final FirebaseFirestore db;

    public FeedRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Post a new feed item.
     */
    public Task<DocumentReference> postFeedItem(FeedItem item) {
        return db.collection(Constants.COLLECTION_SOCIAL_FEED).add(item);
    }

    /**
     * Get the first page of feed items, ordered by timestamp descending.
     */
    public Task<QuerySnapshot> getFeedPage(int limit) {
        return db.collection(Constants.COLLECTION_SOCIAL_FEED)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }

    /**
     * Get the next page of feed items after the given cursor.
     */
    public Task<QuerySnapshot> getFeedPageAfter(DocumentSnapshot lastVisible, int limit) {
        return db.collection(Constants.COLLECTION_SOCIAL_FEED)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(limit)
                .get();
    }

    /**
     * Atomically increment a reaction count on a feed item.
     * @param feedItemId the document ID
     * @param emoji      the reaction emoji key (e.g. "🌱")
     */
    public Task<Void> incrementReaction(String feedItemId, String emoji) {
        return db.collection(Constants.COLLECTION_SOCIAL_FEED)
                .document(feedItemId)
                .update("reactions." + emoji, FieldValue.increment(1));
    }

    /**
     * Atomically decrement a reaction count on a feed item (min 0).
     */
    public Task<Void> decrementReaction(String feedItemId, String emoji) {
        return db.collection(Constants.COLLECTION_SOCIAL_FEED)
                .document(feedItemId)
                .update("reactions." + emoji, FieldValue.increment(-1));
    }

    /**
     * Get the number of feed items posted today (for the live banner).
     */
    public Task<Integer> getTodayFeedCount() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        return db.collection(Constants.COLLECTION_SOCIAL_FEED)
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(startOfDay))
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) return 0;
                    return task.getResult().size();
                });
    }
}
