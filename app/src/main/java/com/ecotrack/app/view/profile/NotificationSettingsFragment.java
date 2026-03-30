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

import com.ecotrack.app.model.NotificationPreferences;
import com.ecotrack.app.viewmodel.NotificationViewModel;
import com.example.saturn.databinding.FragmentNotificationSettingsBinding;
import com.google.android.material.snackbar.Snackbar;

/**
 * Notification settings — toggle daily reminders, challenge updates, streak alerts,
 * campus milestones and badge unlocks.  Backed by SharedPreferences via
 * {@link NotificationViewModel}.
 */
public class NotificationSettingsFragment extends Fragment {

    private FragmentNotificationSettingsBinding binding;
    private NotificationViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        setupClickListeners();
        observeViewModel();

        viewModel.loadPreferences();
    }

    /* ── Click listeners ─────────────────────────────────────────── */

    private void setupClickListeners() {

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        /* Daily-reminder toggle shows / hides the time stepper */
        binding.switchDailyReminder.setOnCheckedChangeListener((btn, checked) -> {
            viewModel.setDailyReminderEnabled(checked);
            binding.layoutReminderTime.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        binding.btnTimeMinus.setOnClickListener(v -> viewModel.decrementReminderHour());
        binding.btnTimePlus.setOnClickListener(v -> viewModel.incrementReminderHour());

        binding.switchChallengeUpdates.setOnCheckedChangeListener((btn, checked) ->
                viewModel.setChallengeUpdates(checked));

        binding.switchStreakAlerts.setOnCheckedChangeListener((btn, checked) ->
                viewModel.setStreakAlerts(checked));

        binding.switchCampusMilestones.setOnCheckedChangeListener((btn, checked) ->
                viewModel.setCampusMilestones(checked));

        binding.switchBadgeUnlocks.setOnCheckedChangeListener((btn, checked) ->
                viewModel.setBadgeUnlocks(checked));

        binding.btnSave.setOnClickListener(v -> viewModel.savePreferences());
    }

    /* ── Observers ────────────────────────────────────────────────── */

    private void observeViewModel() {

        viewModel.getPreferences().observe(getViewLifecycleOwner(), this::populateToggles);

        viewModel.getSaveResult().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;
            if (success) {
                Snackbar.make(binding.getRoot(), "Settings saved ✓", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.getRoot(), "Failed to save settings", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /* ── Populate switches from preferences ──────────────────────── */

    private void populateToggles(NotificationPreferences prefs) {
        if (prefs == null) return;

        binding.switchDailyReminder.setChecked(prefs.isDailyReminderEnabled());
        binding.layoutReminderTime.setVisibility(
                prefs.isDailyReminderEnabled() ? View.VISIBLE : View.GONE);
        binding.tvReminderTime.setText(prefs.getFormattedReminderTime());

        binding.switchChallengeUpdates.setChecked(prefs.isChallengeUpdates());
        binding.switchStreakAlerts.setChecked(prefs.isStreakAlerts());
        binding.switchCampusMilestones.setChecked(prefs.isCampusMilestones());
        binding.switchBadgeUnlocks.setChecked(prefs.isBadgeUnlocks());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
