package com.ecotrack.app.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.ecotrack.app.model.Badge;
import com.ecotrack.app.model.BadgeDefinition;
import com.ecotrack.app.model.User;
import com.ecotrack.app.util.Constants;
import com.ecotrack.app.viewmodel.ProfileViewModel;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Profile screen — stats, streak card, badges grid, impact breakdown.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private BadgeGridAdapter badgeAdapter;
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupBadgeGrid();
        setupActions();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadProfile();
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupBadgeGrid() {
        badgeAdapter = new BadgeGridAdapter();
        badgeAdapter.setOnBadgeClickListener((def, isEarned) -> {
            Bundle args = new Bundle();
            args.putString("badgeType", def.getBadgeType());
            String uid = FirebaseAuth.getInstance().getUid();
            args.putString("userId", uid != null ? uid : "");
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profile_to_badge_detail, args);
        });
        binding.rvBadges.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.rvBadges.setAdapter(badgeAdapter);
    }

    private void setupActions() {
        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profile_to_edit));

        binding.btnAdminPanel.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profile_to_admin));

        binding.btnSignOut.setOnClickListener(v -> {
            viewModel.logout();
            Navigation.findNavController(v).navigate(R.id.action_profile_to_login);
        });
    }

    // ── Observers ────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), this::populateUser);

        viewModel.getEcoScore().observe(getViewLifecycleOwner(), score ->
                binding.tvStatEcoScore.setText(String.valueOf(score)));

        viewModel.getStreakMultiplier().observe(getViewLifecycleOwner(), mult -> {
            if (mult > 1.0) {
                binding.chipMultiplier.setVisibility(View.VISIBLE);
                binding.chipMultiplier.setText(mult + "× multiplier");
            } else {
                binding.chipMultiplier.setVisibility(View.GONE);
            }
        });

        // Combine badge data when both arrive
        viewModel.getBadgeDefinitions().observe(getViewLifecycleOwner(), defs -> {
            List<Badge> earned = viewModel.getEarnedBadges().getValue();
            badgeAdapter.submitData(defs, earned);
            updateBadgeCount(defs, earned);
        });
        viewModel.getEarnedBadges().observe(getViewLifecycleOwner(), earned -> {
            List<BadgeDefinition> defs = viewModel.getBadgeDefinitions().getValue();
            if (defs != null) {
                badgeAdapter.submitData(defs, earned);
                updateBadgeCount(defs, earned);
            }
        });
    }

    private void populateUser(User user) {
        if (user == null) return;

        // Show admin panel button for admin users
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            binding.btnAdminPanel.setVisibility(View.VISIBLE);
        } else {
            binding.btnAdminPanel.setVisibility(View.GONE);
        }

        binding.tvDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
        binding.tvDepartment.setText(user.getDepartment() != null ? user.getDepartment() : "");

        // Joined date
        if (user.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            binding.tvJoinedDate.setText("Member since " + sdf.format(user.getCreatedAt().toDate()));
        }

        // Stats row
        binding.tvStatPoints.setText(nf.format(user.getTotalPoints()));
        binding.tvStatActivities.setText(String.valueOf(user.getTotalActivitiesLogged()));

        // Streak card
        int streak = user.getCurrentStreak();
        binding.tvStreakCount.setText(streak + " Day Streak");

        // Progress to next milestone
        int nextMilestone;
        if (streak < Constants.STREAK_TIER_1_DAYS) {
            nextMilestone = Constants.STREAK_TIER_1_DAYS;
        } else {
            nextMilestone = Constants.STREAK_TIER_2_DAYS;
        }
        int progress = (int) (((double) streak / nextMilestone) * 100);
        binding.progressStreak.setProgress(Math.min(progress, 100));
        binding.tvStreakMilestone.setText("Next milestone: " + nextMilestone + " days");

        // Impact breakdown
        binding.tvImpactCo2.setText(String.format(Locale.getDefault(), "%.1f kg", user.getTotalCo2Saved()));
        binding.tvImpactWater.setText(String.format(Locale.getDefault(), "%.0f L", user.getTotalWaterSaved()));
        binding.tvImpactWaste.setText(String.format(Locale.getDefault(), "%.1f kg", user.getTotalWasteDiverted()));
    }

    private void updateBadgeCount(List<BadgeDefinition> defs, List<Badge> earned) {
        int total = defs != null ? defs.size() : 0;
        int count = earned != null ? earned.size() : 0;
        binding.tvBadgeCount.setText(count + " of " + total + " earned");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
