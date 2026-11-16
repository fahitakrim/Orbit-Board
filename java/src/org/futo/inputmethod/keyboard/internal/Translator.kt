package org.futo.inputmethod.keyboard.internal

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object CloudTranslationHandler {
    private const val TAG = "CloudTranslationHandler"

    suspend fun translate(source: String, target: String, text: String): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext Result.success("")

        // 1. Primary: Chrome Dictionary Translation API
        val primaryResult = tryTranslate(
            urlBuilder = { s, t, q -> "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=$s&tl=$t&q=$q" },
            source = source,
            target = target,
            text = text
        )

        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        Log.w(TAG, "Primary translation endpoint failed, trying fallback gtx endpoint: ${primaryResult.exceptionOrNull()?.message}")

        // 2. Fallback: Google GTX Single Translation API
        tryTranslate(
            urlBuilder = { s, t, q -> "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$s&tl=$t&dt=t&q=$q" },
            source = source,
            target = target,
            text = text
        )
    }

    private fun tryTranslate(
        urlBuilder: (String, String, String) -> String,
        source: String,
        target: String,
        text: String
    ): Result<String> {
        return try {
            val encodedSource = URLEncoder.encode(source, "UTF-8")
            val encodedTarget = URLEncoder.encode(target, "UTF-8")
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = urlBuilder(encodedSource, encodedTarget, encodedText)

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            connection.connectTimeout = 7000
            connection.readTimeout = 7000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                if (response.isEmpty() || response.startsWith("<") || response.startsWith("<!DOCTYPE")) {
                    return Result.failure(Exception("Invalid server response (rate-limited or HTML)"))
                }
                val translatedText = parseTranslationResponse(response)
                if (translatedText.isNotEmpty()) {
                    Result.success(translatedText)
                } else {
                    Result.failure(Exception("Empty translation received"))
                }
            } else {
                Log.e(TAG, "Translation HTTP error: $responseCode")
                Result.failure(Exception("Translation failed. HTTP status code: $responseCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation exception", e)
            Result.failure(e)
        }
    }

    internal fun parseTranslationResponse(response: String): String {
        try {
            val outerArray = JSONArray(response)
            if (outerArray.length() > 0) {
                val firstItem = outerArray.opt(0)
                if (firstItem is String) {
                    // dict-chrome-ex without auto: ["Hello World"] or ["Hello ", "World"]
                    val sb = StringBuilder()
                    for (i in 0 until outerArray.length()) {
                        sb.append(outerArray.optString(i))
                    }
                    val result = sb.toString().trim()
                    if (result.isNotEmpty()) return result
                } else if (firstItem is JSONArray) {
                    if (firstItem.length() > 0) {
                        val nestedFirst = firstItem.opt(0)
                        if (nestedFirst is JSONArray) {
                            // gtx format: [[["sentence 1", "orig 1", ...], ["sentence 2", ...]], ...]
                            val sb = StringBuilder()
                            for (i in 0 until firstItem.length()) {
                                val part = firstItem.optJSONArray(i)
                                if (part != null && part.length() > 0) {
                                    sb.append(part.optString(0))
                                }
                            }
                            val result = sb.toString().trim()
                            if (result.isNotEmpty()) return result
                        } else if (nestedFirst is String) {
                            // dict-chrome-ex with auto: [["translated text", "detected_lang"]]
                            // The translated text is at index 0. Do NOT append index 1 which is the language code.
                            val result = nestedFirst.trim()
                            if (result.isNotEmpty()) return result
                        }
                    }
                } else if (firstItem != null) {
                    val str = firstItem.toString().trim()
                    if (str.isNotEmpty()) return str
                }
            }
        } catch (_: Throwable) {
            // Fall through to fallback
        }
        return parseTranslationResponseFallback(response)
    }

    internal fun parseTranslationResponseFallback(response: String): String {
        val trimmed = response.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return ""
        try {
            // Nested arrays (gtx): [[["translated", "orig", ...]], ...]
            if (trimmed.startsWith("[[[")) {
                val sb = StringBuilder()
                val pattern = java.util.regex.Pattern.compile("\\[\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
                val matcher = pattern.matcher(trimmed)
                while (matcher.find()) {
                    val match = matcher.group(1)
                    if (match != null && !match.contains("http") && !match.contains(".md")) {
                        sb.append(unescapeJson(match))
                    }
                }
                if (sb.isNotEmpty()) return sb.toString().trim()
            }
            // Auto dict-chrome-ex: [["translated text", "en"]]
            if (trimmed.startsWith("[[\"")) {
                val pattern = java.util.regex.Pattern.compile("^\\[\\[\"((?:[^\"\\\\]|\\\\.)*)\"")
                val matcher = pattern.matcher(trimmed)
                if (matcher.find()) {
                    val match = matcher.group(1)
                    if (match != null) return unescapeJson(match).trim()
                }
            }
            // Flat string array: ["translated 1", "translated 2"]
            val pattern = java.util.regex.Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"")
            val matcher = pattern.matcher(trimmed)
            val sb = StringBuilder()
            while (matcher.find()) {
                val match = matcher.group(1)
                if (match != null) {
                    sb.append(unescapeJson(match))
                }
            }
            return sb.toString().trim()
        } catch (_: Exception) {
            return ""
        }
    }

    private fun unescapeJson(s: String): String {
        return s.replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }

    class TranslationException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
