package org.futo.inputmethod.latin.uix.actions

import org.futo.inputmethod.keyboard.KeyboardSwitcher
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.Action

val NumberPadAction = Action(
    icon = R.drawable.dialpad,
    name = R.string.action_number_pad,
    simplePressImpl = { _, _ ->
        KeyboardSwitcher.getInstance().toggleNumberKeyboard()
    },
    windowImpl = null,
)
