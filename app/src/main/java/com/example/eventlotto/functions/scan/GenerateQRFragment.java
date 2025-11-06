package com.example.eventlotto.scan;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventlotto.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

// This class was developed in conjunction with OpenAI, ChatGPT, "How to generate a QR Code based on an id number in Android Studio"
public class GenerateQRFragment extends DialogFragment {

    private String eventId;

    /**
     * Creates a new instance of GenerateQRFragment with the given event ID.
     *
     * @param eventId The unique ID of the event to encode in the QR code
     * @return A new GenerateQRFragment with the event ID set as an argument
     */
    public static GenerateQRFragment newInstance(String eventId) {
        GenerateQRFragment fragment = new GenerateQRFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates the QR code generation layout and displays a QR code
     * based on the provided event ID.
     *
     * @param inflater The LayoutInflater used to create the fragment layout
     * @param container The parent view that the fragment’s UI is attached to
     * @param savedInstanceState The previously saved instance state, if any
     * @return The root view for the fragment’s layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_generate_qr, container, false);
        ImageView qrImage = view.findViewById(R.id.qrImage);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");

            try {
                String qrData = eventId;

                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 600, 600);
                qrImage.setImageBitmap(bitmap);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        return view;
    }
}