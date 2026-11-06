package org.futo.inputmethod.latin.uix

import android.content.Context
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView


class ActionEditText(context: Context) :
    androidx.appcompat.widget.AppCompatEditText(context) {
    var inputConnection: InputConnection? = null
        private set

    init {
        isCursorVisible = true
        isFocusable = true
        isFocusableInTouchMode = true
        privateImeOptions = "org.futo.inputmethod.latin.NoSuggestions=1"
        inputType = inputType or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val cursorDrawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                setColor(0xFFD71921.toInt()) // Accent red
                setSize((2.5f * context.resources.displayMetrics.density).toInt().coerceAtLeast(3), 0)
            }
            textCursorDrawable = cursorDrawable
        } else {
            try {
                val fCursorDrawableRes = android.widget.TextView::class.java.getDeclaredField("mCursorDrawableRes")
                fCursorDrawableRes.isAccessible = true
                fCursorDrawableRes.set(this, 0)
            } catch (_: Exception) {}
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        outAttrs.inputType = outAttrs.inputType or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        outAttrs.privateImeOptions = "org.futo.inputmethod.latin.NoSuggestions=1"
        inputConnection = super.onCreateInputConnection(outAttrs)
        return inputConnection
    }

    private var textChanged: (String) -> Unit = { }
    fun setTextChangeCallback(
        textChanged: (String) -> Unit
    ) {
        this.textChanged = textChanged
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        // For some strange reason this IS null sometimes, even though it
        // shouldn't be
        if(textChanged != null) {
            textChanged(text?.toString() ?: "")
        }
    }
}


@Composable
fun ActionTextEditor(text: MutableState<String>) {
    val context = LocalContext.current
    val manager = LocalManager.current

    val height = with(LocalDensity.current) {
        48.dp.toPx()
    }

    val inputType = EditorInfo.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

    val color = LocalContentColor.current

    AndroidView(
        factory = {
            ActionEditText(context).apply {
                this.inputType = inputType
                isCursorVisible = true
                isFocusable = true
                isFocusableInTouchMode = true

                setTextChangeCallback { text.value = it }

                setText(text.value)
                setTextColor(color.toArgb())

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                privateImeOptions = "org.futo.inputmethod.latin.NoSuggestions=1"

                setHeight(height.toInt())

                val editorInfo = EditorInfo().apply {
                    this.inputType = inputType
                    this.privateImeOptions = "org.futo.inputmethod.latin.NoSuggestions=1"
                }
                onCreateInputConnection(editorInfo)

                manager.overrideInputConnection(inputConnection!!, editorInfo)

                requestFocus()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        onRelease = {
            manager.unsetInputConnection()
        }
    )
}
