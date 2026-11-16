package org.futo.inputmethod.latin.uix

import android.content.Context
import org.futo.inputmethod.latin.R
import org.futo.voiceinput.shared.types.ModelFileFile
import org.futo.voiceinput.shared.types.ModelLoader
import java.util.Locale

/**
 * Non-blocking coroutine extension on ResourceHelper to find voice input model for a given locale.
 */
suspend fun ResourceHelper.findVoiceInputModelForLocale(context: Context, locale: Locale): ModelLoader? {
    val file = findFileForKind(context, locale, FileKind.VoiceInput)
        ?: return BuiltInVoiceInputFallbacks[locale.language]

    return ModelFileFile(R.string.externally_imported_model, file)
}
