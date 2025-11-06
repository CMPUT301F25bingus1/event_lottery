package com.example.eventlotto.ui.entrant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.eventlotto.R;
import com.example.eventlotto.functions.scan.GenerateQRFragment;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

// Class developed in conjunction with OpenAI, ChatGPT, "How to implement QR code reader in Android Studio"
public class Ent_ScanFragment extends Fragment implements BarcodeCallback {

    private DecoratedBarcodeView barcodeView;
    private boolean scanned = false;

    /**
     * Creates and returns the view for the ScanFragment.
     * <p>
     * This method creates the layout, sets up the barcode scanner view.
     * It then checks for camera permission and starts the scanner if granted,
     * or requests permission otherwise before starting.
     *
     * @param inflater  The LayoutInflater used to inflate the fragment's layout
     * @param container The parent view that the fragment's UI will be attached to
     * @param savedInstanceState The saved instance state, if any
     * @return The root view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        barcodeView = view.findViewById(R.id.barcode_scanner);

        // temp QR display button
        Button showQrButton = view.findViewById(R.id.showQrButton);
        showQrButton.setOnClickListener(v -> {
            // replace id later
            String eventId = "test_event_001";

            GenerateQRFragment qrFragment = GenerateQRFragment.newInstance(eventId);
            qrFragment.show(getParentFragmentManager(), "generate_qr_dialog");
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
        }

        return view;
    }

    /**
     * Starts the continuous barcode scanner.
     * <p>
     * This method resumes the camera preview to begin scanning QR codes.
     */
    private void startScanner() {
        barcodeView.decodeContinuous(this);
        barcodeView.resume();
    }

    /**
     * Handles the result of a scanned QR code.
     * <p>
     * If a valid QR code is detected and hasn’t been processed yet,
     * this method opens the {@link Ent_EventDetailsFragment} to display the event information
     *
     * @param result The scanned BarcodeResult result object
     */
    @Override
    public void barcodeResult(BarcodeResult result) {
        if (!scanned && result.getText() != null) {
            scanned = true;
            String eventId = result.getText();

            Ent_EventDetailsFragment dialog = Ent_EventDetailsFragment.newInstance(eventId);
            dialog.show(getParentFragmentManager(), "event_details_dialog");
        }
    }

    /**
     * Called after the fragment's view has been created.
     * <p>
     * Sets a listener for the "eventClosed" result, which resets the scan state
     * and resumes the barcode scanner when the event details dialog is dismissed.
     *
     * @param view The fragment's root view
     * @param savedInstanceState The saved instance state, if available
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("eventClosed", this, (requestKey, bundle) -> {
            scanned = false;
            if (barcodeView != null) {
                barcodeView.resume();
            }
        });
    }

    /**
     * Called when the fragment becomes visible and active.
     * <p>
     * Resumes the barcode scanner if it has been initialized.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null) barcodeView.resume();
    }

    /**
     * Called when the fragment is no longer in the foreground.
     * <p>
     * Pauses the barcode scanner.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) barcodeView.pause();
    }

    /**
     * Handles the result of a permission request.
     * <p>
     * This method checks if the camera permission request (request code {@code 1001})
     * was granted. If so, it immediately starts the QR scanner, otherwise, the scanner
     * remains inactive until permission is granted.
     * <p>
     * This method is necessary to force immediate QR scanner start after permission is granted.
     *
     * @param requestCode  The integer request code
     * @param permissions  The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        }
    }
}
