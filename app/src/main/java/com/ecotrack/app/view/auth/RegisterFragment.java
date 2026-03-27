package com.ecotrack.app.view.auth;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentRegisterBinding;
import com.ecotrack.app.util.ViewState;
import com.ecotrack.app.viewmodel.AuthViewModel;

/**
 * Registration screen — name, email, department dropdown, password.
 * Observes AuthViewModel.registerState for Loading/Success/Error states.
 */
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    private static final String[] DEPARTMENTS = {
            "Computer Science",
            "Environmental Sciences",
            "Engineering",
            "Business",
            "Arts & Humanities",
            "Social Sciences",
            "Natural Sciences",
            "Other"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupDepartmentDropdown();
        setupLoginLinkText();
        setupClickListeners();
        observeRegisterState();
    }

    private void setupDepartmentDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                DEPARTMENTS
        );
        binding.dropdownDepartment.setAdapter(adapter);
    }

    /**
     * Style the "Sign In" portion of the login link in accent green.
     */
    private void setupLoginLinkText() {
        String full = "Already have an account? Sign In";
        SpannableString spannable = new SpannableString(full);
        int start = full.indexOf("Sign In");
        if (start >= 0) {
            int color = ContextCompat.getColor(requireContext(), R.color.accent_green);
            spannable.setSpan(new ForegroundColorSpan(color),
                    start, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.tvLoginLink.setText(spannable);
    }

    private void setupClickListeners() {
        // Create Account button
        binding.btnRegister.setOnClickListener(v -> attemptRegister());

        // Navigate back to Login
        binding.tvLoginLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_register_to_login));
    }

    private void attemptRegister() {
        // Clear previous errors
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilDepartment.setError(null);
        binding.tilPassword.setError(null);

        String name = binding.etName.getText() != null
                ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null
                ? binding.etEmail.getText().toString().trim() : "";
        String department = binding.dropdownDepartment.getText() != null
                ? binding.dropdownDepartment.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null
                ? binding.etPassword.getText().toString() : "";

        viewModel.register(name, email, department, password);
    }

    private void observeRegisterState() {
        viewModel.getRegisterState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            switch (state.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;

                case SUCCESS:
                    setLoading(false);
                    // Navigate to Home, clearing auth screens from back stack
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_register_to_home);
                    break;

                case ERROR:
                    setLoading(false);
                    showError(state.getMessage());
                    break;

                case EMPTY:
                    setLoading(false);
                    break;
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnRegister.setText(isLoading ? "" : "Create Account");
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.etName.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.dropdownDepartment.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
    }

    private void showError(String message) {
        if (message == null) return;

        String lower = message.toLowerCase();
        if (lower.contains("name")) {
            binding.tilName.setError(message);
        } else if (lower.contains("email") || lower.contains("account")) {
            binding.tilEmail.setError(message);
        } else if (lower.contains("department")) {
            binding.tilDepartment.setError(message);
        } else if (lower.contains("password")) {
            binding.tilPassword.setError(message);
        } else {
            binding.tilEmail.setError(message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
