package com.ecotrack.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.ecotrack.app.MainActivity;
import com.ecotrack.app.model.NotificationPreferences;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.Constants;
import com.example.saturn.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles incoming FCM messages and token refresh.
 * Creates local notifications for push messages and saves token to Firestore.
 */
public class EcoTrackMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "ecotrack_notifications";
    private static final String CHANNEL_NAME = "EcoTrack";
    private static final String CHANNEL_DESC = "Eco activity reminders and updates";

    /* ── Token refresh ────────────────────────────────────────────── */

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        saveTokenToFirestore(token);
    }

    private void saveTokenToFirestore(String token) {
        UserRepository repo = new UserRepository();
        if (repo.getCurrentUser() == null) return;

        String userId = repo.getCurrentUser().getUid();
        Map<String, Object> update = new HashMap<>();
        update.put("fcmToken", token);
        repo.updateUserFields(userId, update);
    }

    /* ── Message received ─────────────────────────────────────────── */

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // Respect user's notification preferences
        NotificationPreferences prefs = NotificationPreferences.load(this);
        String type = message.getData().get("type");

        if (type != null) {
            switch (type) {
                case "challenge_update":
                    if (!prefs.isChallengeUpdates()) return;
                    break;
                case "streak_alert":
                    if (!prefs.isStreakAlerts()) return;
                    break;
                case "campus_milestone":
                    if (!prefs.isCampusMilestones()) return;
                    break;
                case "badge_unlock":
                    if (!prefs.isBadgeUnlocks()) return;
                    break;
                case "daily_reminder":
                    if (!prefs.isDailyReminderEnabled()) return;
                    break;
            }
        }

        // Extract notification content
        String title = "EcoTrack";
        String body = "";

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle() != null
                    ? message.getNotification().getTitle() : title;
            body = message.getNotification().getBody() != null
                    ? message.getNotification().getBody() : "";
        } else {
            title = message.getData().getOrDefault("title", title);
            body = message.getData().getOrDefault("body", "");
        }

        showNotification(title, body);
    }

    /* ── Build & show notification ────────────────────────────────── */

    private void showNotification(String title, String body) {
        ensureNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_eco_24)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notifId = (int) System.currentTimeMillis();
            manager.notify(notifId, builder.build());
        }
    }

    private void ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
