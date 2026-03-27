package com.ecotrack.app.view.logging;

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
import androidx.transition.TransitionManager;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentLogActivityBinding;
import com.ecotrack.app.util.ImpactCalculator;
import com.ecotrack.app.util.ViewState;
import com.ecotrack.app.viewmodel.ActivityViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity logging screen — 3×2 card grid for activity types,
 * quantity stepper, real-time impact preview, and submit button.
 */
public class LogActivityFragment extends Fragment {

    private FragmentLogActivityBinding binding;
    private ActivityViewModel viewModel;
    private ActivityCategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLogActivityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ActivityViewModel.class);

        setupActivityGrid();
        setupStepperButtons();
        setupSubmitButton();
        setupQrButton();
        observeViewModel();

        viewModel.loadConversionFactors();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadTodayLogCount();
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupActivityGrid() {
        adapter = new ActivityCategoryAdapter();
        binding.rvActivityGrid.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvActivityGrid.setAdapter(adapter);

        // Set hardcoded categories (icons + colors from resources)
        List<ActivityCategoryAdapter.ActivityCategory> categories = new ArrayList<>();
        categories.add(new ActivityCategoryAdapter.ActivityCategory(
                "biking", "Biking", R.drawable.ic_directions_bike_24, R.color.color_biking));
        categories.add(new ActivityCategoryAdapter.ActivityCategory(
                "walking", "Walking", R.drawable.ic_directions_walk_24, R.color.color_walking));
        categories.add(new ActivityCategoryAdapter.ActivityCategory(
                "recycling", "Recycling", R.drawable.ic_recycling_24, R.color.color_recycling));
        categories.add(new ActivityCategoryAdapter.ActivityCategory(
                "water_save", "Water Save", R.drawable.ic_water_drop_24, R.color.color_water_save));
        categories.add(new ActivityCategoryAdapter.ActivityCategory(
                "energy_saving", "Energy", R.drawable.ic_bolt_24, R.color.color_energy));
        categories.add(new ActivityCategoryAdapter.ActivityCategory(
                "plastic_free", "Plastic-Free", R.drawable.ic_eco_24, R.color.color_plastic_free));
        adapter.setCategories(categories);

        // Selection listener
        adapter.setOnCategorySelectedListener(category -> {
            viewModel.selectActivity(category.type);
            showQuantitySection(true);
        });
    }

    private void setupStepperButtons() {
        binding.btnPlus.setOnClickListener(v -> viewModel.incrementQuantity(1));
        binding.btnMinus.setOnClickListener(v -> viewModel.incrementQuantity(-1));

        // Long press for ±5
        binding.btnPlus.setOnLongClickListener(v -> {
            viewModel.incrementQuantity(5);
            return true;
        });
        binding.btnMinus.setOnLongClickListener(v -> {
            viewModel.incrementQuantity(-5);
            return true;
        });
    }

    private void setupSubmitButton() {
        binding.btnSubmit.setOnClickListener(v -> viewModel.submitLog());
    }

    private void setupQrButton() {
        binding.btnQrScan.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_log_to_qr));
    }

    // ── Observe ViewModel ────────────────────────────────────────────────

    private void observeViewModel() {
        // Quantity display
        viewModel.getQuantity().observe(getViewLifecycleOwner(), qty -> {
            if (qty != null) {
                // Show as integer if whole number, otherwise 1 decimal
                if (qty == Math.floor(qty) && !Double.isInfinite(qty)) {
                    binding.tvQuantity.setText(String.valueOf((int) Math.max(0, qty)));
                } else {
                    binding.tvQuantity.setText(String.format(Locale.US, "%.1f", Math.max(0, qty)));
                }
            }
        });

        // Unit label
        viewModel.getSelectedUnit().observe(getViewLifecycleOwner(), unit -> {
            if (unit != null && !unit.isEmpty()) {
                binding.tvUnit.setText(unit);
            }
        });

        // Impact preview
        viewModel.getImpactPreview().observe(getViewLifecycleOwner(), impact -> {
            if (impact != null && impact.hasImpact()) {
                binding.tvPreviewCo2.setText(
                        String.format(Locale.US, "🌿 %.2f kg CO₂ saved", impact.getCo2Saved()));
                binding.tvPreviewWater.setText(
                        String.format(Locale.US, "💧 %.1f L water saved", impact.getWaterSaved()));
                binding.tvPreviewWaste.setText(
                        String.format(Locale.US, "♻\uFE0F %.2f kg waste diverted", impact.getWasteDiverted()));
                binding.tvPreviewPoints.setText(
                        String.format(Locale.US, "⭐ +%d points", impact.getPointsEarned()));

                // Show/hide zero rows
                binding.tvPreviewCo2.setVisibility(impact.getCo2Saved() > 0 ? View.VISIBLE : View.GONE);
                binding.tvPreviewWater.setVisibility(impact.getWaterSaved() > 0 ? View.VISIBLE : View.GONE);
                binding.tvPreviewWaste.setVisibility(impact.getWasteDiverted() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        // Log result
        viewModel.getLogResult().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            switch (state.getStatus()) {
                case LOADING:
                    setSubmitLoading(true);
                    break;

                case SUCCESS:
                    setSubmitLoading(false);
                    Snackbar.make(requireView(),
                                    "✅ Activity logged! +" + state.getData().getPointsEarned() + " points",
                                    Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.bg_card, null))
                            .setTextColor(getResources().getColor(R.color.accent_green, null))
                            .show();
                    resetForm();
                    break;

                case ERROR:
                    setSubmitLoading(false);
                    Snackbar.make(requireView(), state.getMessage(), Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.bg_card, null))
                            .setTextColor(getResources().getColor(R.color.color_error, null))
                            .show();
                    break;

                case EMPTY:
                    setSubmitLoading(false);
                    break;
            }
        });

        // Daily log count
        viewModel.getTodayLogCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.tvDailyCount.setText(
                        String.format(Locale.US, "%d of 20 daily logs used", count));
            }
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private void showQuantitySection(boolean show) {
        TransitionManager.beginDelayedTransition(
                (ViewGroup) binding.getRoot().findViewById(R.id.layout_quantity).getParent());
        binding.layoutQuantity.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.cardImpactPreview.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(true);
        binding.tvDailyCount.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setSubmitLoading(boolean loading) {
        binding.btnSubmit.setEnabled(!loading);
        binding.btnSubmit.setText(loading ? "Logging…" : "Log Activity");
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void resetForm() {
        viewModel.resetForm();
        adapter.clearSelection();
        showQuantitySection(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
