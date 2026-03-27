package com.ecotrack.app.view.auth;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentLoginBinding;
import com.ecotrack.app.util.ViewState;
import com.ecotrack.app.viewmodel.AuthViewModel;

/**
 * Login screen — email/password authentication via Firebase Auth.
 * Observes AuthViewModel.loginState for Loading/Success/Error states.
 */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupRegisterLinkText();
        setupClickListeners();
        observeLoginState();
    }

    /**
     * Style the "Create one" portion of the register link in accent green.
     */
    private void setupRegisterLinkText() {
        String full = "Don't have an account? Create one";
        SpannableString spannable = new SpannableString(full);
        int start = full.indexOf("Create one");
        if (start >= 0) {
            int color = ContextCompat.getColor(requireContext(), R.color.accent_green);
            spannable.setSpan(new ForegroundColorSpan(color),
                    start, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.tvRegisterLink.setText(spannable);
    }

    private void setupClickListeners() {
        // Sign In button
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Navigate to Register
        binding.tvRegisterLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_register));
    }

    private void attemptLogin() {
        // Clear previous errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        String email = binding.etEmail.getText() != null
                ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null
                ? binding.etPassword.getText().toString() : "";

        viewModel.login(email, password);
    }

    private void observeLoginState() {
        viewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            switch (state.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;

                case SUCCESS:
                    setLoading(false);
                    // Navigate to Home, clearing auth screens from back stack
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_login_to_home);
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
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? "" : "Sign In");
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
    }

    private void showError(String message) {
        if (message == null) return;

        // Route error to the appropriate field
        String lower = message.toLowerCase();
        if (lower.contains("email") || lower.contains("account")) {
            binding.tilEmail.setError(message);
        } else if (lower.contains("password")) {
            binding.tilPassword.setError(message);
        } else {
            // Generic error — show on email field
            binding.tilEmail.setError(message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
