package com.example.eventlotto;

import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventlotto.ui.LoginFragment;
import com.example.eventlotto.ui.entrant.Ent_HomeFragment;
import com.example.eventlotto.ui.entrant.Ent_MyEventsFragment;
import com.example.eventlotto.ui.entrant.Ent_NotificationsFragment;
import com.example.eventlotto.ui.entrant.Ent_ScanFragment;
import com.example.eventlotto.ui.entrant.Ent_UsersFragment;
import com.example.eventlotto.ui.organizer.Org_CreateEventFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

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

            // Entrant menu
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


            // Organizer menu
            else if (role.equals("organizer")) {
                if (id == R.id.nav_home) fragment = new Ent_HomeFragment();
                else if (id == R.id.nav_create_event)
                    fragment = new Org_CreateEventFragment();
                else if (id == R.id.nav_notifications)
                    fragment = new Ent_NotificationsFragment();
                else if (id == R.id.nav_profile) fragment = new LoginFragment();
            }

            // Admin menu
            else if (role.equals("admin")) {
                if (id == R.id.nav_home) fragment = new Ent_HomeFragment();
                else if (id == R.id.nav_users)
                    fragment = new Ent_UsersFragment();
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
}
