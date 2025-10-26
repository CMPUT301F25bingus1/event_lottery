package com.example.eventlotto;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    loadFragment(new com.example.eventlotto.ui.HomeFragment());
                    return true;
                } else if (id == R.id.nav_scan) {
                    loadFragment(new com.example.eventlotto.ui.ScanFragment());
                    return true;
                } else if (id == R.id.nav_notifications) {
                    loadFragment(new com.example.eventlotto.ui.NotificationsFragment());
                    return true;
                } else if (id == R.id.nav_my_events) {
                    loadFragment(new com.example.eventlotto.ui.MyEventsFragment());
                    return true;
                } else if (id == R.id.nav_profile) {
                    loadFragment(new com.example.eventlotto.ui.ProfileFragment());
                    return true;
                }
                return false;
            });
            if (savedInstanceState == null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }
}
