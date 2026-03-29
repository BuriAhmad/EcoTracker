package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.controller.FeedController;
import com.ecotrack.app.model.FeedItem;
import com.ecotrack.app.util.Constants;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * UI state for the social feed screen.
 * Manages paginated feed items, today's count, and reactions.
 */
public class FeedViewModel extends ViewModel {

    private final FeedController feedController;

    private final MutableLiveData<List<FeedItem>> feedItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> todayCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private DocumentSnapshot lastVisible;
    private boolean hasMoreData = true;
    private boolean isLoadingMore = false;

    public FeedViewModel() {
        feedController = new FeedController();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<List<FeedItem>> getFeedItems() { return feedItems; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsRefreshing() { return isRefreshing; }
    public LiveData<Integer> getTodayCount() { return todayCount; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public boolean hasMoreData() { return hasMoreData; }
    public boolean isLoadingMore() { return isLoadingMore; }

    // ── Load Feed ────────────────────────────────────────────────────────

    /**
     * Load the initial page of the feed (clears existing data).
     */
    public void loadFeed() {
        isLoading.setValue(true);
        lastVisible = null;
        hasMoreData = true;

        feedController.getFeedPage(new FeedController.DataCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshot) {
                List<FeedItem> items = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    FeedItem item = doc.toObject(FeedItem.class);
                    if (item != null) items.add(item);
                }

                if (!snapshot.isEmpty()) {
                    lastVisible = snapshot.getDocuments()
                            .get(snapshot.size() - 1);
                }
                hasMoreData = snapshot.size() >= Constants.PAGE_SIZE_FEED;

                feedItems.setValue(items);
                isLoading.setValue(false);
                isRefreshing.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
                isRefreshing.setValue(false);
            }
        });
    }

    /**
     * Refresh the feed (pull-to-refresh).
     */
    public void refreshFeed() {
        isRefreshing.setValue(true);
        loadFeed();
        loadTodayCount();
    }

    /**
     * Load the next page of feed items (infinite scroll).
     */
    public void loadMoreFeed() {
        if (!hasMoreData || isLoadingMore || lastVisible == null) return;
        isLoadingMore = true;

        feedController.getNextFeedPage(lastVisible,
                new FeedController.DataCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshot) {
                List<FeedItem> current = feedItems.getValue();
                if (current == null) current = new ArrayList<>();

                List<FeedItem> newItems = new ArrayList<>(current);
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    FeedItem item = doc.toObject(FeedItem.class);
                    if (item != null) newItems.add(item);
                }

                if (!snapshot.isEmpty()) {
                    lastVisible = snapshot.getDocuments()
                            .get(snapshot.size() - 1);
                }
                hasMoreData = snapshot.size() >= Constants.PAGE_SIZE_FEED;

                feedItems.setValue(newItems);
                isLoadingMore = false;
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoadingMore = false;
            }
        });
    }

    // ── Today's Count ────────────────────────────────────────────────────

    public void loadTodayCount() {
        feedController.getTodayFeedCount(new FeedController.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                todayCount.setValue(count);
            }

            @Override
            public void onError(String message) {
                // Silently ignore — banner is non-critical
            }
        });
    }

    // ── Reactions ────────────────────────────────────────────────────────

    /**
     * Increment a reaction on a feed item. Updates the local list optimistically.
     */
    public void addReaction(String feedItemId, String emoji) {
        // Optimistic update
        List<FeedItem> current = feedItems.getValue();
        if (current != null) {
            for (FeedItem item : current) {
                if (feedItemId.equals(item.getFeedItemId()) && item.getReactions() != null) {
                    Long count = item.getReactions().get(emoji);
                    item.getReactions().put(emoji, (count != null ? count : 0) + 1);
                    break;
                }
            }
            feedItems.setValue(current); // Trigger observer
        }

        // Persist to Firestore
        feedController.addReaction(feedItemId, emoji, new FeedController.DataCallback<Void>() {
            @Override public void onSuccess(Void data) { }
            @Override public void onError(String message) { }
        });
    }
}
