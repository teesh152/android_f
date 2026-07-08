package com.mob2.tubeexplorer.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.mob2.tubeexplorer.R;

/**
 * NotificationHelper
 * ------------------
 * Handles the Notifications requirement of the project:
 *   - shows a notification when content is SUCCESSFULLY loaded
 *   - shows a notification when an ERROR occurs while fetching API data
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "tube_explorer_channel";
    private static final int SUCCESS_ID = 1001;
    private static final int ERROR_ID = 1002;

    /** Create the notification channel (required on Android 8.0+). */
    public static void createChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "TubeExplorer Updates",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Notifications for API loading status");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    /** Notification shown when videos are loaded successfully. */
    public static void showSuccess(Context context, int count) {
        show(context, SUCCESS_ID,
                context.getString(R.string.notif_success_title),
                context.getString(R.string.notif_success_text, count));
    }

    /** Notification shown when an error occurs while fetching data. */
    public static void showError(Context context, String message) {
        show(context, ERROR_ID,
                context.getString(R.string.notif_error_title),
                message);
    }

    private static void show(Context context, int id, String title, String text) {
        // On Android 13+ we must hold the POST_NOTIFICATIONS runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(id, builder.build());
    }
}
