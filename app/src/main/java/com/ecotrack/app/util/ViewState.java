package com.ecotrack.app.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Generic sealed-class-style wrapper for UI state management.
 * Represents Loading, Success, Empty, and Error states.
 *
 * Usage in ViewModel:
 *   MutableLiveData<ViewState<List<User>>> usersState = new MutableLiveData<>();
 *   usersState.setValue(ViewState.loading());
 *   usersState.setValue(ViewState.success(userList));
 *   usersState.setValue(ViewState.error("Network error"));
 *   usersState.setValue(ViewState.empty());
 *
 * Usage in Fragment:
 *   viewModel.getUsersState().observe(getViewLifecycleOwner(), state -> {
 *       switch (state.getStatus()) {
 *           case LOADING: showLoading(); break;
 *           case SUCCESS: showData(state.getData()); break;
 *           case EMPTY:   showEmpty(); break;
 *           case ERROR:   showError(state.getMessage()); break;
 *       }
 *   });
 *
 * @param <T> The type of data held in the success state.
 */
public class ViewState<T> {

    public enum Status {
        LOADING,
        SUCCESS,
        EMPTY,
        ERROR
    }

    @NonNull
    private final Status status;

    @Nullable
    private final T data;

    @Nullable
    private final String message;

    private ViewState(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    // ── Factory methods ──────────────────────────────────────────────────

    @NonNull
    public static <T> ViewState<T> loading() {
        return new ViewState<>(Status.LOADING, null, null);
    }

    @NonNull
    public static <T> ViewState<T> success(@NonNull T data) {
        return new ViewState<>(Status.SUCCESS, data, null);
    }

    @NonNull
    public static <T> ViewState<T> empty() {
        return new ViewState<>(Status.EMPTY, null, null);
    }

    @NonNull
    public static <T> ViewState<T> error(@NonNull String message) {
        return new ViewState<>(Status.ERROR, null, message);
    }

    // ── Getters ──────────────────────────────────────────────────────────

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    // ── Convenience checks ───────────────────────────────────────────────

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isEmpty() {
        return status == Status.EMPTY;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
