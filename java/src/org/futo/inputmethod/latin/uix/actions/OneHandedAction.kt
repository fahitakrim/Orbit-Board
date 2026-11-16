package org.futo.inputmethod.latin.uix.actions

import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.Action
import org.futo.inputmethod.latin.uix.KEYBOARD_DISPLAY_MODE
import org.futo.inputmethod.latin.uix.KeyboardDisplayMode
import org.futo.inputmethod.latin.uix.KeyboardDisplayModeManager
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.setSettingBlocking

val OneHandedAction = Action(
    icon = R.drawable.one_handed,
    name = R.string.action_one_handed,
    simplePressImpl = { manager, _ ->
        val context = manager.getContext()
        val currentMode = try {
            val currentStr = context.getSettingBlocking(KEYBOARD_DISPLAY_MODE)
            KeyboardDisplayMode.valueOf(currentStr)
        } catch (_: Throwable) {
            KeyboardDisplayMode.STANDARD
        }

        val newMode = when (currentMode) {
            KeyboardDisplayMode.ONE_HANDED_LEFT,
            KeyboardDisplayMode.ONE_HANDED_RIGHT -> KeyboardDisplayMode.STANDARD
            else -> KeyboardDisplayMode.ONE_HANDED_RIGHT
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
