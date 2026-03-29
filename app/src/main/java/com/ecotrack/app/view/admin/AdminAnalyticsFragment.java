package com.ecotrack.app.view.admin;

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

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentAdminAnalyticsBinding;
import com.ecotrack.app.view.teams.TeamMemberAdapter;
import com.ecotrack.app.viewmodel.AdminViewModel;

import java.util.Locale;
import java.util.Map;

/**
 * Admin analytics dashboard — campus stats 2×2 grid, top students,
 * quick actions (create challenge).
 */
public class AdminAnalyticsFragment extends Fragment {

    private FragmentAdminAnalyticsBinding binding;
    private AdminViewModel viewModel;
    private TeamMemberAdapter topStudentsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        setupUI();
        observeViewModel();
        viewModel.loadDashboard();
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupUI() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        // Top students list (reusing TeamMemberAdapter for rank/name/points)
        topStudentsAdapter = new TeamMemberAdapter();
        binding.rvTopStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTopStudents.setAdapter(topStudentsAdapter);

        // Quick action: Create Challenge
        binding.btnCreateChallenge.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_admin_to_create_challenge));
    }

    // ── Observers ────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getUserCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvStatUsers.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getChallengeCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvStatChallenges.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getTeamCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvStatTeams.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getCampusStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                Object co2 = stats.get("totalCo2Saved");
                double co2Value = co2 instanceof Number ? ((Number) co2).doubleValue() : 0;
                binding.tvStatCo2.setText(String.format(Locale.US, "%.1f kg", co2Value));
            }
        });

        viewModel.getTopStudents().observe(getViewLifecycleOwner(), students -> {
            if (students != null) {
                topStudentsAdapter.setMembers(students);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(
                    Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
