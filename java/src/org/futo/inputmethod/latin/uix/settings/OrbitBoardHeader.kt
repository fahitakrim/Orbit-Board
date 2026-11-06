package org.futo.inputmethod.latin.uix.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.futo.inputmethod.latin.BuildConfig
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.NothingRed

@Composable
fun CrescentMoonIcon(modifier: Modifier = Modifier, color: Color = NothingRed) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val moonPath = Path().apply {
            addOval(Rect(center.x - r, center.y - r, center.x + r, center.y + r))
        }
        val cutPath = Path().apply {
            addOval(Rect(center.x - r * 0.25f, center.y - r * 1.05f, center.x + r * 1.55f, center.y + r * 0.75f))
        }
        val result = Path().apply {
            op(moonPath, cutPath, PathOperation.Difference)
        }
        drawPath(result, color)
    }
}

@Composable
fun OrbitBoardHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Orbit Board",
                fontFamily = Doto,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = 1.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Simply bangla",
                fontFamily = Inter,
                fontSize = 13.sp,
                color = Color(0xFF8E8E93)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = Color(0xFF8E8E93)
            )
            Text(
                text = "|",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = Color(0xFF3E3E44)
            )
            CrescentMoonIcon(
                modifier = Modifier.size(16.dp),
                color = NothingRed
            )
        }
    }
}
