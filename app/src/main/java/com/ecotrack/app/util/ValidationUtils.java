package com.ecotrack.app.util;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Stateless input validation helpers.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Prevent instantiation
    }

    /**
     * Checks if a string is a valid email address.
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Checks if a password meets minimum requirements (≥6 characters).
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Checks if a string is non-null and non-blank.
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Checks if a quantity is positive and non-zero.
     */
    public static boolean isPositiveQuantity(double quantity) {
        return quantity > 0;
    }
}
