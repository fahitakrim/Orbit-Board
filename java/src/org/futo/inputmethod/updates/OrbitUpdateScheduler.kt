package org.futo.inputmethod.updates

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object OrbitUpdateScheduler {
    private const val TAG = "OrbitUpdateScheduler"
    private const val PERIODIC_WORK_NAME = "orbit_board_update_check"
    private const val ONE_TIME_WORK_NAME = "orbit_board_update_check_now"

    fun schedulePeriodicCheck(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWork = PeriodicWorkRequestBuilder<UpdateNotificationWorker>(
                12, TimeUnit.HOURS,
                2, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
            Log.i(TAG, "Enqueued periodic update checking work (every 12 hours)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule periodic update check", e)
        }
    }

    fun checkNow(context: Context, forceNotification: Boolean = false) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = UpdateChecker.checkForUpdate()
                    val update = result.getOrNull()
                    if (update != null && update.isUpdateAvailable) {
                        UpdateNotificationWorker.showUpdateNotification(context, update, force = forceNotification)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Direct check failed, falling back to OneTimeWorkRequest", e)
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val oneTimeWork = OneTimeWorkRequestBuilder<UpdateNotificationWorker>()
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance(context).enqueueUniqueWork(
                        ONE_TIME_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        oneTimeWork
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger update check now", e)
        }
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(
            UpdateNotificationWorker.PREF_NAME,
            Context.MODE_PRIVATE
        )
        return prefs.getBoolean(UpdateNotificationWorker.KEY_UPDATES_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(
            UpdateNotificationWorker.PREF_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit().putBoolean(UpdateNotificationWorker.KEY_UPDATES_ENABLED, enabled).apply()
        if (enabled) {
            schedulePeriodicCheck(context)
        } else {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to cancel update work", e)
            }
        }
    }
}
