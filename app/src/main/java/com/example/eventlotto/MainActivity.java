package com.example.eventlotto;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
<<<<<<< Updated upstream
import android.provider.Settings;

=======
import android.view.View;
>>>>>>> Stashed changes
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventlotto.ui.CreateProfileFragment;
import com.example.eventlotto.ui.HomeFragment;
import com.example.eventlotto.ui.MyEventsFragment;
import com.example.eventlotto.ui.NotificationsFragment;
import com.example.eventlotto.ui.LoginFragment;
<<<<<<< Updated upstream
import com.example.eventlotto.ui.organizer.Org_CreateEventFragment;
import com.example.eventlotto.ui.UsersFragment;
import com.example.eventlotto.ui.entrant.Ent_HomeFragment;
import com.example.eventlotto.ui.entrant.Ent_MyEventsFragment;
import com.example.eventlotto.ui.entrant.Ent_NotificationsFragment;
import com.example.eventlotto.ui.entrant.Ent_ScanFragment;
import com.example.eventlotto.ui.organizer.Org_CreateEventFragment;
=======
import com.example.eventlotto.ui.ScanFragment;
import com.example.eventlotto.ui.WelcomeFragment;
>>>>>>> Stashed changes
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

<<<<<<< Updated upstream
    private FirestoreService firestoreService;
    private String deviceId;
    private static final String CHANNEL_ID = "event_status_updates";
    private static final int NOTIFICATION_ICON = R.drawable.notification_on;
    private java.util.Set<String> subscribedEventIds = new java.util.HashSet<>();
    private java.util.Map<String, String> lastNotified = new java.util.HashMap<>();
    private ListenerRegistration subscriptionsReg;
    private java.util.Map<String, ListenerRegistration> statusDocRegs = new java.util.HashMap<>();
    private java.util.Map<String, Boolean> statusDocInitialized = new java.util.HashMap<>();
    private java.util.Map<String, String> lastSeenStatus = new java.util.HashMap<>();
    private static final int REQ_POST_NOTIFICATIONS = 1001;
=======
    private BottomNavigationView bottomNavigationView;
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

<<<<<<< Updated upstream
        firestoreService = new FirestoreService();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        ensureNotificationChannel();
        requestNotificationPermissionIfNeeded();
        startSubscriptionListener();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            firestoreService.getUser(deviceId)
                    .addOnSuccessListener(snapshot -> {
                        String role = "entrant";
                        if (snapshot.exists() && snapshot.getString("role") != null) {
                            role = snapshot.getString("role");
                        }
                        setupBottomNavMenu(bottomNavigationView, role);
                    })
                    .addOnFailureListener(e -> setupBottomNavMenu(bottomNavigationView, "entrant"));
        }
    }

    private void setupBottomNavMenu(BottomNavigationView bottomNav, String role) {
        bottomNav.getMenu().clear();

        // Inflate menu based on role
        switch (role) {
            case "admin":
                bottomNav.inflateMenu(R.menu.bottom_nav_menu_admin);
                break;
            case "organizer":
                bottomNav.inflateMenu(R.menu.bottom_nav_menu_organizer);
                break;
            default:
                bottomNav.inflateMenu(R.menu.bottom_nav_menu_entrant);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (role.equals("entrant")) {
                if (id == R.id.nav_home) fragment = new Ent_HomeFragment();
                else if (id == R.id.nav_my_events)
                    fragment = new Ent_MyEventsFragment();
                else if (id == R.id.nav_scan)
                    fragment = new Ent_ScanFragment();
                else if (id == R.id.nav_notifications)
                    fragment = new Ent_NotificationsFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            }


            else if (role.equals("organizer")) {
                if (id == R.id.nav_home) fragment = new Ent_HomeFragment();
                else if (id == R.id.nav_create_event)
                    fragment = new Org_CreateEventFragment();
                else if (id == R.id.nav_notifications)
                    fragment = new Ent_NotificationsFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            }

            else if (role.equals("admin")) {
                if (id == R.id.nav_home) fragment = new Ent_HomeFragment();
                else if (id == R.id.nav_users)
                    fragment = new UsersFragment();
                else if (id == R.id.nav_images)
                    fragment = new com.example.eventlotto.ui.ImagesFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

        // Set default selected tab
        bottomNav.setSelectedItemId(R.id.nav_home);
=======
        bottomNavigationView = findViewById(R.id.bottom_navigation);


        if (savedInstanceState == null) {
            bottomNavigationView.setVisibility(View.GONE);
            loadFragment(new WelcomeFragment());
        }


        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    loadFragment(new HomeFragment());
                    return true;
                } else if (id == R.id.nav_scan) {
                    loadFragment(new ScanFragment());
                    return true;
                } else if (id == R.id.nav_notifications) {
                    loadFragment(new NotificationsFragment());
                    return true;
                } else if (id == R.id.nav_my_events) {
                    loadFragment(new MyEventsFragment());
                    return true;
                } else if (id == R.id.nav_profile) {
                    loadFragment(new LoginFragment());
                    return true;
                }

                return false;
            });
        }
    }

    public void showBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
>>>>>>> Stashed changes
    }


    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscriptionsReg != null) {
            subscriptionsReg.remove();
            subscriptionsReg = null;
        }
        // Remove any per-event status doc listeners
        if (!statusDocRegs.isEmpty()) {
            for (ListenerRegistration r : statusDocRegs.values()) {
                if (r != null) r.remove();
            }
            statusDocRegs.clear();
        }
        if (!statusDocRegs.isEmpty()) {
            for (ListenerRegistration r : statusDocRegs.values()) {
                if (r != null) r.remove();
            }
            statusDocRegs.clear();
        }
    }

    private void ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    channel = new NotificationChannel(CHANNEL_ID, "Event Status Updates", NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("Notifications when your event status changes");
                    manager.createNotificationChannel(channel);
                }
            }
        }
    }

    private void startSubscriptionListener() {
        if (subscriptionsReg != null) {
            subscriptionsReg.remove();
            subscriptionsReg = null;
        }
        subscriptionsReg = firestoreService.notifications()
                .whereEqualTo("uid", deviceId)
                .addSnapshotListener((query, error) -> {
                    subscribedEventIds.clear();
                    if (query != null) {
                        for (DocumentSnapshot doc : query) {
                            String eid = doc.getString("eid");
                            if (eid != null) subscribedEventIds.add(eid);
                        }
                    }
                    // Attach per-event document listeners that don't depend on a uid field
                    updatePerEventStatusListeners();
                });
    }
    

    private static String safeLower(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    private void sendLocalNotification(String eventId, String eventTitle, String status) {
        String title;
        String body;
        if ("selected".equals(status)) {
            title = "You're selected!";
            body = (eventTitle != null ? eventTitle : eventId) + ": You have been selected.";
        } else {
            title = "Not chosen";
            body = (eventTitle != null ? eventTitle : eventId) + ": You were not chosen.";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(NOTIFICATION_ICON)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        try {
            nm.notify(Math.abs(eventId.hashCode()), builder.build());
        } catch (SecurityException se) {
            // POST_NOTIFICATIONS may be denied on Android 13+
            Toast.makeText(this, "Enable notifications in Settings to receive updates", Toast.LENGTH_SHORT).show();
        }

        // Also show an in-app banner at the top
        final String bannerMessage =
                "selected".equals(status)
                        ? "Congratulations! You have been selected for " + (eventTitle != null ? eventTitle : eventId)
                        : "Sorry, you were not selected for " + (eventTitle != null ? eventTitle : eventId);
        final boolean isPositive = "selected".equals(status);
        runOnUiThread(() -> showInAppBanner(bannerMessage, isPositive));
    }

    private void showInAppBanner(String message, boolean isPositive) {
        View banner = findViewById(R.id.in_app_banner);
        if (banner == null) return;
        TextView tv = findViewById(R.id.banner_text);
        ImageView icon = findViewById(R.id.banner_icon);
        View accent = findViewById(R.id.banner_accent);
        ImageButton close = findViewById(R.id.banner_close);

        if (tv != null) tv.setText(message);
        if (icon != null) icon.setImageResource(isPositive ? R.drawable.happy_cat : R.drawable.crying_cat);
        if (accent != null) accent.setBackgroundColor(ContextCompat.getColor(this, isPositive ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        if (close != null) close.setOnClickListener(v -> hideBannerNow());

        //slide-in + fade animation
        banner.setAlpha(0f);
        banner.setTranslationY(-40f);
        banner.setVisibility(View.VISIBLE);
        banner.animate().alpha(1f).translationY(0f).setDuration(200).start();

        banner.removeCallbacks(hideBannerRunnable);
        banner.postDelayed(hideBannerRunnable, 3500);
    }

    private final Runnable hideBannerRunnable = () -> {
        hideBannerNow();
    };

    private void hideBannerNow() {
        View banner = findViewById(R.id.in_app_banner);
        if (banner != null && banner.getVisibility() == View.VISIBLE) {
            banner.animate().alpha(0f).translationY(-20f).setDuration(180).withEndAction(() -> banner.setVisibility(View.GONE)).start();
        }
    }

    private void updatePerEventStatusListeners() {
        //remove listeners for events no longer subscribed
        java.util.Iterator<java.util.Map.Entry<String, ListenerRegistration>> it = statusDocRegs.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<String, ListenerRegistration> e = it.next();
            if (!subscribedEventIds.contains(e.getKey())) {
                if (e.getValue() != null) e.getValue().remove();
                it.remove();
            }
        }

        //add listeners for newly subscribed events
        for (String eid : subscribedEventIds) {
            if (statusDocRegs.containsKey(eid)) continue;
            ListenerRegistration reg = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("events").document(eid)
                    .collection("status").document(deviceId)
                    .addSnapshotListener((snap, err) -> {
                        if (err != null || snap == null || !snap.exists()) return;
                        String s = safeLower(snap.getString("status"));
                        String normalized = ("not_chosen".equals(s) ? "not chosen" : s);
                        //on first emission for this event, record and skip notifying
                        if (!statusDocInitialized.containsKey(eid)) {
                            statusDocInitialized.put(eid, true);
                            lastSeenStatus.put(eid, normalized);
                            return;
                        }
                        //only notify when the status actually changes and is a target state
                        String prev = lastSeenStatus.get(eid);
                        if (normalized == null || normalized.equals(prev)) return;
                        lastSeenStatus.put(eid, normalized);
                        if (!"selected".equals(normalized) && !"not chosen".equals(normalized)) return;
                        String last = lastNotified.get(eid);
                        if (normalized.equals(last)) return;
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("events").document(eid)
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    String title = eventDoc.getString("eventTitle");
                                    sendLocalNotification(eid, title, normalized);
                                    lastNotified.put(eid, normalized);
                                });
                    });
            statusDocRegs.put(eid, reg);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
            }
        }
    }
}
