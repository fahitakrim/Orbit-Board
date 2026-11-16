package org.futo.inputmethod.latin.uix.actions

import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.uix.Action
import org.futo.inputmethod.latin.uix.ActionWindow

@Composable
fun IconWithColor(@DrawableRes iconId: Int, iconColor: Color, modifier: Modifier = Modifier) {
    val icon = painterResource(id = iconId)

    Canvas(modifier = modifier) {
        translate(
            left = this.size.width / 2.0f - icon.intrinsicSize.width / 2.0f,
            top = this.size.height / 2.0f - icon.intrinsicSize.height / 2.0f
        ) {
            with(icon) {
                draw(
                    icon.intrinsicSize,
                    colorFilter = ColorFilter.tint(
                        iconColor
                    )
                )
            }
        }
    }
}

@Composable
fun TogglableKey(
    onToggle: (Boolean) -> Unit,
    toggled: Boolean,
    modifier: Modifier = Modifier,
    contents: @Composable (color: Color) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .padding(4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = { onToggle(!toggled) }
            ),
        shape = RoundedCornerShape(8.dp),
        color = if(toggled) { MaterialTheme.colorScheme.primary } else { MaterialTheme.colorScheme.secondaryContainer }
    ) {
        contents(if(toggled) { MaterialTheme.colorScheme.onPrimary } else { MaterialTheme.colorScheme.onSecondaryContainer })
    }

}

@Composable
fun Modifier.repeatablyClickableAction(repeatable: Boolean = true, onTrigger: (Boolean) -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val currentOnTrigger by rememberUpdatedState(onTrigger)

    return this
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(repeatable) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val press = PressInteraction.Press(down.position)
                scope.launch { interactionSource.emit(press) }

                currentOnTrigger(false)

                var repeatJob: Job? = null
                if (repeatable) {
                    repeatJob = scope.launch {
                        delay(670L)
                        while (isActive) {
                            currentOnTrigger(true)
                            delay(50L)
                        }
                    }
                }

                val up = waitForUpOrCancellation()
                repeatJob?.cancel()

                val endInteraction = if (up != null) {
                    PressInteraction.Release(press)
                } else {
                    PressInteraction.Cancel(press)
                }
                scope.launch { interactionSource.emit(endInteraction) }
            }
        }
}

@Composable
fun ActionKey(
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier,
    repeatable: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    contents: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(4.dp)
            .repeatablyClickableAction(
                repeatable = repeatable,
                onTrigger = { onTrigger() }
            ),
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        contents()
    }
}

@Composable
fun ArrowKeys(
    modifier: Modifier,
    moveCursor: (direction: Direction) -> Unit
) {
    Row(modifier = modifier) {
        ActionKey(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxHeight(),
            onTrigger = { moveCursor(Direction.Left) }
        ) {
            IconWithColor(
                iconId = R.drawable.arrow_left,
                iconColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        Column(modifier = Modifier
            .weight(1.0f)
            .fillMaxHeight()) {
            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                onTrigger = { moveCursor(Direction.Up) }
            ) {
                IconWithColor(
                    iconId = R.drawable.arrow_up,
                    iconColor = MaterialTheme.colorScheme.onPrimary
                )
            }


            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                onTrigger = { moveCursor(Direction.Down) }
            ) {
                IconWithColor(
                    iconId = R.drawable.arrow_down,
                    iconColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        ActionKey(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxHeight(),
            onTrigger = { moveCursor(Direction.Right) }
        ) {
            IconWithColor(
                iconId = R.drawable.arrow_right,
                iconColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun CtrlShiftMetaKeys(modifier: Modifier, ctrlState: MutableState<Boolean>, shiftState: MutableState<Boolean>) {
    Row(modifier = modifier) {
        TogglableKey(
            onToggle = { ctrlState.value = it },
            toggled = ctrlState.value,
            modifier = Modifier
                .weight(1.0f)
                .fillMaxHeight()
        ) {
            IconWithColor(
                iconId = R.drawable.ctrl,
                iconColor = it
            )
        }
        TogglableKey(
            onToggle = { shiftState.value = it },
            toggled = shiftState.value,
            modifier = Modifier
                .weight(1.0f)
                .fillMaxHeight()
        ) {
            IconWithColor(
                iconId = R.drawable.shift,
                iconColor = it
            )
        }
    }
}

@Composable
fun SideKeys(modifier: Modifier, onEditAction: (Int) -> Unit, onCodePoint: (Int) -> Unit, keyboardShown: Boolean) {
    Column(modifier = modifier) {
        Row(modifier = Modifier
            .weight(1.0f)
            .fillMaxWidth()) {
            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
                repeatable = false,
                color = MaterialTheme.colorScheme.primaryContainer,
                onTrigger = { onEditAction(android.R.id.selectAll) }
            ) {
                IconWithColor(
                    iconId = R.drawable.maximize,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
                repeatable = false,
                color = MaterialTheme.colorScheme.primaryContainer,
                onTrigger = { onEditAction(android.R.id.cut) }
            ) {
                IconWithColor(
                    iconId = R.drawable.scissors,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Row(modifier = Modifier
            .weight(1.0f)
            .fillMaxWidth()) {
            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
                repeatable = false,
                color = MaterialTheme.colorScheme.primaryContainer,
                onTrigger = { onEditAction(android.R.id.copy) }
            ) {
                IconWithColor(
                    iconId = R.drawable.copy,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
                repeatable = false,
                color = MaterialTheme.colorScheme.primaryContainer,
                onTrigger = { onEditAction(android.R.id.paste) }
            ) {
                IconWithColor(
                    iconId = R.drawable.clipboard,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if(!keyboardShown) {
            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                repeatable = true,
                color = MaterialTheme.colorScheme.primaryContainer,
                onTrigger = { onCodePoint(Constants.CODE_DELETE) }
            ) {
                IconWithColor(
                    iconId = R.drawable.delete,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }


        Row(modifier = Modifier
            .weight(1.0f)
            .fillMaxWidth()) {
            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
                repeatable = false,
                color = MaterialTheme.colorScheme.primaryContainer,
                onTrigger = { onEditAction(android.R.id.undo) }
            ) {
                IconWithColor(
                    iconId = R.drawable.undo,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            ActionKey(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
                repeatable = false,
                color = MaterialTheme.colorScheme.primaryContainer,
                onTrigger = { onEditAction(android.R.id.redo) }
            ) {
                IconWithColor(
                    iconId = R.drawable.redo,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

enum class Direction {
    Left,
    Right,
    Up,
    Down
}

@Composable
fun TextEditScreen(
    onCodePoint: (Int) -> Unit,
    onEditAction: (Int) -> Unit,
    moveCursor: (direction: Direction, ctrl: Boolean, shift: Boolean) -> Unit,
    keyboardShown: Boolean
) {
    val shiftState = remember { mutableStateOf(false) }
    val ctrlState = remember { mutableStateOf(false) }

    val sendMoveCursor = { direction: Direction -> moveCursor(direction, ctrlState.value, shiftState.value) }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .weight(3.0f)) {
            ArrowKeys(
                modifier = Modifier
                    .weight(3.0f)
                    .fillMaxWidth(),
                moveCursor = sendMoveCursor
            )
            CtrlShiftMetaKeys(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                ctrlState = ctrlState,
                shiftState = shiftState
            )
        }
        SideKeys(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.0f),
            onEditAction = onEditAction,
            onCodePoint = onCodePoint,
            keyboardShown = keyboardShown
        )
    }
}

val TextEditAction = Action(
    icon = R.drawable.edit_text,
    name = R.string.text_edit_action_title,
    simplePressImpl = null,
    persistentState = null,
    canShowKeyboard = true,
    windowImpl = { manager, persistentState ->
        object : ActionWindow {
            @Composable
            override fun windowName(): String {
                return stringResource(R.string.text_edit_action_title)
            }

            @Composable
            override fun WindowContents(keyboardShown: Boolean) {
                val view = LocalView.current
                TextEditScreen(
                    onCodePoint = { a ->
                        manager.sendCodePointEvent(a)
                        manager.performHapticAndAudioFeedback(a, view)
                    },
                    onEditAction = { actionId ->
                        manager.performEditAction(actionId)
                        manager.performHapticAndAudioFeedback(Constants.CODE_TAB, view)
                    },
                    moveCursor = { direction, ctrl, shift ->
                        val keyEventMetaState = 0 or
                                (if(shift) { KeyEvent.META_SHIFT_ON } else { 0 }) or
                                (if(ctrl) { KeyEvent.META_CTRL_ON } else { 0 })

                        when(direction) {
                            Direction.Left -> manager.cursorLeft(1, stepOverWords = ctrl, select = shift)
                            Direction.Right -> manager.cursorRight(1, stepOverWords = ctrl, select = shift)
                            Direction.Up -> manager.sendKeyEvent(KeyEvent.KEYCODE_DPAD_UP, keyEventMetaState)
                            Direction.Down -> manager.sendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, keyEventMetaState)
                        }

                        manager.performHapticAndAudioFeedback(Constants.CODE_TAB, view)
                    },
                    keyboardShown = keyboardShown
                )
            }

            override fun close() {
            }
        }
    }
)

@Composable
@Preview(showBackground = true)
fun TextEditScreenPreview() {
    Surface(modifier = Modifier.height(256.dp)) {
        TextEditScreen(onCodePoint = { }, onEditAction = { }, moveCursor = { _, _, _ -> }, keyboardShown = false)
    }
}
@Composable
@Preview(showBackground = true)
fun TextEditScreenPreviewWithKb() {
    Surface(modifier = Modifier.height(256.dp)) {
        TextEditScreen(onCodePoint = { }, onEditAction = { }, moveCursor = { _, _, _ -> }, keyboardShown = true)
    }
}