package com.example.eventlotto;

import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventlotto.ui.admin.AdminPlaceholderFragment;
import com.example.eventlotto.ui.entrant.EntHomeFragment;
import com.example.eventlotto.ui.entrant.EntLoginFragment;
import com.example.eventlotto.ui.entrant.EntNotificationsFragment;
import com.example.eventlotto.ui.RoleSelectionFragment;
import com.example.eventlotto.ui.entrant.EntMyEventsFragment;
import com.example.eventlotto.ui.entrant.EntScanFragment;
import com.example.eventlotto.ui.organizer.OrgAddEventFragment;
import com.example.eventlotto.ui.organizer.OrgHomeFragment;
import com.example.eventlotto.ui.organizer.OrgFragment;
import com.example.eventlotto.ui.organizer.OrgProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements RoleSelectionFragment.RoleSelectionListener {

    private BottomNavigationView bottomNavigationView;
    private String currentRole = null; // "entrant", "organizer", "admin"

    private FirestoreService firestoreService;

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

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if ("organizer".equals(currentRole)) {
                    if (id == R.id.org_nav_home) {
                        loadFragment(new OrgHomeFragment());
                        return true;
                    } else if (id == R.id.org_nav_add_event) {
                        loadFragment(new OrgAddEventFragment());
                        return true;
                    } else if (id == R.id.org_nav_notifications) {
                        loadFragment(new OrgFragment());
                        return true;
                    } else if (id == R.id.org_nav_profile) {
                        loadFragment(new OrgProfileFragment());
                        return true;
                    }
                } else { // entrant (default)
                    if (id == R.id.nav_home) {
                        loadFragment(new EntHomeFragment());
                        return true;
                    } else if (id == R.id.nav_scan) {
                        loadFragment(new EntScanFragment());
                        return true;
                    } else if (id == R.id.nav_notifications) {
                        loadFragment(new EntNotificationsFragment());
                        return true;
                    } else if (id == R.id.nav_my_events) {
                        loadFragment(new EntMyEventsFragment());
                        return true;
                    } else if (id == R.id.nav_profile) {
                        loadFragment(new EntLoginFragment());
                        return true;
                    }
                }
                return false;
            });
        }

        if (savedInstanceState != null) {
            currentRole = savedInstanceState.getString("currentRole");
            if (currentRole != null) {
                applyRole(currentRole, false);
                return;
            }
        firestoreService = new FirestoreService();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            // Get current user UID
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            firestoreService.getUser(deviceId)
                    .addOnSuccessListener(snapshot -> {
                        String role = "entrant";
                        if (snapshot.exists() && snapshot.getString("role") != null) {
                            role = snapshot.getString("role");
                        }
                        if (bottomNavigationView != null) {
                            setupBottomNavMenu(bottomNavigationView, role);
                        }
                    })
                    .addOnFailureListener(e -> setupBottomNavMenu(bottomNavigationView, "entrant"));
        }

        // Initial screen: role selection (hide bottom nav until chosen)
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(android.view.View.GONE);
        loadFragment(new RoleSelectionFragment());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentRole != null) {
            outState.putString("currentRole", currentRole);
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
                bottomNav.inflateMenu(R.menu.bottom_nav_menu);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            // Entrant menu
            if (role.equals("entrant")) {
                if (id == R.id.nav_home) fragment = new com.example.eventlotto.ui.HomeFragment();
                else if (id == R.id.nav_my_events)
                    fragment = new com.example.eventlotto.ui.MyEventsFragment();
                else if (id == R.id.nav_scan)
                    fragment = new com.example.eventlotto.ui.ScanFragment();
                else if (id == R.id.nav_notifications)
                    fragment = new com.example.eventlotto.ui.NotificationsFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            }


            // Organizer menu
            else if (role.equals("organizer")) {
                if (id == R.id.nav_home) fragment = new com.example.eventlotto.ui.HomeFragment();
                else if (id == R.id.nav_create_event)
                    fragment = new com.example.eventlotto.ui.CreateEventFragment();
                else if (id == R.id.nav_notifications)
                    fragment = new com.example.eventlotto.ui.NotificationsFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            }

            // Admin menu
            else if (role.equals("admin")) {
                if (id == R.id.nav_home) fragment = new com.example.eventlotto.ui.HomeFragment();
                else if (id == R.id.nav_users)
                    fragment = new com.example.eventlotto.ui.UsersFragment();
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
    }


    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }

    @Override
    public void onRoleSelected(String roleKey) {
        applyRole(roleKey, true);
    }

    private void applyRole(String roleKey, boolean fromSelection) {
        this.currentRole = roleKey;
        if (bottomNavigationView == null) return;

        // Ensure nav visible only for roles that use it
        if ("admin".equals(roleKey)) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.setVisibility(android.view.View.GONE);
            // Load a blank admin fragment
            loadFragment(new AdminPlaceholderFragment());
            return;
        }

        bottomNavigationView.setVisibility(android.view.View.VISIBLE);
        bottomNavigationView.getMenu().clear();
        if ("organizer".equals(roleKey)) {
            getMenuInflater().inflate(R.menu.organizer_bottom_nav_menu, bottomNavigationView.getMenu());
            bottomNavigationView.setSelectedItemId(R.id.org_nav_home);
            loadFragment(new OrgHomeFragment());
        } else { // entrant default
            getMenuInflater().inflate(R.menu.bottom_nav_menu, bottomNavigationView.getMenu());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            loadFragment(new EntHomeFragment());
        }
    }
}
