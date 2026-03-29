package com.ecotrack.app.view.logging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.saturn.R;
import com.example.saturn.databinding.FragmentQrScannerBinding;
import com.ecotrack.app.model.QrScanResult;
import com.ecotrack.app.viewmodel.QrScannerViewModel;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * QR code scanner — CameraX preview with ML Kit barcode scanning.
 * On scan: shows result overlay card. Confirm → navigate to LogActivityFragment
 * with pre-filled arguments.
 */
public class QrScannerFragment extends Fragment {

    private FragmentQrScannerBinding binding;
    private QrScannerViewModel viewModel;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            startCamera();
                        } else {
                            Navigation.findNavController(requireView()).popBackStack();
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQrScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QrScannerViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();

        setupButtons();
        observeViewModel();
        checkCameraPermission();
    }

    // ── Setup ────────────────────────────────────────────────────────────

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        binding.btnTorch.setOnClickListener(v -> viewModel.toggleTorch());

        binding.btnConfirm.setOnClickListener(v -> {
            QrScanResult result = viewModel.getScanResult().getValue();
            if (result != null && result.isSuccess()) {
                Bundle args = new Bundle();
                args.putString("activityType", result.getActivityType());
                args.putInt("quantity", result.getQuantity());
                args.putString("locationName", result.getLocationName());
                Navigation.findNavController(v)
                        .navigate(R.id.action_qr_to_log, args);
            }
        });

        binding.btnCancel.setOnClickListener(v -> {
            viewModel.resetScan();
            binding.cardResult.setVisibility(View.GONE);
            binding.tvInstruction.setVisibility(View.VISIBLE);
        });
    }

    // ── Camera Permission ────────────────────────────────────────────────

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // ── CameraX Setup ────────────────────────────────────────────────────

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());

        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCameraUseCases();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCameraUseCases() {
        if (cameraProvider == null || binding == null) return;

        cameraProvider.unbindAll();

        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        // Image analysis for barcode scanning
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // ML Kit barcode scanner
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            processImage(imageProxy, scanner);
        });

        // Bind to lifecycle
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        try {
            androidx.camera.core.Camera camera = cameraProvider.bindToLifecycle(
                    getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);

            // Observe torch state
            viewModel.getIsTorchOn().observe(getViewLifecycleOwner(), torchOn -> {
                if (camera.getCameraInfo().hasFlashUnit()) {
                    camera.getCameraControl().enableTorch(Boolean.TRUE.equals(torchOn));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImage(ImageProxy imageProxy, BarcodeScanner scanner) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        if (barcode.getRawValue() != null) {
                            viewModel.onQrCodeDetected(barcode.getRawValue());
                            break;
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    // ── Observers ────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getScanResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.isSuccess()) {
                binding.cardResult.setVisibility(View.VISIBLE);
                binding.tvInstruction.setVisibility(View.GONE);
                binding.tvResultTitle.setText("📍 " + result.getLocationName());
                binding.tvResultDetails.setText(String.format(Locale.US,
                        "Activity: %s · Quantity: %d",
                        result.getActivityType(), result.getQuantity()));
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && binding != null) {
                binding.tvInstruction.setText(msg);
                binding.tvInstruction.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        binding = null;
    }
}
