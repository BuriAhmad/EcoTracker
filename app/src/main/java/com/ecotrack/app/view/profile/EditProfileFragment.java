package com.ecotrack.app.view.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.ecotrack.app.model.User;
import com.ecotrack.app.util.ImageUtils;
import com.ecotrack.app.viewmodel.ProfileViewModel;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentEditProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * Edit profile form — update name, department, avatar, privacy toggles.
 * Delete account with confirmation dialog.
 */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private ProfileViewModel viewModel;

    private static final String[] DEPARTMENTS = {
            "Computer Science", "Environmental Sciences", "Engineering",
            "Business", "Arts & Humanities", "Social Sciences",
            "Natural Sciences", "Other"
    };

    /** Gallery picker launcher. */
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleAvatarSelected(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Share ViewModel with ProfileFragment for profile data
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        setupDepartmentDropdown();
        setupActions();
        observeViewModel();

        viewModel.loadProfile();
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupDepartmentDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                DEPARTMENTS);
        binding.actvDepartment.setAdapter(adapter);
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        binding.avatarContainer.setOnClickListener(v ->
                galleryLauncher.launch("image/*"));

        binding.btnSave.setOnClickListener(v -> saveProfile());

        binding.btnNotifications.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.notificationSettingsFragment));

        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmation());
    }

    // ── Observe ──────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), this::populateForm);

        viewModel.getIsUpdating().observe(getViewLifecycleOwner(), updating -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(updating) ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!Boolean.TRUE.equals(updating));
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Snackbar.make(requireView(), "✅ Profile updated", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getResources().getColor(R.color.bg_card, null))
                        .setTextColor(getResources().getColor(R.color.accent_green, null))
                        .show();
            }
        });

        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_edit_to_login);
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

    // ── Populate form with current user data ─────────────────────────────

    private void populateForm(User user) {
        if (user == null) return;

        binding.etDisplayName.setText(user.getDisplayName());
        binding.actvDepartment.setText(user.getDepartment(), false);
        binding.switchAnonymous.setChecked(user.isAnonymousOnFeed());
        binding.switchLeaderboard.setChecked(user.isShowOnLeaderboard());

        // Load avatar if available
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.bg_avatar_circle)
                    .into(binding.ivAvatar);
        }
    }

    // ── Save ─────────────────────────────────────────────────────────────

    private void saveProfile() {
        String name = binding.etDisplayName.getText() != null
                ? binding.etDisplayName.getText().toString().trim() : "";
        String department = binding.actvDepartment.getText() != null
                ? binding.actvDepartment.getText().toString().trim() : "";
        boolean anonymous = binding.switchAnonymous.isChecked();
        boolean leaderboard = binding.switchLeaderboard.isChecked();

        if (name.isEmpty()) {
            binding.tilDisplayName.setError("Name is required");
            return;
        }
        binding.tilDisplayName.setError(null);

        viewModel.updateProfile(name, department, anonymous, leaderboard);
    }

    // ── Avatar Upload ────────────────────────────────────────────────────

    private void handleAvatarSelected(Uri imageUri) {
        // Show preview immediately
        Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(binding.ivAvatar);

        // Compress and upload
        byte[] compressed = ImageUtils.compressImage(requireContext(), imageUri);
        if (compressed != null) {
            viewModel.uploadAvatar(compressed);
        } else {
            Snackbar.make(requireView(), "Failed to process image", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(R.color.bg_card, null))
                    .setTextColor(getResources().getColor(R.color.color_error, null))
                    .show();
        }
    }

    // ── Delete Account ───────────────────────────────────────────────────

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This will permanently delete your account and all data. This cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteAccount())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
