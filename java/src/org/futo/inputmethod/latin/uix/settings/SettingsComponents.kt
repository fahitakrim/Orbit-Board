package org.futo.inputmethod.latin.uix.settings

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.NothingRed

// =============================================================================
// Reusable Nothing OS / Orbit Design Tokens
// =============================================================================
val SettingsCardBackground = Color(0xFF141416)
val SettingsSurfaceVariant = Color(0xFF1B1B1F)
val SettingsBorderColor = Color(0xFF232326)
val SettingsDividerColor = Color(0xFF1E1E22)
val SettingsTextPrimary = Color(0xFFFFFFFF)
val SettingsTextSecondary = Color(0xFF8E8E93)
val SettingsCardCornerRadius = 22.dp

// =============================================================================
// 1. ExpandableSettingsCard
// =============================================================================
/**
 * Sleek Nothing OS accordion card featuring a 22dp corner radius, 1dp subtle border,
 * 34dp circular icon badge, dot-matrix Doto header, live summary, animated rotating chevron,
 * and smooth spring-based vertical expansion.
 */
@Composable
fun ExpandableSettingsCard(
    title: String,
    summary: String = "",
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = NothingRed,
    content: @Composable ColumnScope.() -> Unit
) {
    val view = LocalView.current
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "expandable_card_chevron"
    )

    NeuCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsCardCornerRadius),
        contentPadding = 0.dp,
        borderColor = SettingsBorderColor,
        borderWidth = 1.dp
    ) {
        // Clickable Accordion Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (_: Exception) {}
                    onToggle()
                }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isExpanded) accentColor.copy(alpha = 0.15f) else SettingsSurfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isExpanded) accentColor.copy(alpha = 0.5f) else SettingsBorderColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isExpanded) accentColor else SettingsTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        fontFamily = Doto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        letterSpacing = 0.5.sp,
                        color = SettingsTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = summary,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = SettingsTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = if (isExpanded) accentColor else SettingsTextSecondary,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(rotationAngle)
            )
        }

        // Fluid Animated Expansion Body
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(200)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(150)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SettingsBorderColor)
                )
                Spacer(modifier = Modifier.height(6.dp))
                content()
            }
        }
    }
}

// =============================================================================
// 2. SettingsRow
// =============================================================================
/**
 * Standard settings item row providing a minimum height of 64dp, structured title/subtitle,
 * optional leading icon, and constrained trailing control (switch, picker, pill button).
 */
@Composable
fun SettingsRow(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = 14.dp,
    icon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit = {}
) {
    val view = LocalView.current
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                try {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                } catch (_: Exception) {}
                onClick()
            }
        )
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .then(clickModifier)
            .padding(horizontal = horizontalPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(10.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SettingsTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = SettingsTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = SettingsTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier.widthIn(max = 200.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            trailingContent()
        }
    }
}

// =============================================================================
// 3. SegmentedControl
// =============================================================================
/**
 * Tactile Nothing OS capsule toggle with rounded pill tabs.
 * Supports custom accent colors, equal-weight stretching, and vibration feedback.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    equalWeight: Boolean = false,
    activeColor: Color = NothingRed,
    activeContentColor: Color = Color.White,
    selectedColors: List<Color>? = null,
    selectedContentColors: List<Color>? = null
) {
    val view = LocalView.current
    val containerShape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .clip(containerShape)
            .background(SettingsSurfaceVariant)
            .border(1.dp, SettingsBorderColor, containerShape)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            val itemShape = RoundedCornerShape(18.dp)
            val customBg = selectedColors?.getOrNull(index)
            val customContent = selectedContentColors?.getOrNull(index)

            val bg = when {
                isSelected && customBg != null -> customBg
                isSelected -> activeColor
                else -> Color.Transparent
            }
            val contentColor = when {
                isSelected && customContent != null -> customContent
                isSelected -> activeContentColor
                else -> SettingsTextSecondary
            }

            Box(
                modifier = (if (equalWeight) Modifier.weight(1f) else Modifier)
                    .clip(itemShape)
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (index != selectedIndex) {
                            try {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            } catch (_: Exception) {}
                            onSelect(index)
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}

// =============================================================================
// 4. PillButton
// =============================================================================
/**
 * 36dp/44dp capsule action button with tactile spring press feedback matching Orbit Music.
 * Supports filled and outlined styles, custom icon, and custom container colors.
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    filled: Boolean = true,
    height: Dp = 36.dp,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val view = LocalView.current
    val shape = RoundedCornerShape(50.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.8f),
        label = "pill_button_scale"
    )

    val effectiveContainerColor = containerColor ?: if (filled) NothingRed else Color.Transparent
    val effectiveContentColor = contentColor ?: if (filled) Color.White else SettingsTextPrimary
    val effectiveBorderColor = if (containerColor != null && !filled) containerColor else if (filled) Color.Transparent else SettingsBorderColor
    val horizontalPadding = if (height <= 36.dp) 14.dp else 20.dp

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .then(
                if (!filled || effectiveBorderColor != Color.Transparent) {
                    Modifier.border(1.dp, effectiveBorderColor, shape)
                } else Modifier
            )
            .background(effectiveContainerColor, shape)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (_: Exception) {}
                    onClick()
                }
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = effectiveContentColor,
                    modifier = Modifier.size(if (height <= 36.dp) 14.dp else 18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (height <= 36.dp) 12.sp else 13.5.sp,
                color = effectiveContentColor,
                maxLines = 1
            )
        }
    }
}

// =============================================================================
// 5. SettingsDivider
// =============================================================================
/**
 * 1dp hairline divider for clean separation between settings rows within an accordion card.
 */
@Composable
fun SettingsDivider(
    modifier: Modifier = Modifier,
    color: Color = SettingsDividerColor,
    thickness: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}
