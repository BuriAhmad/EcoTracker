package com.ecotrack.app.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ecotrack.app.model.Badge;
import com.ecotrack.app.model.BadgeDefinition;
import com.ecotrack.app.model.User;
import com.ecotrack.app.util.BadgeEvaluator;
import com.ecotrack.app.util.Constants;

import com.example.saturn.databinding.FragmentBadgeDetailBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Badge detail view — badge info, progress, rarity, unlock date.
 */
public class BadgeDetailFragment extends Fragment {

    private FragmentBadgeDetailBinding binding;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBadgeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        Bundle args = getArguments();
        if (args == null) return;

        String badgeType = args.getString("badgeType", "");
        String userId = args.getString("userId", "");

        if (!badgeType.isEmpty()) {
            loadBadgeDetail(badgeType, userId);
        }
    }

    private void loadBadgeDetail(String badgeType, String userId) {
        // 1. Load badge definition
        db.collection(Constants.COLLECTION_BADGE_DEFINITIONS).document(badgeType)
                .get().addOnSuccessListener(defDoc -> {
                    if (defDoc == null || !defDoc.exists()) return;
                    BadgeDefinition def = defDoc.toObject(BadgeDefinition.class);
                    if (def == null) return;

                    populateDefinition(def);

                    // 2. Check if user earned it
                    if (!userId.isEmpty()) {
                        loadEarnedStatus(userId, badgeType, def);
                    }
                });
    }

    private void populateDefinition(BadgeDefinition def) {
        binding.tvBadgeName.setText(def.getName() != null ? def.getName() : def.getBadgeType());
        binding.tvDescription.setText(def.getDescription() != null ? def.getDescription() : "");
        binding.chipRarity.setText(def.getRarity() != null ? capitalise(def.getRarity()) : "Common");

        // Rarity colour
        if ("epic".equalsIgnoreCase(def.getRarity())) {
            binding.chipRarity.setChipBackgroundColorResource(com.example.saturn.R.color.accent_violet);
        } else if ("rare".equalsIgnoreCase(def.getRarity())) {
            binding.chipRarity.setChipBackgroundColorResource(com.example.saturn.R.color.accent_cyan);
        } else {
            binding.chipRarity.setChipBackgroundColorResource(com.example.saturn.R.color.accent_amber);
        }
    }

    private void loadEarnedStatus(String userId, String badgeType, BadgeDefinition def) {
        // Check if user has this badge
        db.collection(Constants.COLLECTION_USERS).document(userId)
                .collection(Constants.COLLECTION_BADGES)
                .whereEqualTo("badgeType", badgeType)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        // Badge earned
                        Badge badge = snap.getDocuments().get(0).toObject(Badge.class);
                        binding.tvEarnedDate.setVisibility(View.VISIBLE);
                        binding.tvNotEarned.setVisibility(View.GONE);
                        if (badge != null && badge.getUnlockedAt() != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                            binding.tvEarnedDate.setText("Earned on " + sdf.format(badge.getUnlockedAt().toDate()));
                        } else {
                            binding.tvEarnedDate.setText("Earned ✓");
                        }
                        binding.progressBar.setProgress(100);
                        binding.tvProgressText.setText("Completed!");
                        binding.ivBadgeIcon.setAlpha(1.0f);
                    } else {
                        // Not earned — show progress
                        binding.tvEarnedDate.setVisibility(View.GONE);
                        binding.tvNotEarned.setVisibility(View.VISIBLE);
                        binding.ivBadgeIcon.setAlpha(0.4f);
                        loadProgressFromUser(userId, def);
                    }
                });
    }

    private void loadProgressFromUser(String userId, BadgeDefinition def) {
        db.collection(Constants.COLLECTION_USERS).document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) return;
                    User user = doc.toObject(User.class);
                    if (user == null) return;

                    double currentValue = BadgeEvaluator.getMetricValue(user, def.getMetric());
                    double threshold = def.getThreshold();
                    int progress = threshold > 0 ? (int) ((currentValue / threshold) * 100) : 0;
                    progress = Math.min(progress, 100);

                    binding.progressBar.setProgress(progress);
                    binding.tvProgressText.setText(
                            String.format(Locale.getDefault(), "%.0f / %.0f", currentValue, threshold));
                });
    }

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
