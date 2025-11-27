package com.example.eventlotto;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventlotto.ui.LoginFragment;
import com.example.eventlotto.ui.admin.AdmHomeFragment;
import com.example.eventlotto.ui.organizer.OrgHomeFragment;
import com.example.eventlotto.ui.organizer.OrgCreateEventFragment;
import com.example.eventlotto.ui.admin.AdmImagesFragment;
import com.example.eventlotto.ui.admin.AdmProfilesFragment;
import com.example.eventlotto.ui.admin.AdmNotificationsFragment;
import com.example.eventlotto.ui.entrant.EntHomeFragment;
import com.example.eventlotto.ui.entrant.EntMyEventsFragment;
import com.example.eventlotto.ui.entrant.EntNotificationsFragment;
import com.example.eventlotto.ui.entrant.EntScanFragment;
import com.example.eventlotto.ui.entrant.EntWelcomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Main activity for EventLotto.
 * <p>
 * Handles navigation through fragments, bottom nav setup, and event status notifications.
 * Implements in - app banners and local notifications for event status updates.
 * </p>
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirestoreService firestoreService;
    private String deviceId;

    private static final String CHANNEL_ID = "event_status_updates";
    private static final int NOTIFICATION_ICON = R.drawable.notification_on;
    private static final int REQ_POST_NOTIFICATIONS = 1001;

    private Set<String> subscribedEventIds = new HashSet<>();
    private Map<String, String> lastNotified = new HashMap<>();
    private ListenerRegistration subscriptionsReg;
    private Map<String, ListenerRegistration> statusDocRegs = new HashMap<>();
    private Map<String, Boolean> statusDocInitialized = new HashMap<>();
    private Map<String, String> lastSeenStatus = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestoreService = new FirestoreService();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);

        ensureNotificationChannel();
        requestNotificationPermissionIfNeeded();
        startSubscriptionListener();

        if (savedInstanceState == null) {
            firestoreService.getUser(deviceId)
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot != null && snapshot.exists()) {
                            String role = snapshot.getString("role") != null
                                    ? snapshot.getString("role") : "entrant"; // changed
                            setupBottomNavMenu(bottomNavigationView, role);

                            if ("organizer".equals(role)) {
                                loadFragment(new OrgHomeFragment());
                            } else if ("admin".equals(role)) {
                                loadFragment(new AdmHomeFragment());
                            } else {
                                loadFragment(new EntHomeFragment());
                            }

                            showBottomNavigation();

                        } else {
                            loadFragment(new EntWelcomeFragment());
                            hideBottomNavigation();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadFragment(new EntWelcomeFragment());
                        hideBottomNavigation();
                    });
        }
    }

    /**
     * Sets up the bottom navigation menu based on the user's role.
     *
     * @param bottomNav BottomNavigationView to configure.
     * @param role User role (can be 'admin', 'organizer', or 'entrant').
     */
    private void setupBottomNavMenu(BottomNavigationView bottomNav, String role) {
        if (bottomNav == null) return;
        bottomNav.getMenu().clear();

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

            if (role.equals("admin")) {
                if (id == R.id.nav_home) fragment = new AdmHomeFragment();
                else if (id == R.id.nav_admin_images)
                    fragment = new AdmImagesFragment();
                else if (id == R.id.nav_admin_notifications)
                    fragment = new AdmNotificationsFragment();
                else if (id == R.id.nav_admin_profiles)
                    fragment = new AdmProfilesFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            } else if ("organizer".equals(role)) {
                if (id == R.id.nav_home) fragment = new OrgHomeFragment();
                else if (id == R.id.nav_create_event) fragment = new OrgCreateEventFragment();
                else if (id == R.id.nav_notifications) fragment = new EntNotificationsFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            } else {
                // entrant
                if (id == R.id.nav_home) fragment = new EntHomeFragment();
                else if (id == R.id.nav_scan) fragment = new EntScanFragment();
                else if (id == R.id.nav_notifications) fragment = new EntNotificationsFragment();
                else if (id == R.id.nav_my_events) fragment = new EntMyEventsFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    /**
     * Replaces the current fragment with the specified fragment.
     *
     * @param fragment Fragment to display.
     */
    public void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }

    /** For bottom navigation visibility. */
    public void showBottomNavigation() {
        if (bottomNavigationView != null)
            bottomNavigationView.setVisibility(View.VISIBLE);
    }
    /** For hiding bottom navigation visibility. */
    public void hideBottomNavigation() {
        if (bottomNavigationView != null)
            bottomNavigationView.setVisibility(View.GONE);
    }

    /**
     * Initializes the bottom navigation menu after creating a profile or switching roles.
     *
     * @param role User role to set up ("admin", "organizer", or "entrant").
     */
    // Call this after creating a new profile so the bottom
    // navigation is fully initialized for the user's role.
    public void initBottomNavForRole(String role) {
        setupBottomNavMenu(bottomNavigationView, role);
        showBottomNavigation();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    /** Ensures the notification channel is created for version O and above. */
    void ensureNotificationChannel() { // channel required for notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    channel = new NotificationChannel(
                            CHANNEL_ID,
                            "Event Status Updates",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    channel.setDescription("Notifications when your event status changes");
                    manager.createNotificationChannel(channel);
                }
            }
        }
    }

    /** Starts listening for the user’s subscribed event status updates in Firestore. */
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
                    // attach per-event listeners that don't depend on uid field
                    updatePerEventStatusListeners();
                });
    }

    /** Updates per-event listeners for status changes. */
    private void updatePerEventStatusListeners() {
        Iterator<Map.Entry<String, ListenerRegistration>> it = statusDocRegs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ListenerRegistration> e = it.next();
            if (!subscribedEventIds.contains(e.getKey())) {
                if (e.getValue() != null) e.getValue().remove();
                it.remove();
            }
        }

        for (String eid : subscribedEventIds) {
            if (statusDocRegs.containsKey(eid)) continue;
            ListenerRegistration reg = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("events").document(eid)
                    .collection("status").document(deviceId)
                    .addSnapshotListener((snap, err) -> {
                        if (err != null || snap == null || !snap.exists()) return;
                        String s = safeLower(snap.getString("status"));
                        String normalized = ("not_chosen".equals(s) ? "not chosen" : s);

                        if (!statusDocInitialized.containsKey(eid)) {
                            statusDocInitialized.put(eid, true);
                            lastSeenStatus.put(eid, normalized);
                            return;
                        }

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

    static String safeLower(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    /**
     * Sends a local notification and in-app banner for a status change.
     *
     * @param eventId Event ID
     * @param eventTitle Event title (optional)
     * @param status New event status ("selected" or "not chosen")
     */
    private void sendLocalNotification(String eventId, String eventTitle, String status) {
        String title;
        String body;
        if ("selected".equals(status)) { // device notification
            title = "You're selected!";
            body = (eventTitle != null ? eventTitle : eventId) + ": You have been selected!";
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
            Toast.makeText(this, "Enable notifications in Settings to receive updates", Toast.LENGTH_SHORT).show();
        }

        // in-app banner at the top
        final String bannerMessage =
                "selected".equals(status)
                        ? "Congratulations! You have been selected for " + (eventTitle != null ? eventTitle : eventId)
                        : "Sorry, you were not selected for " + (eventTitle != null ? eventTitle : eventId);
        final boolean isPositive = "selected".equals(status);
        runOnUiThread(() -> showInAppBanner(bannerMessage, isPositive));
    }

    /**
     * Displays an in-app banner notification.
     *
     * @param message Banner text
     * @param isPositive True if positive (green/happy), false if negative (red/sad)
     */
    void showInAppBanner(String message, boolean isPositive) { //in app banner for notification
        View banner = findViewById(R.id.in_app_banner);
        if (banner == null) return;
        TextView tv = findViewById(R.id.banner_text);
        ImageView icon = findViewById(R.id.banner_icon);
        View accent = findViewById(R.id.banner_accent);
        ImageButton close = findViewById(R.id.banner_close);

        if (tv != null) tv.setText(message);
        if (icon != null) icon.setImageResource(isPositive ? R.drawable.happy_cat : R.drawable.crying_cat);
        if (accent != null)
            accent.setBackgroundColor(ContextCompat.getColor(this,
                    isPositive ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        if (close != null) close.setOnClickListener(v -> hideBannerNow());

        banner.setAlpha(0f);
        banner.setTranslationY(-40f);
        banner.setVisibility(View.VISIBLE);
        banner.animate().alpha(1f).translationY(0f).setDuration(200).start();

        banner.removeCallbacks(hideBannerRunnable);
        banner.postDelayed(hideBannerRunnable, 3500);
    }

    private final Runnable hideBannerRunnable = this::hideBannerNow;

    private void hideBannerNow() {
        View banner = findViewById(R.id.in_app_banner);
        if (banner != null && banner.getVisibility() == View.VISIBLE) {
            banner.animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(180)
                    .withEndAction(() -> banner.setVisibility(View.GONE))
                    .start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscriptionsReg != null) {
            subscriptionsReg.remove();
            subscriptionsReg = null;
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
    /** Requests notification permission on devices if not granted. */
    private void requestNotificationPermissionIfNeeded() { // we need this for notification
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
            }
            statusDocRegs.clear();
        }
    }
}