package org.futo.inputmethod.latin.uix

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey


val lastUsedEmoji = stringPreferencesKey("last_used_emoji")
val lastUsedColor = stringPreferencesKey("last_used_color")

val DEFAULT_RECENT_EMOJIS = listOf(
    "😂", "❤️", "😍", "🤣", "😊", "🙏", "😭", "😘",
    "👍", "🥺", "🔥", "🥰", "✨", "🎉", "👏", "😁",
    "😎", "😅", "💯", "💖", "🤔", "😉", "🙄", "💀",
    "💔", "😜", "🙈", "🥳", "🤗", "😴", "🤩", "🙌",
    "🤤", "👀", "🤝", "👌"
)

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
            it[lastUsedEmoji] = combined.take(128).joinToString("<|>")
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
        if(isDeviceLocked) return listOf()

        val raw = getSetting(lastUsedEmoji, "")
        val userEmojis = raw.split("<|>")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        // Maintain at least 32-36 emojis (3-4 full rows) so users always have rich recent emoji access
        val combined = (userEmojis + DEFAULT_RECENT_EMOJIS).distinct()
        return if (userEmojis.size >= 36) {
            userEmojis.take(48)
        } else {
            combined.take(36)
        }
    }

    suspend fun Context.resetRecentEmojis() {
        if(isDeviceLocked) return

        setSetting(lastUsedEmoji, "")
    }
}