package com.example.eventlotto.functions.scan;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventlotto.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * This class was developed in conjunction with OpenAI, ChatGPT.
 * It generates and displays a QR code based on an event ID,
 * and allows the user to download it to the device gallery.
 */
public class GenerateQRFragment extends DialogFragment {

    private String eventId;
    private Bitmap qrBitmap;

    public static GenerateQRFragment newInstance(String eventId) {
        GenerateQRFragment fragment = new GenerateQRFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_generate_qr, container, false);
        ImageView qrImage = view.findViewById(R.id.qrImage);
        Button downloadButton = view.findViewById(R.id.download_qr_button);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");

            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                qrBitmap = barcodeEncoder.encodeBitmap(eventId, BarcodeFormat.QR_CODE, 600, 600);
                qrImage.setImageBitmap(qrBitmap);

            } catch (WriterException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error generating QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        // Handle QR download
        downloadButton.setOnClickListener(v -> saveQRCodeToGallery(qrBitmap));

        return view;
    }

    /**
     * Saves the generated QR code to the device’s gallery.
     */
    private void saveQRCodeToGallery(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(getContext(), "No QR code to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = "event_qr_" + System.currentTimeMillis() + ".png";

        try {
            OutputStream fos;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ uses MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EventLotto");

                Uri imageUri = requireContext().getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = requireContext().getContentResolver().openOutputStream(imageUri);
            } else {
                // Older devices: write to external storage
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "EventLotto");
                if (!dir.exists()) dir.mkdirs();

                File image = new File(dir, filename);
                fos = new FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Toast.makeText(getContext(), "QR saved to gallery!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error saving QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
