package com.ecotrack.app.view.social;

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
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.viewmodel.FeedViewModel;
import com.example.saturn.R;
import com.example.saturn.databinding.FragmentFeedBinding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Social feed — campus-wide activity feed with reactions and infinite scroll.
 */
public class FeedFragment extends Fragment implements FeedItemAdapter.ReactionListener {

    private FragmentFeedBinding binding;
    private FeedViewModel viewModel;
    private FeedItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        setupRecyclerView();
        setupSwipeRefresh();
        setupListeners();
        observeViewModel();

        viewModel.loadFeed();
        viewModel.loadTodayCount();
    }

    private void setupRecyclerView() {
        adapter = new FeedItemAdapter(this);
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        binding.recyclerFeed.setLayoutManager(lm);
        binding.recyclerFeed.setAdapter(adapter);

        // Infinite scroll
        binding.recyclerFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // scrolling down
                    int totalItemCount = lm.getItemCount();
                    int lastVisible = lm.findLastVisibleItemPosition();
                    if (lastVisible >= totalItemCount - 3 && viewModel.hasMoreData()
                            && !viewModel.isLoadingMore()) {
                        viewModel.loadMoreFeed();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.accent_green);
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_card);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshFeed());
    }

    private void setupListeners() {
        binding.btnSearch.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_feed_to_search));
    }

    private void observeViewModel() {
        viewModel.getFeedItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
            boolean empty = items == null || items.isEmpty();
            binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerFeed.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsRefreshing().observe(getViewLifecycleOwner(), refreshing -> {
            binding.swipeRefresh.setRefreshing(refreshing);
        });

        viewModel.getTodayCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvLiveBanner.setText(
                    String.format(Locale.US, "🌿 %d activities logged today", count));
        });

        viewModel.getUserReactions().observe(getViewLifecycleOwner(), reactions -> {
            // Cast is safe: HashSet<String> is a Set<String>
            @SuppressWarnings("unchecked")
            Map<String, Set<String>> castReactions = (Map<String, Set<String>>) (Map<?, ?>) reactions;
            adapter.setUserReactions(castReactions);
        });
    }

    @Override
    public void onReaction(String feedItemId, String emoji) {
        viewModel.toggleReaction(feedItemId, emoji);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
