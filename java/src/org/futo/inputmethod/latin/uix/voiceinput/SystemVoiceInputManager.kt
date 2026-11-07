package org.futo.inputmethod.latin.uix.voiceinput

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import org.futo.inputmethod.latin.LatinIME
import org.futo.inputmethod.latin.permissions.PermissionsActivity
import org.futo.inputmethod.latin.settings.Settings
import java.util.Locale

class SystemVoiceInputManager(private val latinIME: LatinIME) : RecognitionListener {
    private val TAG = "SystemVoiceInputManager"
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isStreamMutedForVoice = false

    // User's intended state: true when active, false when explicitly turned off by user
    @Volatile
    var isListening: Boolean = false
        private set

    // Observable Compose state for UI components like the mic action button
    val isListeningState: MutableState<Boolean> = mutableStateOf(false)

    // Live partial text purely for UI preview in ActionBar
    val livePartialText: MutableState<String?> = mutableStateOf(null)

    private var activeLocale: Locale? = null
    private var lastComposingLength: Int = 0

    fun toggle(locale: Locale) {
        if (isListening) {
            stopListening()
        } else {
            startListening(locale)
        }
    }

    private fun getVoiceLanguageTag(locale: Locale): String {
        return when (locale.language) {
            "bn" -> if (locale.country.isNotEmpty()) locale.toLanguageTag() else "bn-BD"
            "en" -> if (locale.country.isNotEmpty()) locale.toLanguageTag() else "en-US"
            else -> locale.toLanguageTag()
        }
    }

    fun startListening(locale: Locale) {
        try {
            activeLocale = locale

            // Check RECORD_AUDIO permission before attempting to listen
            if (ContextCompat.checkSelfPermission(latinIME, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "RECORD_AUDIO permission not granted, requesting via PermissionsManager")
                try {
                    org.futo.inputmethod.latin.permissions.PermissionsManager.get(latinIME).requestPermissions(
                        { allGranted ->
                            if (allGranted) {
                                mainHandler.post {
                                    startListening(locale)
                                }
                            } else {
                                latinIME.uixManager.showAssistantMessage("মাইক্রোফোন পারমিশন প্রয়োজন")
                            }
                        },
                        null,
                        Manifest.permission.RECORD_AUDIO
                    )
                } catch (e: Throwable) {
                    Log.e(TAG, "Failed to request permission via PermissionsManager", e)
                    latinIME.uixManager.showAssistantMessage("মাইক্রোফোন পারমিশন প্রয়োজন")
                }
                return
            }

            isListening = true
            mainHandler.post {
                try {
                    isListeningState.value = true
                    livePartialText.value = null
                    startRecognitionInternal(locale)
                } catch (t: Throwable) {
                    Log.e(TAG, "Failed in startRecognitionInternal on main thread", t)
                    isListening = false
                    isListeningState.value = false
                    latinIME.uixManager.showAssistantMessage("ভয়েস ইনপুট চালু করা যায়নি")
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error starting voice listening", t)
            isListening = false
            isListeningState.value = false
            latinIME.uixManager.showAssistantMessage("ভয়েস ইনপুট চালু করা যায়নি")
        }
    }

    private fun resetRecognizer() {
        try {
            speechRecognizer?.setRecognitionListener(null)
            speechRecognizer?.destroy()
        } catch (e: Throwable) {
            Log.e(TAG, "Error destroying speechRecognizer", e)
        }
        speechRecognizer = null
    }

    /**
     * Discovers the best speech recognition component on this device.
     * Priority:
     *   1. com.google.android.tts (Speech Services by Google) — best multilingual support
     *   2. com.google.android.googlequicksearchbox (Google App) — also good
     *   3. null -> fall back to system default
     */
    private fun findBestSpeechComponent(): ComponentName? {
        try {
            val intent = Intent("android.speech.RecognitionService")
            val resolveInfoList = latinIME.packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA)

            Log.d(TAG, "Found ${resolveInfoList.size} speech recognition services:")
            for (info in resolveInfoList) {
                Log.d(TAG, "  - ${info.serviceInfo.packageName} / ${info.serviceInfo.name}")
            }

            // Priority 1: Speech Services by Google (com.google.android.tts)
            val ttsService = resolveInfoList.find {
                it.serviceInfo.packageName == "com.google.android.tts"
            }
            if (ttsService != null) {
                val component = ComponentName(ttsService.serviceInfo.packageName, ttsService.serviceInfo.name)
                Log.i(TAG, "Selected best speech engine: $component (Speech Services by Google)")
                return component
            }

            // Priority 2: Google App (com.google.android.googlequicksearchbox)
            val googleApp = resolveInfoList.find {
                it.serviceInfo.packageName == "com.google.android.googlequicksearchbox"
            }
            if (googleApp != null) {
                val component = ComponentName(googleApp.serviceInfo.packageName, googleApp.serviceInfo.name)
                Log.i(TAG, "Selected speech engine: $component (Google App)")
                return component
            }

            // Priority 3: Any Google service (avoid AiAi/Bixby/etc)
            val anyGoogle = resolveInfoList.find {
                it.serviceInfo.packageName.startsWith("com.google.") &&
                    it.serviceInfo.packageName != "com.google.android.as" // Skip AiAi
            }
            if (anyGoogle != null) {
                val component = ComponentName(anyGoogle.serviceInfo.packageName, anyGoogle.serviceInfo.name)
                Log.i(TAG, "Selected speech engine (Google fallback): $component")
                return component
            }

            Log.w(TAG, "No preferred speech engine found, will use system default")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering speech engines", e)
            return null
        }
    }

    private fun startRecognitionInternal(locale: Locale) {
        if (!isListening) return

        try {
            if (speechRecognizer == null) {
                val isAvailable = try {
                    SpeechRecognizer.isRecognitionAvailable(latinIME)
                } catch (_: Throwable) {
                    false
                }
                if (isAvailable) {
                    val bestComponent = findBestSpeechComponent()
                    speechRecognizer = if (bestComponent != null) {
                        try {
                            SpeechRecognizer.createSpeechRecognizer(latinIME, bestComponent)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed creating SpeechRecognizer with component $bestComponent, using default", e)
                            SpeechRecognizer.createSpeechRecognizer(latinIME)
                        }
                    } else {
                        SpeechRecognizer.createSpeechRecognizer(latinIME)
                    }
                    if (speechRecognizer == null) {
                        Log.w(TAG, "SpeechRecognizer creation returned null, falling back to intent dialog")
                        isListening = false
                        mainHandler.post { 
                            isListeningState.value = false 
                            livePartialText.value = null
                        }
                        fallbackToIntent(locale)
                        return
                    }
                    speechRecognizer?.setRecognitionListener(this)
                } else {
                    Log.w(TAG, "SpeechRecognizer not available on this device, checking intent dialog")
                    isListening = false
                    mainHandler.post { 
                        isListeningState.value = false 
                        livePartialText.value = null
                    }
                    val hasSpeechActivity = try {
                        latinIME.packageManager.queryIntentActivities(
                            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0
                        ).isNotEmpty()
                    } catch (_: Throwable) {
                        false
                    }
                    if (!hasSpeechActivity) {
                        Log.w(TAG, "No speech recognition activity or engine found on device")
                        latinIME.uixManager.showAssistantMessage("ভয়েস ইনপুট সার্ভিস পাওয়া যায়নি (Google Speech Services প্রয়োজন)")
                        return
                    }
                    fallbackToIntent(locale)
                    return
                }
            }

            val langTag = getVoiceLanguageTag(locale)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, latinIME.packageName)
                // Extended silence tolerances allowing user to pause and breathe naturally without cutting off
                putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 5000L)
                putExtra("android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 5000L)
                putExtra("android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 5000L)
            }

            latinIME.uixManager.assistantMessage.value = null
            lastComposingLength = 0
            muteSoundIfConfigured()
            speechRecognizer?.startListening(intent)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting SpeechRecognizer", e)
            restoreSoundIfMuted()
            isListening = false
            mainHandler.post { 
                isListeningState.value = false 
                livePartialText.value = null
            }
            resetRecognizer()
            latinIME.uixManager.showAssistantMessage("মাইক্রোফোন পারমিশন প্রয়োজন")
        } catch (e: Throwable) {
            Log.e(TAG, "Error starting SpeechRecognizer, falling back to intent", e)
            restoreSoundIfMuted()
            isListening = false
            mainHandler.post { 
                isListeningState.value = false 
                livePartialText.value = null
            }
            resetRecognizer()
            fallbackToIntent(locale)
        }
    }

    private fun muteSoundIfConfigured() {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(latinIME)
            val soundEnabled = prefs.getBoolean(Settings.PREF_VOICE_INPUT_SOUND, true)
            if (!soundEnabled && !isStreamMutedForVoice) {
                val audioManager = latinIME.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                if (audioManager != null) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
                    isStreamMutedForVoice = true
                    // Safety auto-unmute after 2 seconds to ensure normal streams are never permanently muted
                    mainHandler.postDelayed({
                        restoreSoundIfMuted()
                    }, 5000L)
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to mute voice input stream", e)
        }
    }

    private fun restoreSoundIfMuted() {
        if (isStreamMutedForVoice) {
            try {
                val audioManager = latinIME.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
                audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to restore voice input stream", e)
            } finally {
                isStreamMutedForVoice = false
            }
        }
    }

    fun stopListening() {
        restoreSoundIfMuted()
        isListening = false
        mainHandler.post {
            isListeningState.value = false
            livePartialText.value = null
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping SpeechRecognizer", e)
            }
            latinIME.uixManager.assistantMessage.value = null
        }
    }

    fun destroy() {
        restoreSoundIfMuted()
        isListening = false
        mainHandler.post {
            isListeningState.value = false
            livePartialText.value = null
            resetRecognizer()
            latinIME.uixManager.assistantMessage.value = null
        }
    }

    private fun fallbackToIntent(locale: Locale) {
        VoiceInputFallbackActivity.start(latinIME, locale)
    }

    override fun onReadyForSpeech(params: Bundle?) {
        if (!isListening) return
        latinIME.uixManager.assistantMessage.value = null
    }

    override fun onBeginningOfSpeech() {
        if (!isListening) return
        latinIME.uixManager.assistantMessage.value = null
    }

    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        // Recognition completed an utterance; engine is processing, wait for onResults
    }

    override fun onError(error: Int) {
        Log.w(TAG, "Speech recognition error: $error")
        restoreSoundIfMuted()
        isListening = false
        mainHandler.post {
            isListeningState.value = false
            livePartialText.value = null
            lastComposingLength = 0
            resetRecognizer()
        }

        try {
            when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    try {
                        PermissionsActivity.run(
                            latinIME,
                            101,
                            Manifest.permission.RECORD_AUDIO
                        )
                    } catch (e: Throwable) {
                        Log.e(TAG, "PermissionsActivity failed on error", e)
                    }
                    latinIME.uixManager.showAssistantMessage("মাইক্রোফোন পারমিশন প্রয়োজন")
                }
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                    latinIME.uixManager.showAssistantMessage("ইন্টারনেট কানেকশন প্রয়োজন")
                }
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT, SpeechRecognizer.ERROR_NO_MATCH -> {
                    // User finished speaking or stayed silent: cleanly stop without popup or disturbance
                }
                else -> {
                    // Any other recognizer error: cleanly stopped and reset, keyboard stays open
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unexpected error handling speech onError", t)
        }
    }

    override fun onResults(results: Bundle?) {
        restoreSoundIfMuted()
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()

        mainHandler.post {
            livePartialText.value = null
            latinIME.uixManager.assistantMessage.value = null

            if (!text.isNullOrBlank()) {
                val ic = latinIME.currentInputConnection
                if (ic != null) {
                    ic.commitText("$text ", 1)
                }
            }
            lastComposingLength = 0

            // If user still has listening active, seamlessly restart recognizer for continuous dictation
            if (isListening && activeLocale != null) {
                resetRecognizer()
                mainHandler.postDelayed({
                    if (isListening && activeLocale != null) {
                        startRecognitionInternal(activeLocale!!)
                    }
                }, 150L)
            } else {
                isListening = false
                isListeningState.value = false
                resetRecognizer()
            }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        if (!isListening) return
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            mainHandler.post {
                // Live real-time transcription preview purely for UI display in ActionBar
                livePartialText.value = text
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
