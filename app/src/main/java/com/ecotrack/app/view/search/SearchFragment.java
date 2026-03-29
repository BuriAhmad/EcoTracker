package com.ecotrack.app.view.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.saturn.databinding.FragmentSearchBinding;
import com.ecotrack.app.viewmodel.SearchViewModel;

/**
 * Search screen — search bar with debounce, filter chips, results RecyclerView.
 */
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private SearchResultAdapter adapter;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        setupRecyclerView();
        setupSearchBar();
        setupChips();
        setupButtons();
        observeViewModel();
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new SearchResultAdapter(new SearchResultAdapter.OnSearchItemClickListener() {
            @Override
            public void onChallengeClick(String challengeId) {
                Bundle args = new Bundle();
                args.putString("challengeId", challengeId);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_search_to_challenge_detail, args);
            }

            @Override
            public void onTeamClick(String teamId) {
                Bundle args = new Bundle();
                args.putString("teamId", teamId);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_search_to_team_detail, args);
            }
        });
        binding.rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvResults.setAdapter(adapter);
    }

    private void setupSearchBar() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (debounceRunnable != null) {
                    debounceHandler.removeCallbacks(debounceRunnable);
                }
                debounceRunnable = () -> {
                    String query = s.toString().trim();
                    viewModel.search(query);
                };
                debounceHandler.postDelayed(debounceRunnable, 400);
            }
        });
    }

    private void setupChips() {
        binding.chipAll.setOnClickListener(v -> {
            viewModel.setFilter("all");
            triggerSearch();
        });
        binding.chipChallenges.setOnClickListener(v -> {
            viewModel.setFilter("challenges");
            triggerSearch();
        });
        binding.chipTeams.setOnClickListener(v -> {
            viewModel.setFilter("teams");
            triggerSearch();
        });
    }

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
    }

    private void triggerSearch() {
        if (binding.etSearch.getText() != null) {
            viewModel.search(binding.etSearch.getText().toString().trim());
        }
    }

    // ── Observers ────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getChallengeResults().observe(getViewLifecycleOwner(), challenges -> {
            adapter.setResults(challenges, viewModel.getTeamResults().getValue());
            updateEmptyState();
        });

        viewModel.getTeamResults().observe(getViewLifecycleOwner(), teams -> {
            adapter.setResults(viewModel.getChallengeResults().getValue(), teams);
            updateEmptyState();
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(
                    Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });
    }

    private void updateEmptyState() {
        int total = adapter.getItemCount();
        String query = binding.etSearch.getText() != null
                ? binding.etSearch.getText().toString().trim() : "";

        if (query.isEmpty()) {
            binding.tvEmpty.setText("Search for challenges and teams");
            binding.tvEmpty.setVisibility(View.VISIBLE);
        } else if (total == 0) {
            binding.tvEmpty.setText("No results found for \"" + query + "\"");
            binding.tvEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
        binding = null;
    }
}
