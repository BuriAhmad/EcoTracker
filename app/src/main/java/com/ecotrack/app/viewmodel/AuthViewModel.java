package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.controller.UserController;
import com.ecotrack.app.model.User;
import com.ecotrack.app.util.ViewState;

/**
 * UI state management for Login and Register flows.
 * Exposes LiveData that fragments observe for Loading/Success/Error states.
 */
public class AuthViewModel extends ViewModel {

    private final UserController userController;

    private final MutableLiveData<ViewState<User>> loginState = new MutableLiveData<>();
    private final MutableLiveData<ViewState<User>> registerState = new MutableLiveData<>();

    public AuthViewModel() {
        this.userController = new UserController();
    }

    // ── Exposed LiveData (read-only) ─────────────────────────────────────

    public LiveData<ViewState<User>> getLoginState() {
        return loginState;
    }

    public LiveData<ViewState<User>> getRegisterState() {
        return registerState;
    }

    // ── Actions ──────────────────────────────────────────────────────────

    /**
     * Attempt login. Posts Loading → Success/Error to loginState.
     */
    public void login(String email, String password) {
        loginState.setValue(ViewState.loading());

        userController.login(email, password, new UserController.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                loginState.setValue(ViewState.success(user));
            }

            @Override
            public void onError(String message) {
                loginState.setValue(ViewState.error(message));
            }
        });
    }

    /**
     * Attempt registration. Posts Loading → Success/Error to registerState.
     */
    public void register(String name, String email, String department, String password) {
        registerState.setValue(ViewState.loading());

        userController.register(name, email, department, password, new UserController.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                registerState.setValue(ViewState.success(user));
            }

            @Override
            public void onError(String message) {
                registerState.setValue(ViewState.error(message));
            }
        });
    }

    /**
     * Sign out the current user.
     */
    public void logout() {
        userController.logout();
    }

    /**
     * Whether a user is currently signed in.
     */
    public boolean isLoggedIn() {
        return userController.isLoggedIn();
    }
}
