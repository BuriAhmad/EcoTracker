package com.ecotrack.app.view.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ecotrack.app.model.User;
import com.ecotrack.app.util.Constants;
import com.ecotrack.app.viewmodel.LeaderboardViewModel;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentLeaderboardBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Leaderboard screen — podium top-3, ranked list, time period tabs.
 */
public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;
    private LeaderboardViewModel viewModel;
    private LeaderboardAdapter adapter;
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);

        setupRecyclerView();
        setupChips();
        setupTeamsButton();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        String period = getSelectedPeriod();
        // Only reload if the data is stale (different period or older than 5 min)
        if (viewModel.isStale(period)) {
            viewModel.setTimePeriod(period);
        }
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) adapter.setCurrentUserId(uid);

        binding.rvRanked.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRanked.setAdapter(adapter);
    }

    private void setupChips() {
        binding.chipWeek.setOnClickListener(v -> viewModel.setTimePeriod(Constants.PERIOD_THIS_WEEK));
        binding.chipMonth.setOnClickListener(v -> viewModel.setTimePeriod(Constants.PERIOD_THIS_MONTH));
        binding.chipAllTime.setOnClickListener(v -> viewModel.setTimePeriod(Constants.PERIOD_ALL_TIME));
    }

    private void setupTeamsButton() {
        binding.btnViewTeams.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_board_to_teams));
    }

    private String getSelectedPeriod() {
        if (binding.chipWeek.isChecked()) return Constants.PERIOD_THIS_WEEK;
        if (binding.chipMonth.isChecked()) return Constants.PERIOD_THIS_MONTH;
        return Constants.PERIOD_ALL_TIME;
    }

    // ── Observers ────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getLeaderboardEntries().observe(getViewLifecycleOwner(), this::populateLeaderboard);

        viewModel.getUserRank().observe(getViewLifecycleOwner(), rank -> {
            if (rank != null && rank > 0) {
                binding.cardYourRank.setVisibility(View.VISIBLE);
                binding.tvYourRankValue.setText("#" + rank);
            } else {
                binding.cardYourRank.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });
    }

    private void populateLeaderboard(List<User> entries) {
        if (entries == null || entries.isEmpty()) {
            binding.podiumContainer.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
            adapter.submitList(entries != null ? entries : List.of());
            return;
        }

        binding.tvEmpty.setVisibility(View.GONE);

        // ── Podium (top 3) ──────────────────────────────────────────
        if (entries.size() >= 1) {
            binding.podiumContainer.setVisibility(View.VISIBLE);
            User first = entries.get(0);
            binding.tvFirstName.setText(firstName(first.getDisplayName()));
            binding.tvFirstPoints.setText(nf.format(first.getTotalPoints()) + " pts");
        }
        if (entries.size() >= 2) {
            User second = entries.get(1);
            binding.tvSecondName.setText(firstName(second.getDisplayName()));
            binding.tvSecondPoints.setText(nf.format(second.getTotalPoints()) + " pts");
        }
        if (entries.size() >= 3) {
            User third = entries.get(2);
            binding.tvThirdName.setText(firstName(third.getDisplayName()));
            binding.tvThirdPoints.setText(nf.format(third.getTotalPoints()) + " pts");
        }

        // ── List (rank 4+) ──────────────────────────────────────────
        if (entries.size() > 3) {
            adapter.submitList(entries.subList(3, entries.size()));
        } else {
            adapter.submitList(List.of());
        }
    }

    private String firstName(String displayName) {
        if (displayName == null) return "User";
        String[] parts = displayName.trim().split("\\s+");
        return parts[0];
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
