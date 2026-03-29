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

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentCreateTeamBinding;
import com.ecotrack.app.model.Team;
import com.ecotrack.app.viewmodel.TeamViewModel;
import com.google.android.material.snackbar.Snackbar;

/**
 * Create team form — name, type chips, description, public toggle, create button.
 */
public class CreateTeamFragment extends Fragment {

    private FragmentCreateTeamBinding binding;
    private TeamViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateTeamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        setupButtons();
        observeViewModel();
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        binding.btnCreate.setOnClickListener(v -> createTeam());
    }

    private void createTeam() {
        String name = binding.etTeamName.getText() != null
                ? binding.etTeamName.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null
                ? binding.etDescription.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.tilTeamName.setError("Team name is required");
            return;
        }
        binding.tilTeamName.setError(null);

        // Get selected type
        String type = "club";
        if (binding.chipDepartment.isChecked()) {
            type = "department";
        } else if (binding.chipOther.isChecked()) {
            type = "other";
        }

        Team team = new Team();
        team.setName(name);
        team.setType(type);
        team.setDescription(description);
        team.setPublic(binding.switchPublic.isChecked());

        viewModel.createTeam(team);
    }

    // ── Observers ────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getIsCreating().observe(getViewLifecycleOwner(), creating -> {
            binding.btnCreate.setEnabled(!Boolean.TRUE.equals(creating));
            binding.btnCreate.setText(Boolean.TRUE.equals(creating)
                    ? "Creating…" : "Create Team");
            binding.progressBar.setVisibility(
                    Boolean.TRUE.equals(creating) ? View.VISIBLE : View.GONE);
        });

        viewModel.getCreateResult().observe(getViewLifecycleOwner(), teamId -> {
            if (teamId != null && !teamId.isEmpty()) {
                Snackbar.make(requireView(), "✅ Team created!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getResources().getColor(R.color.bg_card, null))
                        .setTextColor(getResources().getColor(R.color.accent_green, null))
                        .show();
                // Navigate back to team list
                Navigation.findNavController(requireView()).popBackStack();
            }
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
