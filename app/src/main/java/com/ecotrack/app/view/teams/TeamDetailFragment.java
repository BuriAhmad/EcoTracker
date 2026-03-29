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
import com.example.saturn.databinding.FragmentTeamDetailBinding;
import com.ecotrack.app.model.Team;
import com.ecotrack.app.viewmodel.TeamViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Team detail — hero, stats, join/leave button, member leaderboard.
 */
public class TeamDetailFragment extends Fragment {

    private FragmentTeamDetailBinding binding;
    private TeamViewModel viewModel;
    private TeamMemberAdapter memberAdapter;
    private String teamId;
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTeamDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        // Get teamId from arguments
        if (getArguments() != null) {
            teamId = getArguments().getString("teamId");
        }

        setupUI();
        observeViewModel();

        if (teamId != null) {
            viewModel.loadTeamDetail(teamId);
        }
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupUI() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        memberAdapter = new TeamMemberAdapter();
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMembers.setAdapter(memberAdapter);

        binding.btnJoinLeave.setOnClickListener(v -> {
            if (teamId == null) return;
            Boolean member = viewModel.getIsMember().getValue();
            if (Boolean.TRUE.equals(member)) {
                viewModel.leaveTeam(teamId);
            } else {
                viewModel.joinTeam(teamId);
            }
        });
    }

    // ── Observers ────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getCurrentTeam().observe(getViewLifecycleOwner(), this::populateTeam);

        viewModel.getTeamMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                memberAdapter.setMembers(members);
            }
        });

        viewModel.getIsMember().observe(getViewLifecycleOwner(), isMember -> {
            if (Boolean.TRUE.equals(isMember)) {
                binding.btnJoinLeave.setText("Leave Team");
                binding.btnJoinLeave.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                getResources().getColor(R.color.color_error, null)));
            } else {
                binding.btnJoinLeave.setText("Join Team");
                binding.btnJoinLeave.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                getResources().getColor(R.color.accent_green, null)));
            }
        });

        viewModel.getIsDetailLoading().observe(getViewLifecycleOwner(), loading -> {
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

    private void populateTeam(Team team) {
        if (team == null) return;

        binding.tvTeamName.setText(team.getName() != null ? team.getName() : "Team");
        binding.tvTeamInitial.setText(team.getInitial());

        String type = team.getType();
        if (type != null) {
            binding.tvTeamType.setText(type.substring(0, 1).toUpperCase() + type.substring(1));
        }

        binding.tvDescription.setText(
                team.getDescription() != null ? team.getDescription() : "");
        binding.tvDescription.setVisibility(
                team.getDescription() != null && !team.getDescription().isEmpty()
                        ? View.VISIBLE : View.GONE);

        binding.tvStatMembers.setText(String.valueOf(team.getMemberCount()));
        binding.tvStatPoints.setText(nf.format(team.getTotalPoints()));

        long avgPoints = team.getMemberCount() > 0
                ? team.getTotalPoints() / team.getMemberCount() : 0;
        binding.tvStatAvgPoints.setText(nf.format(avgPoints));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
