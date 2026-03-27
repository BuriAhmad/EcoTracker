package com.ecotrack.app.util;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Date formatting, day-of-week helpers, and timestamp comparisons.
 */
public final class DateUtils {

    private DateUtils() { /* Prevent instantiation */ }

    /**
     * Returns true if d1 and d2 are on consecutive calendar days (d2 is 1 day after d1).
     */
    public static boolean isConsecutiveDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = toStartOfDay(d1);
        Calendar c2 = toStartOfDay(d2);
        c1.add(Calendar.DAY_OF_MONTH, 1);
        return c1.getTimeInMillis() == c2.getTimeInMillis();
    }

    /**
     * Returns true if the given date is today.
     */
    public static boolean isToday(Date date) {
        if (date == null) return false;
        Calendar given = toStartOfDay(date);
        Calendar today = toStartOfDay(new Date());
        return given.getTimeInMillis() == today.getTimeInMillis();
    }

    /**
     * Formats a timestamp relative to now: "Just now", "2h ago", "Yesterday", "Mar 15".
     */
    public static String formatRelativeTime(Date timestamp) {
        if (timestamp == null) return "";

        long diffMs = System.currentTimeMillis() - timestamp.getTime();
        long diffMin = TimeUnit.MILLISECONDS.toMinutes(diffMs);
        long diffHrs = TimeUnit.MILLISECONDS.toHours(diffMs);
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMs);

        if (diffMin < 1) return "Just now";
        if (diffMin < 60) return diffMin + "m ago";
        if (diffHrs < 24) return diffHrs + "h ago";
        if (diffDays == 1) return "Yesterday";
        if (diffDays < 7) return diffDays + "d ago";

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.US);
        return sdf.format(timestamp);
    }

    /**
     * Overload for Firebase Timestamp.
     */
    public static String formatRelativeTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return formatRelativeTime(timestamp.toDate());
    }

    /**
     * Returns the start of the current week (Monday 00:00:00).
     */
    public static Date getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // Go back to Monday
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
        return cal.getTime();
    }

    /**
     * Returns the start of the current month.
     */
    public static Date getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Returns start of today (00:00:00.000).
     */
    public static Date getStartOfToday() {
        return toStartOfDay(new Date()).getTime();
    }

    /**
     * Returns the number of whole days between two dates.
     */
    public static int getDaysBetween(Date start, Date end) {
        if (start == null || end == null) return 0;
        long diffMs = Math.abs(end.getTime() - start.getTime());
        return (int) TimeUnit.MILLISECONDS.toDays(diffMs);
    }

    /**
     * Returns a Calendar set to the start of the given day (00:00:00.000).
     */
    private static Calendar toStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Returns a date N days ago from today.
     */
    public static Date daysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        return cal.getTime();
    }

    /**
     * Format date as "MMM yyyy" for "Member since" display.
     */
    public static String formatMonthYear(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.US);
        return sdf.format(date);
    }

    /**
     * Format date as "MMM d, yyyy".
     */
    public static String formatFullDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        return sdf.format(date);
    }
}
