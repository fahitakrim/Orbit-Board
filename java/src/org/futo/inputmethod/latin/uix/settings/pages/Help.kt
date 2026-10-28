package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import org.futo.inputmethod.latin.BuildConfig
import org.futo.inputmethod.latin.uix.getSetting
import org.futo.inputmethod.latin.uix.setSetting
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.ScrollableList
import org.futo.inputmethod.latin.uix.settings.Tip
import org.futo.inputmethod.updates.openURI

@Preview(showBackground = true)
@Composable
fun HelpScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current

    val numPresses = remember { mutableIntStateOf(0) }
    var lastToast: MutableState<Toast?> = remember { mutableStateOf(null) }

    ScrollableList {
        ScreenTitle("Help & Feedback", showBack = true, navController)

        NavigationItem(
            title = "Version Name: ${BuildConfig.VERSION_NAME}",
            style = NavigationItemStyle.MiscNoArrow,
            navigate = {

            }
        )
        NavigationItem(
            title = "Version Code: ${BuildConfig.VERSION_CODE}",
            style = NavigationItemStyle.MiscNoArrow,
            navigate = {
                val makeToast: (String) -> Unit = { text ->
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).let {
                        lastToast.value?.cancel()
                        lastToast.value = it
                        it.show()
                    }
                }
                if(runBlocking { context.getSetting(IS_DEVELOPER) }) {
                    makeToast("You're already a developer")
                } else {
                    numPresses.intValue += 1
                    if (numPresses.intValue > 3) {
                        val pressesUntilDeveloper = 8 - numPresses.intValue
                        if (pressesUntilDeveloper <= 0) {
                            makeToast("You're now a developer")
                            runBlocking { context.setSetting(IS_DEVELOPER, true) }
                        } else {
                            makeToast("You are $pressesUntilDeveloper steps until becoming a developer")
                        }
                    }
                }
            }
        )

        NavigationItem(
            title = "Report a Bug / Feedback",
            subtitle = "itz.takrim@gmail.com",
            style = NavigationItemStyle.Misc,
            navigate = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:itz.takrim@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Orbit Board - Bug Report / Feedback")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "\n\n---\nApp Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nDevice: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})"
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(intent, "Report a Bug").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            }
        )
    }
}