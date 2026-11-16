package org.futo.inputmethod.latin.uix

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey

val lastUsedEmoji = stringPreferencesKey("last_used_emoji")
val lastUsedColor = stringPreferencesKey("last_used_color")

object EmojiTracker {
    suspend fun Context.setLastUsedColor(color: String) {
        if(isDeviceLocked) return
        
        dataStore.edit {
            it[lastUsedColor] = color
        }
    }

    suspend fun Context.useEmoji(emoji: String) {
        if(isDeviceLocked) return

        dataStore.edit {
            val existing = (it[lastUsedEmoji] ?: "")
                .split("<|>")
                .map { e -> e.trim() }
                .filter { e -> e.isNotEmpty() && e != emoji.trim() }
            val combined = listOf(emoji.trim()) + existing
            it[lastUsedEmoji] = combined.take(66).joinToString("<|>")
        }
    }

    suspend fun Context.unuseEmoji(emoji: String) {
        if(isDeviceLocked) return

        dataStore.edit {
            val split = (it[lastUsedEmoji] ?: "").split("<|>")
            val idxToRemove = split.indexOfFirst { v -> v == emoji || v.trim() == emoji.trim() }
            it[lastUsedEmoji] = split.filterIndexed { i, _ -> i != idxToRemove}.joinToString("<|>")
        }
    }

    suspend fun Context.getRecentEmojis(): List<String> {
        if(isDeviceLocked) return emptyList()

        val raw = getSetting(lastUsedEmoji, "")
        if (raw.isBlank()) return emptyList()

        val userEmojis = raw.split("<|>")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        return userEmojis.take(66)
    }

    suspend fun Context.resetRecentEmojis() {
        if(isDeviceLocked) return

        setSetting(lastUsedEmoji, "")
    }
}