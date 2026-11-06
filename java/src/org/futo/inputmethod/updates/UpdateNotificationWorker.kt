package org.futo.inputmethod.updates

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.settings.SettingsActivity

class UpdateNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting periodic update check from GitHub repository...")
        try {
            val result = UpdateChecker.checkForUpdate()
            val update = result.getOrNull()

            if (update != null && update.isUpdateAvailable) {
                Log.i(TAG, "Update available: v${update.versionName} (${update.releaseTitle})")
                showUpdateNotification(applicationContext, update)
            } else {
                Log.i(TAG, "Orbit Board is up to date.")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check for updates in background", e)
            return Result.retry()
        }
    }

    companion object {
        const val TAG = "OrbitUpdateWorker"
        const val CHANNEL_ID = "orbit_board_updates"
        const val NOTIFICATION_ID = 20261101
        const val PREF_NAME = "orbit_board_updates_prefs"
        const val KEY_LAST_NOTIFIED_VERSION = "last_notified_version"
        const val KEY_LAST_NOTIFIED_TIME = "last_notified_time"
        const val KEY_UPDATES_ENABLED = "update_notifications_enabled"

        // Rate limit notifications to at most once per 24 hours for the same version
        private const val MIN_NOTIFICATION_INTERVAL_MS = 24L * 60L * 60L * 1000L

        fun showUpdateNotification(context: Context, update: AppUpdate, force: Boolean = false) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val updatesEnabled = prefs.getBoolean(KEY_UPDATES_ENABLED, true)
            if (!updatesEnabled && !force) {
                Log.i(TAG, "Update notifications disabled by user")
                return
            }

            val lastVersion = prefs.getString(KEY_LAST_NOTIFIED_VERSION, "") ?: ""
            val lastTime = prefs.getLong(KEY_LAST_NOTIFIED_TIME, 0L)
            val now = System.currentTimeMillis()

            if (!force && lastVersion == update.versionName && (now - lastTime < MIN_NOTIFICATION_INTERVAL_MS)) {
                Log.i(TAG, "Notification for v${update.versionName} already shown within 24 hours, skipping")
                return
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Create notification channel for Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Orbit Board Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts when a new version of Orbit Board is available to download"
                    enableLights(true)
                    lightColor = 0xFFD71921.toInt()
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Primary tap intent: opens SettingsActivity
            val appIntent = Intent(context, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SHOW_UPDATE_DIALOG", true)
                putExtra("UPDATE_VERSION", update.versionName)
                putExtra("UPDATE_URL", update.downloadUrl)
            }
            val pendingAppIntent = PendingIntent.getActivity(
                context,
                1001,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Direct download action intent (if valid URL provided)
            val downloadPendingIntent = if (update.downloadUrl.isNotBlank()) {
                val downloadIntent = Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                PendingIntent.getActivity(
                    context,
                    1002,
                    downloadIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else null

            val titleText = if (update.releaseTitle.isNotBlank()) {
                "Orbit Board: ${update.releaseTitle}"
            } else {
                "Orbit Board v${update.versionName} Available"
            }

            val contentText = "New version v${update.versionName} is ready to download."

            val bigText = buildString {
                append("A new update is available for Orbit Board!\n\n")
                if (update.releaseNotes.isNotBlank()) {
                    append(update.releaseNotes.take(300))
                    if (update.releaseNotes.length > 300) append("...")
                } else {
                    append("Bug fixes and performance improvements.")
                }
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titleText)
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(0xFFD71921.toInt())
                .setAutoCancel(true)
                .setContentIntent(pendingAppIntent)

            if (downloadPendingIntent != null) {
                builder.addAction(
                    android.R.drawable.stat_sys_download,
                    "Download",
                    downloadPendingIntent
                )
            }
            builder.addAction(
                android.R.drawable.ic_menu_info_details,
                "View",
                pendingAppIntent
            )

            try {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                prefs.edit()
                    .putString(KEY_LAST_NOTIFIED_VERSION, update.versionName)
                    .putLong(KEY_LAST_NOTIFIED_TIME, now)
                    .apply()
                Log.i(TAG, "Update notification posted successfully for v${update.versionName}")
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot post notification: POST_NOTIFICATIONS permission not granted", e)
            }
        }
    }
}
