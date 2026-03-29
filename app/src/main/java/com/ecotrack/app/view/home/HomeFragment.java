package com.ecotrack.app.view.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentHomeBinding;
import com.ecotrack.app.view.dashboard.RecentLogAdapter;
import com.ecotrack.app.viewmodel.HomeViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Home/landing screen — eco-score hero, quick stats, campus impact, weekly chart,
 * recent activity, and quick-action buttons.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private RecentLogAdapter recentLogAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerView();
        setupChart();
        setupClickListeners();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadHome();
    }

    // ── RecyclerView ─────────────────────────────────────────────────────

    private void setupRecyclerView() {
        recentLogAdapter = new RecentLogAdapter();
        binding.rvRecentLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentLogs.setAdapter(recentLogAdapter);
    }

    // ── Bar Chart (identical dark-theme config as Dashboard) ─────────────

    private void setupChart() {
        binding.chartWeekly.setBackgroundColor(Color.TRANSPARENT);
        binding.chartWeekly.getDescription().setEnabled(false);
        binding.chartWeekly.getLegend().setEnabled(false);
        binding.chartWeekly.setDrawGridBackground(false);
        binding.chartWeekly.setDrawBorders(false);
        binding.chartWeekly.setTouchEnabled(false);
        binding.chartWeekly.setDragEnabled(false);
        binding.chartWeekly.setScaleEnabled(false);
        binding.chartWeekly.setExtraBottomOffset(8f);

        // X axis
        XAxis xAxis = binding.chartWeekly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));
        xAxis.setGranularity(1f);

        // Y axis – left only
        binding.chartWeekly.getAxisLeft().setDrawGridLines(true);
        binding.chartWeekly.getAxisLeft().setGridColor(
                ContextCompat.getColor(requireContext(), R.color.chart_grid_line));
        binding.chartWeekly.getAxisLeft().setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_muted));
        binding.chartWeekly.getAxisLeft().setAxisMinimum(0f);
        binding.chartWeekly.getAxisRight().setEnabled(false);
    }

    // ── Click Listeners ──────────────────────────────────────────────────

    private void setupClickListeners() {
        // Notification bell → notification settings
        binding.btnNotifications.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_home_to_notifications));

        // "View All" recent logs → dashboard
        binding.tvViewAll.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_home_to_dashboard));

        // Campus Feed card → feed
        binding.cardFeed.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_home_to_feed));

        // Challenges card → challenges
        binding.cardChallenges.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_home_to_challenges));
    }

    // ── ViewModel Observers ──────────────────────────────────────────────

    private void observeViewModel() {
        // Greeting
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getDisplayName() != null) {
                binding.tvGreeting.setText(String.format("Hello, %s 👋", user.getDisplayName()));
            }
        });

        // Eco-score ring
        viewModel.getEcoScore().observe(getViewLifecycleOwner(), score ->
                binding.ecoScoreRing.setScore(score));

        viewModel.getEcoLevel().observe(getViewLifecycleOwner(), level ->
                binding.tvEcoLevel.setText(level));

        // Quick stats row
        viewModel.getTotalPoints().observe(getViewLifecycleOwner(), pts ->
                binding.tvStatPoints.setText(String.valueOf(pts)));

        viewModel.getUserStreak().observe(getViewLifecycleOwner(), streak ->
                binding.tvStatStreak.setText(String.format(Locale.US, "🔥 %d", streak)));

        viewModel.getTotalCo2().observe(getViewLifecycleOwner(), co2 ->
                binding.tvStatCo2.setText(String.format(Locale.US, "%.1f", co2)));

        viewModel.getActivityCount().observe(getViewLifecycleOwner(), count ->
                binding.tvStatActivities.setText(String.valueOf(count)));

        // Campus impact card
        viewModel.getCampusStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            binding.tvCampusCo2.setText(String.format(Locale.US, "%.1f", stats.getTotalCo2Saved()));
            binding.tvCampusWater.setText(String.format(Locale.US, "%.1f", stats.getTotalWaterSaved()));
            binding.tvCampusWaste.setText(String.format(Locale.US, "%.1f", stats.getTotalWasteDiverted()));
            binding.tvCampusUsers.setText(String.valueOf(stats.getTotalUsers()));
        });

        // Weekly chart
        viewModel.getWeeklyData().observe(getViewLifecycleOwner(), data -> {
            if (data == null) return;
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                entries.add(new BarEntry(i, data[i]));
            }
            BarDataSet dataSet = new BarDataSet(entries, "Points");
            dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.accent_green));
            dataSet.setDrawValues(false);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            binding.chartWeekly.setData(barData);
            binding.chartWeekly.invalidate();
        });

        // Recent logs
        viewModel.getRecentLogs().observe(getViewLifecycleOwner(), logs ->
                recentLogAdapter.setLogs(logs));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
