package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.model.User;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.Constants;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * UI state for the Leaderboard screen.
 * Time-period tabs, paginated ranked list, current user rank.
 */
public class LeaderboardViewModel extends ViewModel {

    private final FirebaseFirestore db;
    private final UserRepository userRepository;

    private final MutableLiveData<String> timePeriod = new MutableLiveData<>(Constants.PERIOD_ALL_TIME);
    private final MutableLiveData<List<User>> leaderboardEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> userRank = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private DocumentSnapshot lastVisible = null;
    private boolean hasMorePages = true;

    public LeaderboardViewModel() {
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<String> getTimePeriod() { return timePeriod; }
    public LiveData<List<User>> getLeaderboardEntries() { return leaderboardEntries; }
    public LiveData<Integer> getUserRank() { return userRank; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // ── Actions ──────────────────────────────────────────────────────────

    /**
     * Set time period and reload leaderboard from scratch.
     */
    public void setTimePeriod(String period) {
        timePeriod.setValue(period);
        lastVisible = null;
        hasMorePages = true;
        leaderboardEntries.setValue(new ArrayList<>());
        loadLeaderboard();
    }

    /**
     * Load first or next page of leaderboard.
     * For MVP, all periods use totalPoints (time-windowed scoring needs Cloud Functions).
     */
    public void loadLeaderboard() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || !hasMorePages) return;
        isLoading.setValue(true);

        Query query = db.collection(Constants.COLLECTION_USERS)
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .limit(Constants.PAGE_SIZE_LEADERBOARD);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnSuccessListener(snapshot -> {
            isLoading.setValue(false);

            if (snapshot.isEmpty()) {
                hasMorePages = false;
                return;
            }

            List<User> page = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot) {
                User user = doc.toObject(User.class);
                if (user != null) page.add(user);
            }

            lastVisible = snapshot.getDocuments().get(snapshot.size() - 1);
            hasMorePages = snapshot.size() >= Constants.PAGE_SIZE_LEADERBOARD;

            // Append to existing list
            List<User> current = leaderboardEntries.getValue();
            if (current == null) current = new ArrayList<>();
            current.addAll(page);
            leaderboardEntries.setValue(current);

            // Compute user rank from full list
            computeUserRank(current);
        }).addOnFailureListener(e -> isLoading.setValue(false));
    }

    /**
     * Load more pages (infinite scroll).
     */
    public void loadNextPage() {
        loadLeaderboard();
    }

    private void computeUserRank(List<User> entries) {
        String currentId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (currentId == null) return;

        for (int i = 0; i < entries.size(); i++) {
            if (currentId.equals(entries.get(i).getUserId())) {
                userRank.setValue(i + 1);
                return;
            }
        }
    }
}
