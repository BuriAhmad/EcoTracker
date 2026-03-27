package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.controller.ActivityController;
import com.ecotrack.app.model.ActivityLog;
import com.ecotrack.app.model.ConversionFactor;
import com.ecotrack.app.util.ImpactCalculator;
import com.ecotrack.app.util.ViewState;

import java.util.List;

/**
 * UI state management for the Activity Logging screen.
 */
public class ActivityViewModel extends ViewModel {

    private final ActivityController controller;

    private final MutableLiveData<String> selectedActivityType = new MutableLiveData<>();
    private final MutableLiveData<Double> quantity = new MutableLiveData<>(1.0);
    private final MutableLiveData<ImpactCalculator.ImpactResult> impactPreview = new MutableLiveData<>();
    private final MutableLiveData<ViewState<ActivityLog>> logResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> todayLogCount = new MutableLiveData<>(0);
    private final MutableLiveData<List<ConversionFactor>> conversionFactors = new MutableLiveData<>();
    private final MutableLiveData<String> selectedUnit = new MutableLiveData<>("");

    // Cache factors locally so impact preview doesn't need network each time
    private List<ConversionFactor> cachedFactors;

    public ActivityViewModel() {
        this.controller = new ActivityController();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<String> getSelectedActivityType() { return selectedActivityType; }
    public LiveData<Double> getQuantity() { return quantity; }
    public LiveData<ImpactCalculator.ImpactResult> getImpactPreview() { return impactPreview; }
    public LiveData<ViewState<ActivityLog>> getLogResult() { return logResult; }
    public LiveData<Integer> getTodayLogCount() { return todayLogCount; }
    public LiveData<List<ConversionFactor>> getConversionFactors() { return conversionFactors; }
    public LiveData<String> getSelectedUnit() { return selectedUnit; }

    // ── Actions ──────────────────────────────────────────────────────────

    /**
     * Load all conversion factors for the activity grid.
     */
    public void loadConversionFactors() {
        controller.getConversionFactors(new ActivityController.DataCallback<List<ConversionFactor>>() {
            @Override
            public void onSuccess(List<ConversionFactor> data) {
                cachedFactors = data;
                conversionFactors.setValue(data);
            }

            @Override
            public void onError(String message) {
                // Silently fail — grid will use hardcoded defaults
            }
        });
    }

    /**
     * Select an activity type. Triggers unit update and impact preview.
     */
    public void selectActivity(String activityType) {
        selectedActivityType.setValue(activityType);

        // Update unit label from cached factors
        if (cachedFactors != null) {
            for (ConversionFactor f : cachedFactors) {
                if (f.getActivityType().equals(activityType)) {
                    selectedUnit.setValue(f.getUnit());
                    break;
                }
            }
        }

        // Reset quantity and update preview
        quantity.setValue(1.0);
        updateImpactPreview();
    }

    /**
     * Set quantity and update impact preview.
     */
    public void setQuantity(double qty) {
        if (qty < 0) qty = 0;
        quantity.setValue(qty);
        updateImpactPreview();
    }

    /**
     * Increment quantity by the given amount.
     */
    public void incrementQuantity(double amount) {
        Double current = quantity.getValue();
        double newQty = (current != null ? current : 0) + amount;
        if (newQty < 0) newQty = 0;
        quantity.setValue(newQty);
        updateImpactPreview();
    }

    /**
     * Submit the activity log.
     */
    public void submitLog() {
        String type = selectedActivityType.getValue();
        Double qty = quantity.getValue();

        if (type == null || type.isEmpty()) {
            logResult.setValue(ViewState.error("Please select an activity type"));
            return;
        }
        if (qty == null || qty <= 0) {
            logResult.setValue(ViewState.error("Please enter a valid quantity"));
            return;
        }

        logResult.setValue(ViewState.loading());

        controller.logActivity(type, qty, null, new ActivityController.LogCallback() {
            @Override
            public void onSuccess(ActivityLog log) {
                logResult.setValue(ViewState.success(log));
                loadTodayLogCount(); // Refresh count
            }

            @Override
            public void onError(String message) {
                logResult.setValue(ViewState.error(message));
            }
        });
    }

    /**
     * Load today's log count.
     */
    public void loadTodayLogCount() {
        controller.getTodayLogCount(new ActivityController.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                todayLogCount.setValue(data);
            }

            @Override
            public void onError(String message) {
                // Silently fail
            }
        });
    }

    /**
     * Reset the form after a successful log.
     */
    public void resetForm() {
        selectedActivityType.setValue(null);
        quantity.setValue(1.0);
        impactPreview.setValue(null);
        logResult.setValue(null);
        selectedUnit.setValue("");
    }

    // ── Private Helpers ──────────────────────────────────────────────────

    private void updateImpactPreview() {
        String type = selectedActivityType.getValue();
        Double qty = quantity.getValue();

        if (type == null || qty == null || qty <= 0) {
            impactPreview.setValue(new ImpactCalculator.ImpactResult(0, 0, 0, 0));
            return;
        }

        // Use cached factor for instant local calculation
        if (cachedFactors != null) {
            for (ConversionFactor f : cachedFactors) {
                if (f.getActivityType().equals(type)) {
                    ImpactCalculator.ImpactResult result =
                            ImpactCalculator.calculateImpact(qty, f);
                    impactPreview.setValue(result);
                    return;
                }
            }
        }

        // Fallback: fetch from Firestore
        controller.getImpactPreview(type, qty, new ActivityController.DataCallback<ImpactCalculator.ImpactResult>() {
            @Override
            public void onSuccess(ImpactCalculator.ImpactResult data) {
                impactPreview.setValue(data);
            }

            @Override
            public void onError(String message) {
                // Silently fail
            }
        });
    }
}
