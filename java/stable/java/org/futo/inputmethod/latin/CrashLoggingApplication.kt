package org.futo.inputmethod.latin

import android.app.Application
import android.content.Context
import android.os.UserManager
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import org.acra.ACRA
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import java.io.File
import java.util.Date

class CrashLoggingApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        // Install safe crash logging that writes directly to local storage without launching any Activity.
        // For an InputMethodService (IME), launching modal dialogs or email intents on crash causes
        // a catastrophic fork storm when Android OS IMMS restarts the IME service, freezing the phone.
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("OrbitBoard", "FATAL CRASH in thread ${thread.name}", throwable)
                val crashFile = File(filesDir, "last_crash.txt")
                crashFile.writeText(
                    "Time: ${Date()}\nThread: ${thread.name}\nVersion: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n" +
                            Log.getStackTraceString(throwable)
                )
            } catch (t: Throwable) {
                Log.e("OrbitBoard", "Failed to write crash dump", t)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        val userManager = getSystemService(Context.USER_SERVICE) as UserManager
        if(userManager.isUserUnlocked) {
            try {
                initAcra {
                    reportFormat = StringFormat.JSON
                    // Do NOT configure dialog {} or mailSender {} here to prevent process fork storms.
                }
                acraInitialized = true
            } catch (t: Throwable) {
                Log.w("CrashLoggingApplication", "Failed to init ACRA", t)
            }
        } else {
            println("Skipping ACRA, as user is locked")
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            val prefs = org.futo.inputmethod.latin.uix.PreferenceUtils.getDefaultSharedPreferences(this)
            org.futo.inputmethod.latin.settings.Settings.upgradeAutocorrectionSettings(prefs, resources)
        } catch (t: Throwable) {
            Log.w("CrashLoggingApplication", "Failed to sanitize autocorrection prefs", t)
        }
    }

    companion object {
        var acraInitialized = false

        fun logPreferences(preferences: Preferences) {
            if(acraInitialized) {
                try {
                    preferences.asMap().forEach {
                        ACRA.errorReporter.putCustomData(it.key.name, it.value.toString())
                    }
                } catch (ignored: Throwable) {}
            }
        }
    }
}
