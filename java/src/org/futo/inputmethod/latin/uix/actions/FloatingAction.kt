package org.futo.inputmethod.latin.uix.actions

import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.Action
import org.futo.inputmethod.latin.uix.KEYBOARD_DISPLAY_MODE
import org.futo.inputmethod.latin.uix.KeyboardDisplayMode
import org.futo.inputmethod.latin.uix.KeyboardDisplayModeManager
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.setSettingBlocking

val FloatingAction = Action(
    icon = R.drawable.floating,
    name = R.string.action_floating,
    simplePressImpl = { manager, _ ->
        val context = manager.getContext()
        val currentMode = try {
            val currentStr = context.getSettingBlocking(KEYBOARD_DISPLAY_MODE)
            KeyboardDisplayMode.valueOf(currentStr)
        } catch (_: Throwable) {
            KeyboardDisplayMode.STANDARD
        }

        val newMode = if (currentMode == KeyboardDisplayMode.FLOATING) {
            KeyboardDisplayMode.STANDARD
        } else {
            KeyboardDisplayMode.FLOATING
        }

        val oldMultiplier = KeyboardDisplayModeManager.getWidthMultiplier(context.resources)
        KeyboardDisplayModeManager.currentMode = newMode
        context.setSettingBlocking(KEYBOARD_DISPLAY_MODE.key, newMode.name)

        val latinIME = manager.getLatinIMEForDebug()
        if (oldMultiplier != KeyboardDisplayModeManager.getWidthMultiplier(context.resources)) {
            latinIME.reloadKeyboard()
        }
        if (newMode != KeyboardDisplayMode.FLOATING) {
            latinIME.clearFloatingBounds()
            latinIME.updateTouchableHeight()
        }
        manager.triggerContentUpdate()
        latinIME.window?.window?.decorView?.let { decor ->
            decor.post { decor.requestLayout() }
        }
    },
    windowImpl = null
)
