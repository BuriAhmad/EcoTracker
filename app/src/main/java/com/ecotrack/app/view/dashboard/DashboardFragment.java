package com.ecotrack.app.view.dashboard;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentDashboardBinding;
import com.ecotrack.app.util.EquivalencyTranslator;
import com.ecotrack.app.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Personal dashboard — eco-score ring, weekly chart, stats, heatmap, equivalencies.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private RecentLogAdapter recentLogAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupRecyclerView();
        setupChart();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadDashboard();
    }

    private void setupRecyclerView() {
        recentLogAdapter = new RecentLogAdapter();
        binding.rvRecentLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentLogs.setAdapter(recentLogAdapter);
    }

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

        // Y axis
        binding.chartWeekly.getAxisLeft().setDrawGridLines(true);
        binding.chartWeekly.getAxisLeft().setGridColor(
                ContextCompat.getColor(requireContext(), R.color.chart_grid_line));
        binding.chartWeekly.getAxisLeft().setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_muted));
        binding.chartWeekly.getAxisLeft().setAxisMinimum(0f);
        binding.chartWeekly.getAxisRight().setEnabled(false);
    }

    private void observeViewModel() {
        // Eco-score ring
        viewModel.getEcoScore().observe(getViewLifecycleOwner(), score ->
                binding.ecoScoreRing.setScore(score));

        viewModel.getEcoLevel().observe(getViewLifecycleOwner(), level ->
                binding.tvEcoLevel.setText(level));

        // Impact stats
        viewModel.getTotalCo2().observe(getViewLifecycleOwner(), co2 ->
                binding.tvStatCo2.setText(String.format(Locale.US, "%.1f", co2)));

        viewModel.getTotalWater().observe(getViewLifecycleOwner(), water ->
                binding.tvStatWater.setText(String.format(Locale.US, "%.1f", water)));

        viewModel.getTotalWaste().observe(getViewLifecycleOwner(), waste ->
                binding.tvStatWaste.setText(String.format(Locale.US, "%.1f", waste)));

        // Equivalencies
        viewModel.getEquivalencies().observe(getViewLifecycleOwner(), equivs -> {
            if (equivs != null && equivs.size() >= 3) {
                binding.tvEquivTrees.setText(equivs.get(0).getDescription());
                binding.tvEquivMiles.setText(equivs.get(1).getDescription());
                binding.tvEquivHomes.setText(equivs.get(2).getDescription());
            }
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

        // Heatmap
        viewModel.getHeatmapData().observe(getViewLifecycleOwner(), map ->
                binding.heatmapView.setData(map));

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
