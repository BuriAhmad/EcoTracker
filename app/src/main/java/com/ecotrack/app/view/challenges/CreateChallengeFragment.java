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

import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.util.Constants;
import com.ecotrack.app.viewmodel.ChallengeViewModel;
import com.example.saturn.R;
import com.example.saturn.databinding.FragmentCreateChallengeBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Create challenge form — admin only, form to create time-boxed challenges.
 */
public class CreateChallengeFragment extends Fragment {

    private FragmentCreateChallengeBinding binding;
    private ChallengeViewModel viewModel;

    private String selectedActivityType = null;
    private Long startDateMillis = null;
    private Long endDateMillis = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateChallengeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChallengeViewModel.class);

        setupActivityTypeChips();
        setupDatePickers();
        setupListeners();
        observeViewModel();
    }

    private void setupActivityTypeChips() {
        binding.chipGroupActivity.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedActivityType = null;
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipBiking) {
                selectedActivityType = Constants.ACTIVITY_BIKING;
                binding.etUnit.setText("km");
            } else if (id == R.id.chipWalking) {
                selectedActivityType = Constants.ACTIVITY_WALKING;
                binding.etUnit.setText("km");
            } else if (id == R.id.chipRecycling) {
                selectedActivityType = Constants.ACTIVITY_RECYCLING;
                binding.etUnit.setText("kg");
            } else if (id == R.id.chipWater) {
                selectedActivityType = Constants.ACTIVITY_WATER_SAVE;
                binding.etUnit.setText("L");
            } else if (id == R.id.chipEnergy) {
                selectedActivityType = Constants.ACTIVITY_ENERGY_SAVING;
                binding.etUnit.setText("kWh");
            } else if (id == R.id.chipTransit) {
                selectedActivityType = Constants.ACTIVITY_PUBLIC_TRANSIT;
                binding.etUnit.setText("km");
            }
        });
    }

    private void setupDatePickers() {
        binding.etStartDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Start Date")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                startDateMillis = selection;
                binding.etStartDate.setText(dateFormat.format(new Date(selection)));
            });
            picker.show(getParentFragmentManager(), "startDate");
        });

        binding.etEndDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("End Date")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                endDateMillis = selection;
                binding.etEndDate.setText(dateFormat.format(new Date(selection)));
            });
            picker.show(getParentFragmentManager(), "endDate");
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        binding.btnPublish.setOnClickListener(v -> publishChallenge());
    }

    private void publishChallenge() {
        String title = binding.etChallengeTitle.getText() != null
                ? binding.etChallengeTitle.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null
                ? binding.etDescription.getText().toString().trim() : "";
        String goalStr = binding.etGoal.getText() != null
                ? binding.etGoal.getText().toString().trim() : "";
        String unit = binding.etUnit.getText() != null
                ? binding.etUnit.getText().toString().trim() : "";
        String pointsStr = binding.etPoints.getText() != null
                ? binding.etPoints.getText().toString().trim() : "";

        // Validation
        if (title.isEmpty()) {
            binding.tilChallengeTitle.setError("Title is required");
            return;
        }
        if (description.isEmpty()) {
            binding.tilDescription.setError("Description is required");
            return;
        }
        if (selectedActivityType == null) {
            Toast.makeText(requireContext(), "Select an activity type", Toast.LENGTH_SHORT).show();
            return;
        }
        if (goalStr.isEmpty()) {
            binding.tilGoal.setError("Goal is required");
            return;
        }
        if (startDateMillis == null || endDateMillis == null) {
            Toast.makeText(requireContext(), "Select start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDateMillis <= startDateMillis) {
            Toast.makeText(requireContext(), "End date must be after start date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double goal;
        try {
            goal = Double.parseDouble(goalStr);
        } catch (NumberFormatException e) {
            binding.tilGoal.setError("Invalid number");
            return;
        }

        int points = 100; // default
        if (!pointsStr.isEmpty()) {
            try {
                points = Integer.parseInt(pointsStr);
            } catch (NumberFormatException e) {
                binding.tilPoints.setError("Invalid number");
                return;
            }
        }

        Challenge challenge = new Challenge();
        challenge.setTitle(title);
        challenge.setDescription(description);
        challenge.setActivityType(selectedActivityType);
        challenge.setGoalQuantity(goal);
        challenge.setUnit(unit);
        challenge.setStartDate(new Timestamp(new Date(startDateMillis)));
        challenge.setEndDate(new Timestamp(new Date(endDateMillis)));
        challenge.setPointsReward(points);
        challenge.setStatus("active");

        viewModel.createChallenge(challenge);
    }

    private void observeViewModel() {
        viewModel.getIsCreating().observe(getViewLifecycleOwner(), creating -> {
            binding.btnPublish.setEnabled(!creating);
            binding.progressLoading.setVisibility(creating ? View.VISIBLE : View.GONE);
        });

        viewModel.getCreateResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty()) {
                Toast.makeText(requireContext(), "Challenge created!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
