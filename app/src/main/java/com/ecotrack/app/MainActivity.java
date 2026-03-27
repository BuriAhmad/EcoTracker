package com.ecotrack.app;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.saturn.R;
import com.example.saturn.databinding.ActivityMainBinding;
import com.ecotrack.app.util.FirestoreSeeder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.Set;

/**
 * Single-Activity host for EcoTrack.
 * Sets up Navigation Component with NavHostFragment and BottomNavigationView.
 * Handles auth state redirection and bottom nav visibility.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    // Destinations where bottom nav should be visible
    private final Set<Integer> bottomNavDestinations = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavDestinations();
        setupNavigation();
        checkAuthState();

        // Seed Firestore with conversion factors & initial campus stats (no-op if already seeded)
        FirestoreSeeder.seedIfNeeded();
    }

    private void setupBottomNavDestinations() {
        bottomNavDestinations.add(R.id.homeFragment);
        bottomNavDestinations.add(R.id.logActivityFragment);
        bottomNavDestinations.add(R.id.leaderboardFragment);
        bottomNavDestinations.add(R.id.profileFragment);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Wire BottomNavigationView to NavController
            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            // Control bottom nav visibility based on destination
            navController.addOnDestinationChangedListener(
                    (controller, destination, arguments) -> {
                        if (bottomNavDestinations.contains(destination.getId())) {
                            binding.bottomNav.setVisibility(View.VISIBLE);
                        } else {
                            binding.bottomNav.setVisibility(View.GONE);
                        }
                    });
        }
    }

    /**
     * Check if user is authenticated. If yes, navigate to Home.
     * If not, stay on Login (the start destination).
     */
    private void checkAuthState() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && navController != null) {
            // User is signed in — navigate to home, clearing login from back stack
            navController.navigate(R.id.action_login_to_home);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}
