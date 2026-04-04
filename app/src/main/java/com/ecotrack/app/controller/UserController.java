package com.ecotrack.app.controller;

import com.ecotrack.app.model.User;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.Constants;
import com.ecotrack.app.util.ValidationUtils;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Orchestrates auth flows: register, login, logout, profile CRUD.
 * All business logic validation happens here before delegating to repository.
 */
public class UserController {

    private final UserRepository userRepository;

    public UserController() {
        this.userRepository = new UserRepository();
    }

    // ── Auth Callback Interface ──────────────────────────────────────────

    /**
     * Callback for authentication operations.
     */
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    // ── Registration ─────────────────────────────────────────────────────

    /**
     * Register a new user: validate inputs → create auth account → create Firestore doc.
     */
    public void register(String name, String email, String department, String password,
                         AuthCallback callback) {
        // ── Input validation ──
        if (!ValidationUtils.isNotEmpty(name)) {
            callback.onError("Please enter your full name");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            callback.onError("Please enter a valid email address");
            return;
        }
        if (!ValidationUtils.isNotEmpty(department)) {
            callback.onError("Please select a department");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            callback.onError("Password must be at least 6 characters");
            return;
        }

        // ── Create Firebase Auth account ──
        userRepository.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Registration failed. Please try again.");
                        return;
                    }

                    // ── Create Firestore user document ──
                    User user = new User(
                            firebaseUser.getUid(),
                            name.trim(),
                            email.trim().toLowerCase(),
                            department
                    );

                    userRepository.saveUserDocument(user)
                            .addOnSuccessListener(aVoid -> {
                                // Increment campus totalUsers counter
                                FirebaseFirestore.getInstance()
                                        .collection(Constants.COLLECTION_CAMPUS_STATS)
                                        .document(Constants.DOC_CAMPUS_AGGREGATE)
                                        .update("totalUsers", FieldValue.increment(1));
                                callback.onSuccess(user);
                            })
                            .addOnFailureListener(e ->
                                    callback.onError("Account created but profile save failed: " + e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("email address is already in use")) {
                        callback.onError("This email is already registered. Try signing in.");
                    } else if (msg != null && msg.contains("badly formatted")) {
                        callback.onError("Please enter a valid email address");
                    } else {
                        callback.onError("Registration failed: " + (msg != null ? msg : "Unknown error"));
                    }
                });
    }

    // ── Login ────────────────────────────────────────────────────────────

    /**
     * Login an existing user: validate → sign in → fetch profile.
     */
    public void login(String email, String password, AuthCallback callback) {
        // ── Input validation ──
        if (!ValidationUtils.isValidEmail(email)) {
            callback.onError("Please enter a valid email address");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            callback.onError("Password must be at least 6 characters");
            return;
        }

        // ── Sign in with Firebase Auth ──
        userRepository.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Sign in failed. Please try again.");
                        return;
                    }

                    // ── Fetch user profile from Firestore ──
                    userRepository.getUserDocument(firebaseUser.getUid())
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    User user = doc.toObject(User.class);
                                    callback.onSuccess(user);
                                } else {
                                    // Edge case: auth exists but Firestore doc missing
                                    // Create a minimal profile
                                    User user = new User(
                                            firebaseUser.getUid(),
                                            firebaseUser.getDisplayName() != null
                                                    ? firebaseUser.getDisplayName() : "User",
                                            firebaseUser.getEmail(),
                                            "Other"
                                    );
                                    userRepository.saveUserDocument(user)
                                            .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                                            .addOnFailureListener(e -> callback.onSuccess(user));
                                }
                            })
                            .addOnFailureListener(e ->
                                    callback.onError("Signed in but couldn't load profile: " + e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("password is invalid")) {
                        callback.onError("Incorrect password. Please try again.");
                    } else if (msg != null && msg.contains("no user record")) {
                        callback.onError("No account found with this email. Create one?");
                    } else if (msg != null && msg.contains("blocked all requests")) {
                        callback.onError("Too many attempts. Please try again later.");
                    } else {
                        callback.onError("Sign in failed: " + (msg != null ? msg : "Unknown error"));
                    }
                });
    }

    // ── Logout ───────────────────────────────────────────────────────────

    /**
     * Sign out the current user.
     */
    public void logout() {
        userRepository.signOut();
    }

    // ── Convenience ──────────────────────────────────────────────────────

    /**
     * Returns the current user's UID, or null if not signed in.
     */
    public String getCurrentUserId() {
        FirebaseUser user = userRepository.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Whether a user is currently signed in.
     */
    public boolean isLoggedIn() {
        return userRepository.isLoggedIn();
    }

    // ── Simple callback for non-auth operations ──────────────────────────

    /**
     * Generic success / error callback for profile and account operations.
     */
    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    // ── Profile Update ───────────────────────────────────────────────────

    /**
     * Update user profile fields in Firestore.
     */
    public void updateProfile(String displayName, String department,
                              boolean anonymousOnFeed, boolean showOnLeaderboard,
                              SimpleCallback callback) {
        if (displayName == null || displayName.trim().isEmpty()) {
            callback.onError("Display name cannot be empty");
            return;
        }

        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Not signed in");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName.trim());
        updates.put("department", department);
        updates.put("anonymousOnFeed", anonymousOnFeed);
        updates.put("showOnLeaderboard", showOnLeaderboard);

        userRepository.updateUserFields(userId, updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Update failed: " + e.getMessage()));
    }

    // ── Avatar Upload ────────────────────────────────────────────────────

    /**
     * Upload compressed avatar bytes to Firebase Storage, then update the user doc.
     */
    public void uploadAvatar(byte[] imageBytes, SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Not signed in");
            return;
        }

        StorageReference ref = FirebaseStorage.getInstance(Constants.STORAGE_BUCKET)
                .getReference()
                .child(Constants.STORAGE_AVATARS)
                .child(userId + ".jpg");

        // Set explicit content type so Storage knows this is an image
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        ref.putBytes(imageBytes, metadata)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Use continueWithTask to properly chain the download URL
                    // request — avoids "object does not exist" race condition
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("avatarUrl", uri.toString());
                    userRepository.updateUserFields(userId, update)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError("Profile save failed: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Avatar upload failed: " + e.getMessage()));
    }

    // ── Account Deletion ─────────────────────────────────────────────────

    /**
     * Delete user: Firestore doc → decrement campus count → delete auth account.
     */
    public void deleteAccount(SimpleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Not signed in");
            return;
        }

        // 1. Delete Firestore document
        userRepository.deleteUserDocument(userId)
                .addOnSuccessListener(aVoid -> {
                    // 2. Decrement campus totalUsers
                    FirebaseFirestore.getInstance()
                            .collection(Constants.COLLECTION_CAMPUS_STATS)
                            .document(Constants.DOC_CAMPUS_AGGREGATE)
                            .update("totalUsers", FieldValue.increment(-1));

                    // 3. Delete Firebase Auth account
                    userRepository.deleteAuthAccount()
                            .addOnSuccessListener(aVoid2 -> callback.onSuccess())
                            .addOnFailureListener(e ->
                                    callback.onError("Profile deleted but auth removal failed"));
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't delete account: " + e.getMessage()));
    }
}
