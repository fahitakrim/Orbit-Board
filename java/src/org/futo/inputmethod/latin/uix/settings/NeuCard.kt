package org.futo.inputmethod.latin.uix.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DefaultNeuCardShape = RoundedCornerShape(16.dp)

/**
 * Neumorphic card component providing AMOLED matte surface, 1dp hairline border,
 * and tactile spring press feedback matching Orbit Music's Nothing OS design.
 */
@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    shape: Shape = DefaultNeuCardShape,
    contentPadding: Dp = 8.dp,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = if (onClick != null) remember { MutableInteractionSource() } else null
    val isPressed = interactionSource?.collectIsPressedAsState()?.value ?: false

    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.8f),
        label = "neu_card_scale"
    )

    val effectiveBorderColor = borderColor ?: Color(0xFF232326)
    val backgroundColor = Color(0xFF141416)

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = scaleState.value
                if (s != 1.0f) {
                    scaleX = s
                    scaleY = s
                }
            }
            .clip(shape)
            .background(backgroundColor, shape)
            .border(borderWidth, effectiveBorderColor, shape)
            .then(
                if (onClick != null && interactionSource != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(contentPadding)
    ) {
        Column(content = content)
    }
}
