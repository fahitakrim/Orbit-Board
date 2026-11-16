package org.futo.inputmethod.latin.uix.voiceinput

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import org.futo.inputmethod.latin.LatinIME
import java.util.Locale

class VoiceInputFallbackActivity : Activity() {
    companion object {
        private const val TAG = "VoiceFallbackActivity"
        private const val REQUEST_SPEECH = 2001
        const val EXTRA_LANG = "language"
        const val EXTRA_PROMPT = "prompt"
        var pendingText: String? = null

        fun start(context: Context, locale: Locale) {
            val intent = Intent(context, VoiceInputFallbackActivity::class.java).apply {
                val langTag = when (locale.language) {
                    "bn" -> if (locale.country.isNotEmpty()) locale.toLanguageTag() else "bn-BD"
                    "en" -> if (locale.country.isNotEmpty()) locale.toLanguageTag() else "en-US"
                    else -> locale.toLanguageTag()
                }
                putExtra(EXTRA_LANG, langTag)
                putExtra(EXTRA_PROMPT, if (locale.language == "bn") "বলুন..." else "Speak now...")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start fallback activity", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lang = intent.getStringExtra(EXTRA_LANG) ?: "bn-BD"
        val prompt = intent.getStringExtra(EXTRA_PROMPT) ?: "Speak now..."

        try {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang)
                putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 5000L)
                putExtra("android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 5000L)
                putExtra("android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 5000L)
            }
            startActivityForResult(speechIntent, REQUEST_SPEECH)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch ACTION_RECOGNIZE_SPEECH", e)
            try {
                LatinIME.instance?.uixManager?.showAssistantMessage("ভয়েস ইনপুট সার্ভিস পাওয়া যায়নি (Google Speech Services প্রয়োজন)")
            } catch (_: Throwable) {}
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK && data != null) {
            val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull()?.trim()
            if (!text.isNullOrBlank()) {
                val formatted = "$text "
                pendingText = formatted
                try {
                    val ic = LatinIME.instance?.currentInputConnection
                    if (ic != null) {
                        ic.commitText(formatted, 1)
                        pendingText = null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to commit recognized text", e)
                }
            }
        }
        finish()
    }
}
