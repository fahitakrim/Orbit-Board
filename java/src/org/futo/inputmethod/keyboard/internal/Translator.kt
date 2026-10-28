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
        try {
            val encodedSource = URLEncoder.encode(source, "UTF-8")
            val encodedTarget = URLEncoder.encode(target, "UTF-8")
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$encodedSource&tl=$encodedTarget&dt=t&q=$encodedText"

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:100.0) Gecko/100.0 Firefox/100.0")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val outerArray = JSONArray(response)
                val sentences = outerArray.getJSONArray(0)
                val sb = StringBuilder()
                for (i in 0 until sentences.length()) {
                    val part = sentences.getJSONArray(i)
                    sb.append(part.getString(0))
                }
                val translatedText = sb.toString()
                Result.success(translatedText)
            } else {
                Log.e(TAG, "Translation HTTP error: $responseCode")
                Result.failure(Exception("Translation failed. HTTP status code: $responseCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation exception", e)
            Result.failure(e)
        }
    }

    class TranslationException(message: String, cause: Throwable? = null) : Exception(message, cause)
}