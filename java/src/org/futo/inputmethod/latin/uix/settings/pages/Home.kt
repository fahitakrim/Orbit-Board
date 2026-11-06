package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.futo.inputmethod.announcements.Announcement
import org.futo.inputmethod.announcements.AnnouncementManager
import org.futo.inputmethod.announcements.AnnouncementSheet
import org.futo.inputmethod.latin.ActiveSubtype
import org.futo.inputmethod.latin.BuildConfig
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.Subtypes
import org.futo.inputmethod.latin.SubtypesSetting
import org.futo.inputmethod.latin.uix.TextEditPopupActivity
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.setSettingBlocking
import org.futo.inputmethod.latin.uix.settings.DeveloperDetailsCard
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.NeuCard
import org.futo.inputmethod.latin.uix.settings.SectionHeader
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsBool
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.updates.AppUpdate
import org.futo.inputmethod.updates.UpdateChecker
import org.futo.inputmethod.updates.UpdateSheet

private val NothingRed = Color(0xFFE5252A)
private val CardBackground = Color(0xFF0E0E11)
private val SurfaceVariant = Color(0xFF1B1B1F)
private val BorderColor = Color(0xFF26262B)
private val DividerColor = Color(0xFF1E1E22)
private val TextSecondary = Color(0xFF8E8E93)

@Composable
fun BengaliLayoutToggleRow(
    title: String,
    subtitle: String,
    active: Boolean,
    canToggle: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = active,
            enabled = canToggle,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NothingRed,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeScreen(navController: NavHostController = rememberNavController()) {
    org.futo.inputmethod.latin.uix.settings.MainPagerScreen(navController = navController)
}
