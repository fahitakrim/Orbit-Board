package org.futo.inputmethod.latin.uix.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.futo.inputmethod.latin.uix.theme.NothingBlue
import org.futo.inputmethod.latin.uix.theme.NothingDarkDock
import org.futo.inputmethod.latin.uix.theme.NothingLightDock
import org.futo.inputmethod.latin.uix.theme.NothingRed
import org.futo.inputmethod.latin.uix.theme.NothingYellow
import kotlin.math.absoluteValue

/**
 * Nothing OS Symmetrical Concentric Capsule Pill Navigation Dock:
 * Ported identically from Orbitwall (MainApp.kt:582).
 * - Outer capsule: 56dp height with 28dp radius
 * - Inner margin: 4dp padding all around
 * - Inner sliding indicator: 48dp height with 24dp radius (concentric curvature R_inner = R_outer - padding)
 * - Symmetrically blends with outer capsule edges on left (My space) and right (More)
 * - Swipe gestures on dock itself and full screen seamlessly shift active pill
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NothingBottomNavDock(
    pagerState: PagerState,
    isDarkMode: Boolean,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalDragDelta by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        totalDragDelta += delta
    }

    // Continuous page position: [0.0f .. 2.0f] using derivedStateOf to prevent recomposition spikes
    val continuousPage by remember {
        derivedStateOf {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 2f)
        }
    }

    val homePillColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
    val activeIndicatorColor by remember(isDarkMode) {
        derivedStateOf {
            when {
                continuousPage <= 1f -> lerp(homePillColor, NothingRed, continuousPage)
                else -> lerp(NothingRed, NothingYellow, continuousPage - 1f)
            }
        }
    }

    val dockBorderColor = if (isDarkMode) NothingYellow.copy(alpha = 0.50f) else Color(0xFFE2B438)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = if (isDarkMode) NothingDarkDock else NothingLightDock,
        border = BorderStroke(1.2.dp, dockBorderColor),
        shadowElevation = 16.dp,
        modifier = modifier
            .height(56.dp)
            .width(240.dp)
            .clip(RoundedCornerShape(28.dp))
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity ->
                    val threshold = 30f
                    if (velocity < -200f || totalDragDelta < -threshold) {
                        // Swiped left -> move right (next tab)
                        val target = (pagerState.currentPage + 1).coerceAtMost(2)
                        onTabSelected(target)
                    } else if (velocity > 200f || totalDragDelta > threshold) {
                        // Swiped right -> move left (prev tab)
                        val target = (pagerState.currentPage - 1).coerceAtLeast(0)
                        onTabSelected(target)
                    }
                    totalDragDelta = 0f
                }
            )
            .testTag("floating_dock")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val totalWidth = maxWidth
                val slotWidth = totalWidth / 3f

                // 1. Thin Vertical Divider Lines (|) Layer
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(NothingYellow.copy(alpha = 0.22f))
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(NothingYellow.copy(alpha = 0.22f))
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 2. Sliding Indicator Pill Layer (Concentric 24dp radius matching 28dp outer - 4dp padding)
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = slotWidth.toPx() * continuousPage
                        }
                        .width(slotWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(activeIndicatorColor)
                )

                // 3. Tab Icons & Interactive Click Targets Layer
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab 0: My space
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(0) }
                    ) {
                        val dist0 = (continuousPage - 0f).absoluteValue.coerceIn(0f, 1f)
                        val iconTint = lerp(
                            start = if (isDarkMode) Color.Black else MaterialTheme.colorScheme.background,
                            stop = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            fraction = (dist0 * 2f).coerceIn(0f, 1f)
                        )
                        Icon(
                            imageVector = if (continuousPage < 0.5f) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "My space",
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Tab 1: Keyboard
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(1) }
                    ) {
                        val dist1 = (continuousPage - 1f).absoluteValue.coerceIn(0f, 1f)
                        val iconTint = lerp(
                            start = Color.White,
                            stop = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            fraction = (dist1 * 2f).coerceIn(0f, 1f)
                        )
                        Icon(
                            imageVector = if (continuousPage in 0.5f..1.5f) Icons.Filled.Keyboard else Icons.Outlined.Keyboard,
                            contentDescription = "Keyboard",
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Tab 2: More
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(2) }
                    ) {
                        val dist2 = (continuousPage - 2f).absoluteValue.coerceIn(0f, 1f)
                        val iconTint = lerp(
                            start = if (isDarkMode) Color.Black else Color.White,
                            stop = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            fraction = (dist2 * 2f).coerceIn(0f, 1f)
                        )
                        Icon(
                            imageVector = if (continuousPage > 1.5f) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz,
                            contentDescription = "More",
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
