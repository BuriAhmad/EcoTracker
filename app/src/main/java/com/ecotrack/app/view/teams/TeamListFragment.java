package com.ecotrack.app.view.teams;

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
import com.example.saturn.databinding.FragmentTeamListBinding;
import com.ecotrack.app.viewmodel.TeamViewModel;
import com.google.android.material.snackbar.Snackbar;

/**
 * Team list screen — list of teams with filter pills and create FAB.
 */
public class TeamListFragment extends Fragment {

    private FragmentTeamListBinding binding;
    private TeamViewModel viewModel;
    private TeamAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTeamListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        setupRecyclerView();
        setupChips();
        setupButtons();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTeams();
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new TeamAdapter(teamId -> {
            Bundle args = new Bundle();
            args.putString("teamId", teamId);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_teams_to_detail, args);
        });
        binding.rvTeams.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTeams.setAdapter(adapter);
    }

    private void setupChips() {
        binding.chipAll.setOnClickListener(v -> {
            viewModel.loadTeams();
        });
        binding.chipClubs.setOnClickListener(v -> {
            viewModel.loadTeamsByType("club");
        });
        binding.chipDepartments.setOnClickListener(v -> {
            viewModel.loadTeamsByType("department");
        });
    }

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        binding.fabCreate.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_teams_to_create));
    }

    private void loadTeams() {
        if (binding.chipClubs.isChecked()) {
            viewModel.loadTeamsByType("club");
        } else if (binding.chipDepartments.isChecked()) {
            viewModel.loadTeamsByType("department");
        } else {
            viewModel.loadTeams();
        }
    }

    // ── Observers ────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getTeams().observe(getViewLifecycleOwner(), teams -> {
            adapter.setTeams(teams);
            binding.tvEmpty.setVisibility(
                    teams == null || teams.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(
                    Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.bg_card, null))
                        .setTextColor(getResources().getColor(R.color.color_error, null))
                        .show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
