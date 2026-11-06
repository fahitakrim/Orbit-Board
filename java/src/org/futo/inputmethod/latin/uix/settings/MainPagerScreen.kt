package org.futo.inputmethod.latin.uix.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.futo.inputmethod.latin.uix.settings.pages.KeyboardCustomizationScreen
import org.futo.inputmethod.latin.uix.settings.pages.MoreSettingsScreen
import org.futo.inputmethod.latin.uix.settings.pages.MySpaceScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    val isImeVisible = imeBottom > 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0C))
    ) {
        // 1. HorizontalPager with 3 core tabs:
        // Tab 0: "My space"
        // Tab 1: "Keyboard"
        // Tab 2: "More"
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> MySpaceScreen(navController = navController)
                1 -> KeyboardCustomizationScreen(navController = navController)
                2 -> MoreSettingsScreen(navController = navController)
            }
        }

        // 2. Floating Concentric Capsule Pill Navigation Dock
        if (!isImeVisible) {
            NothingBottomNavDock(
                pagerState = pagerState,
                isDarkMode = true,
                onTabSelected = { targetPage ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            page = targetPage,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 18.dp)
            )
        }
    }
}
