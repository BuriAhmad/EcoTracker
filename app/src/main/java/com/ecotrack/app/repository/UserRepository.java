package com.ecotrack.app.repository;

import com.ecotrack.app.model.User;
import com.ecotrack.app.util.Constants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

/**
 * Abstracts Firebase Auth + Firestore user document operations.
 */
public class UserRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public UserRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Firebase Auth ────────────────────────────────────────────────────

    /**
     * Create a new account with email and password.
     */
    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Sign in with email and password.
     */
    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Sign out the current user.
     */
    public void signOut() {
        auth.signOut();
    }

    /**
     * Returns the currently authenticated Firebase user, or null.
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Whether a user is currently signed in.
     */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Returns the FirebaseAuth instance (for AuthStateListener).
     */
    public FirebaseAuth getAuth() {
        return auth;
    }

    // ── Firestore User Documents ─────────────────────────────────────────

    /**
     * Saves a User POJO to Firestore at users/{userId}.
     */
    public Task<Void> saveUserDocument(User user) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user);
    }

    /**
     * Fetches the user document from Firestore.
     */
    public Task<DocumentSnapshot> getUserDocument(String userId) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get();
    }

    /**
     * Updates specific fields on an existing user document.
     */
    public Task<Void> updateUserDocument(User user) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user);
    }

    /**
     * Deletes a user's Firestore document.
     */
    public Task<Void> deleteUserDocument(String userId) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .delete();
    }

    /**
     * Update specific fields on a user document (e.g., streak, lastLogDate).
     */
    public Task<Void> updateUserFields(String userId, Map<String, Object> fields) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(fields);
    }

    /**
     * Fetch top users ranked by totalPoints (descending) with a limit.
     */
    public Task<QuerySnapshot> getUsersRankedByPoints(int limit) {
        return db.collection(Constants.COLLECTION_USERS)
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }

    /**
     * Fetch users ranked by totalPoints starting after a document (for pagination).
     */
    public Task<QuerySnapshot> getUsersRankedByPoints(int limit, DocumentSnapshot startAfter) {
        return db.collection(Constants.COLLECTION_USERS)
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .startAfter(startAfter)
                .limit(limit)
                .get();
    }

    /**
     * Deletes the current Firebase Auth account.
     */
    public Task<Void> deleteAuthAccount() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.delete();
        }
        return null;
    }
}
