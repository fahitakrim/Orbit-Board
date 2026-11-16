package org.futo.inputmethod.latin.uix.actions

import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.Action

val SelectAllAction = Action(
    icon = R.drawable.maximize,
    name = R.string.select_all_action_title,
    simplePressImpl = { manager, _ ->
        manager.performEditAction(android.R.id.selectAll)
    },
    windowImpl = null,
)

val CutAction = Action(
    icon = R.drawable.scissors,
    name = R.string.cut_action_title,
    simplePressImpl = { manager, _ ->
        manager.performEditAction(android.R.id.cut)
    },
    windowImpl = null,
)

val CopyAction = Action(
    icon = R.drawable.copy,
    name = R.string.copy_action_title,
    simplePressImpl = { manager, _ ->
        manager.performEditAction(android.R.id.copy)
    },
    windowImpl = null,
)

val PasteAction = Action(
    icon = R.drawable.clipboard,
    name = R.string.clipboard_action_title,
    simplePressImpl = { manager, _ ->
        manager.performEditAction(android.R.id.paste)
    },
    windowImpl = null,
)