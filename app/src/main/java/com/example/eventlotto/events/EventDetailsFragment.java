package com.example.eventlotto.events;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.R;
import com.example.eventlotto.ui.ScanFragment;

public class EventDetailsFragment extends DialogFragment {

    private String eventId;

    /**
     * Creates a new instance of EventDetailsFragment for the given event ID.
     *
     * @param eventId The event ID of the event to display details for
     * @return A new EventDetailsFragment with the event ID stored in its arguments
     */
    public static EventDetailsFragment newInstance(String eventId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates and sets up the Event Details view.
     * <p>
     * Retrieves the event ID passed through fragment, displays mock event
     * information (title and description)
     * </p>
     *
     * @param inflater  Used to create the layout for this fragment
     * @param container Optional parent view that this fragment’s UI will attach to
     * @param savedInstanceState Saved state if the fragment is being re-created
     * @return The full view for the event details dialog
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate your XML layout
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        // Get event ID passed in arguments
        eventId = getArguments() != null ? getArguments().getString("event_id") : "Unknown";

        // Find views
        TextView eventTitle = view.findViewById(R.id.eventTitle);
        TextView eventDesc = view.findViewById(R.id.eventDescription);

        // Mock values (you can later replace this with Firebase)
        eventTitle.setText("Sample Event (" + eventId + ")");
        eventDesc.setText("This is a placeholder description for event ID: " + eventId);

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
     * Called when the dialog is dismissed.
     * <p>
     * Notifies the parent fragment that the event details dialog
     * has been closed by sending a fragment result with the text "eventClosed".
     * </p>
     *
     * @param dialog the dialog interface that was dismissed
     */
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        getParentFragmentManager().setFragmentResult("eventClosed", new Bundle());
    }
}
