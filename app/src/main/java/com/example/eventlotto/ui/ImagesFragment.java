package com.example.eventlotto.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.R;

/**
 * A fragment that displays a collection of images.
 * <p>
 * This fragment inflates the {@link R.layout#fragment_images} layout, which can be used to show image content in the app.
 * </p>
 */
public class ImagesFragment extends Fragment {
    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, the fragment is being re-constructed from a previous saved state.
     * @return The root {@link View} of the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_images, container, false);
    }
}

