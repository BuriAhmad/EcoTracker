package com.ecotrack.app.view.challenges;

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

import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.Constants;
import com.ecotrack.app.viewmodel.ChallengeViewModel;
import com.example.saturn.R;
import com.example.saturn.databinding.FragmentChallengesBinding;

/**
 * Challenges list — active, available, and completed challenges.
 */
public class ChallengesFragment extends Fragment
        implements ChallengeAdapter.ChallengeClickListener {

    private FragmentChallengesBinding binding;
    private ChallengeViewModel viewModel;
    private ChallengeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChallengesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChallengeViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeViewModel();
        checkAdminVisibility();

        viewModel.loadChallenges();
    }

    private void setupRecyclerView() {
        adapter = new ChallengeAdapter(this);
        binding.recyclerChallenges.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerChallenges.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnCreate.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_challenges_to_create));
    }

    private void checkAdminVisibility() {
        // Show create button only for admins
        String role = requireContext()
                .getSharedPreferences(Constants.PREFS_NAME, 0)
                .getString(Constants.PREF_USER_ROLE, Constants.ROLE_STUDENT);
        binding.btnCreate.setVisibility(
                Constants.ROLE_ADMIN.equals(role) ? View.VISIBLE : View.GONE);
    }

    private void observeViewModel() {
        viewModel.getChallenges().observe(getViewLifecycleOwner(), challenges -> {
            adapter.setChallenges(challenges);
            boolean empty = challenges == null || challenges.isEmpty();
            binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerChallenges.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    // ── ChallengeAdapter callbacks ───────────────────────────────────────

    @Override
    public void onChallengeClick(String challengeId) {
        Bundle args = new Bundle();
        args.putString("challengeId", challengeId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_challenges_to_detail, args);
    }

    @Override
    public void onJoinClick(String challengeId) {
        // Quick-join from the list; ViewModel will handle it
        viewModel.loadChallengeDetail(challengeId);
        viewModel.joinChallenge(challengeId);
        // Refresh the list after a short delay to reflect new state
        binding.getRoot().postDelayed(() -> viewModel.loadChallenges(), 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
