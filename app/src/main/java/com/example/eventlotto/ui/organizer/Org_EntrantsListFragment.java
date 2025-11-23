package com.example.eventlotto.ui.organizer;
import com.example.eventlotto.R;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Org_EntrantsListFragment extends DialogFragment {

    private static final String ARG_EVENT_ID = "eventId";
    private String eventId;

    public static Org_EntrantsListFragment newInstance(String eventId) {
        Org_EntrantsListFragment fragment = new Org_EntrantsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView entrantsRecycler;
    private EntrantsAdapter adapter;
    private List<DocumentSnapshot> entrantsList = new ArrayList<>();
    private ActivityResultLauncher<String> createFileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrants_list, container, false);

        eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;

        entrantsRecycler = view.findViewById(R.id.entrantsRecycler);
        entrantsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EntrantsAdapter(entrantsList);
        entrantsRecycler.setAdapter(adapter);

        if (eventId != null) loadEntrants(eventId);

        View returnButton = view.findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> dismiss());

        super.onViewCreated(view, savedInstanceState);

        // SAF launcher to let user save CSV
        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> {
                    if (uri != null) exportCsvToUri(uri);
                }
        );

        view.findViewById(R.id.exportCsvButton).setOnClickListener(v -> {
            createFileLauncher.launch("entrants_export.csv");
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    private void exportCsvToUri(Uri uri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .collection("entrants")
                .get()
                .addOnSuccessListener(entrantsSnap -> {

                    if (entrantsSnap.isEmpty()) {
                        Toast.makeText(getContext(), "No entrants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    List<DocumentSnapshot> entrantsDocs = entrantsSnap.getDocuments();

                    // For each entrant, fetch matching user
                    for (DocumentSnapshot entrant : entrantsDocs) {
                        String userId = entrant.getId();
                        Task<DocumentSnapshot> userTask =
                                db.collection("users").document(userId).get();

                        userTasks.add(userTask);
                    }

                    // When ALL user tasks complete:
                    Tasks.whenAllSuccess(userTasks)
                            .addOnSuccessListener(results -> {

                                StringBuilder csv = new StringBuilder();
                                csv.append("Full Name,Email,Phone,Status\n");

                                for (int i = 0; i < entrantsDocs.size(); i++) {
                                    DocumentSnapshot entrant = entrantsDocs.get(i);
                                    DocumentSnapshot user = (DocumentSnapshot) results.get(i);

                                    String name = user.getString("fullName");
                                    String email = user.getString("email");
                                    String phone = user.getString("phone");
                                    String status = entrant.getString("status");

                                    csv.append(safe(name)).append(",");
                                    csv.append(safe(email)).append(",");
                                    csv.append(safe(phone)).append(",");
                                    csv.append(safe(status)).append("\n");
                                }

                                writeCsv(uri, csv.toString());
                            });
                });
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private void writeCsv(Uri uri, String csvContent) {
        try (OutputStream out = requireContext()
                .getContentResolver()
                .openOutputStream(uri)) {

            out.write(csvContent.getBytes());
            out.flush();

            Toast.makeText(getContext(), "CSV exported successfully!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEntrants(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId)
                .collection("status")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    entrantsList.clear();
                    entrantsList.addAll(querySnapshot.getDocuments());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}