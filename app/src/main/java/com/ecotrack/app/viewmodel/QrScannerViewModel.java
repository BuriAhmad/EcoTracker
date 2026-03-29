package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.model.QrScanResult;

/**
 * UI state for the QR Scanner screen.
 */
public class QrScannerViewModel extends ViewModel {

    private final MutableLiveData<QrScanResult> scanResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTorchOn = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Prevent duplicate scans
    private boolean isProcessing = false;

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<QrScanResult> getScanResult() { return scanResult; }
    public LiveData<Boolean> getIsTorchOn() { return isTorchOn; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // ── Actions ──────────────────────────────────────────────────────────

    public void onQrCodeDetected(String rawValue) {
        if (isProcessing) return;
        isProcessing = true;

        try {
            // Parse the QR JSON
            org.json.JSONObject json = new org.json.JSONObject(rawValue);
            QrScanResult result = new QrScanResult(
                    json.optString("locationId", ""),
                    json.optString("locationName", "Unknown Location"),
                    json.optString("activityType", ""),
                    json.optInt("quantity", 1)
            );
            result.setSuccess(true);
            scanResult.postValue(result);
        } catch (org.json.JSONException e) {
            QrScanResult error = new QrScanResult();
            error.setSuccess(false);
            scanResult.postValue(error);
            errorMessage.postValue("Invalid QR code format");
            isProcessing = false;
        }
    }

    public void toggleTorch() {
        Boolean current = isTorchOn.getValue();
        isTorchOn.setValue(current != null && !current);
    }

    public void resetScan() {
        isProcessing = false;
        scanResult.setValue(null);
    }
}
