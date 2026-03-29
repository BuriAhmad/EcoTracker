package com.ecotrack.app.view.challenges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.model.ChallengeParticipant;
import com.ecotrack.app.util.Constants;
import com.ecotrack.app.viewmodel.ChallengeViewModel;
import com.example.saturn.R;
import com.example.saturn.databinding.FragmentChallengeDetailBinding;

import java.util.Locale;

/**
 * Challenge detail — progress ring, description, participants, join/leave.
 */
public class ChallengeDetailFragment extends Fragment {

    private FragmentChallengeDetailBinding binding;
    private ChallengeViewModel viewModel;
    private ParticipantAdapter participantAdapter;
    private String challengeId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChallengeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChallengeViewModel.class);

        // Read challengeId from Bundle args
        if (getArguments() != null) {
            challengeId = getArguments().getString("challengeId", "");
        }
        if (challengeId == null || challengeId.isEmpty()) {
            Navigation.findNavController(view).popBackStack();
            return;
        }

        setupParticipantsList();
        setupListeners();
        observeViewModel();

        viewModel.loadChallengeDetail(challengeId);
    }

    private void setupParticipantsList() {
        participantAdapter = new ParticipantAdapter();
        binding.recyclerParticipants.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerParticipants.setAdapter(participantAdapter);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        binding.btnJoin.setOnClickListener(v -> {
            Boolean joined = viewModel.getIsJoined().getValue();
            if (joined != null && joined) {
                viewModel.leaveChallenge(challengeId);
            } else {
                viewModel.joinChallenge(challengeId);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCurrentChallenge().observe(getViewLifecycleOwner(), this::bindChallenge);

        viewModel.getIsJoined().observe(getViewLifecycleOwner(), joined -> {
            if (joined) {
                binding.btnJoin.setText("Leave Challenge");
                binding.btnJoin.setBackgroundTintList(
                        getResources().getColorStateList(R.color.bg_card, null));
                binding.btnJoin.setTextColor(getResources().getColor(R.color.text_primary, null));
                binding.btnJoin.setStrokeColorResource(R.color.border_card);
                binding.btnJoin.setStrokeWidth(2);
            } else {
                binding.btnJoin.setText("Join Challenge");
                binding.btnJoin.setBackgroundTintList(
                        getResources().getColorStateList(R.color.accent_green, null));
                binding.btnJoin.setTextColor(getResources().getColor(R.color.text_on_accent, null));
                binding.btnJoin.setStrokeWidth(0);
            }
        });

        viewModel.getUserParticipant().observe(getViewLifecycleOwner(), participant -> {
            if (participant != null) {
                binding.cardProgress.setVisibility(View.VISIBLE);
                Challenge c = viewModel.getCurrentChallenge().getValue();
                String unit = c != null && c.getUnit() != null ? c.getUnit() : "";
                double goal = c != null ? c.getGoalQuantity() : 0;
                int progressPercent = goal > 0
                        ? (int) (participant.getCurrentProgress() / goal * 100) : 0;
                binding.progressRing.setProgress(Math.min(100, progressPercent));
                binding.tvProgressText.setText(String.format(Locale.US,
                        "%.1f / %.0f %s", participant.getCurrentProgress(), goal, unit));
            } else {
                binding.cardProgress.setVisibility(View.GONE);
            }
        });

        viewModel.getParticipants().observe(getViewLifecycleOwner(), participants -> {
            Challenge c = viewModel.getCurrentChallenge().getValue();
            participantAdapter.setUnit(c != null ? c.getUnit() : "");
            participantAdapter.setParticipants(participants);
        });

        viewModel.getIsDetailLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindChallenge(Challenge challenge) {
        if (challenge == null) return;

        binding.tvTitle.setText(challenge.getTitle());
        binding.tvDescription.setText(challenge.getDescription());
        binding.tvParticipantCount.setText(String.valueOf(challenge.getParticipantCount()));
        binding.tvPointsReward.setText(String.valueOf(challenge.getPointsReward()));
        binding.tvDaysRemaining.setText(String.valueOf(challenge.getDaysRemaining()));

        // Activity icon
        int iconRes = getActivityIcon(challenge.getActivityType());
        int tintColor = getActivityColor(challenge.getActivityType());
        binding.ivIcon.setImageResource(iconRes);
        binding.ivIcon.setColorFilter(requireContext().getColor(tintColor));
    }

    private int getActivityIcon(String type) {
        if (type == null) return R.drawable.ic_eco_24;
        switch (type) {
            case Constants.ACTIVITY_BIKING: return R.drawable.ic_directions_bike_24;
            case Constants.ACTIVITY_WALKING: return R.drawable.ic_directions_walk_24;
            case Constants.ACTIVITY_RECYCLING: return R.drawable.ic_recycling_24;
            case Constants.ACTIVITY_WATER_SAVE: return R.drawable.ic_water_drop_24;
            case Constants.ACTIVITY_ENERGY_SAVING: return R.drawable.ic_bolt_24;
            default: return R.drawable.ic_eco_24;
        }
    }

    private int getActivityColor(String type) {
        if (type == null) return R.color.accent_green;
        switch (type) {
            case Constants.ACTIVITY_BIKING: return R.color.color_biking;
            case Constants.ACTIVITY_WALKING: return R.color.color_walking;
            case Constants.ACTIVITY_RECYCLING: return R.color.color_recycling;
            case Constants.ACTIVITY_WATER_SAVE: return R.color.color_water_save;
            case Constants.ACTIVITY_ENERGY_SAVING: return R.color.color_energy;
            default: return R.color.accent_green;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
