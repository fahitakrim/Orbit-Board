package org.futo.inputmethod.latin.uix.theme.presets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.theme.ThemeOption

// Taiyaki Bakery / Biscuit Cafe Color Palette
val BakeryBgCream = Color(0xFFFBF4DE)        // Soft warm custard cream
val BakeryTopCoffee = Color(0xFF7A5B43)      // Warm coffee header banner
val BakeryToastCrust = Color(0xFF683F1D)     // Roasted toast crust outline
val BakeryBiscuitGold = Color(0xFFFEE8C4)    // Golden biscuit key
val BakeryBreadCream = Color(0xFFFFF3DC)     // Toasted bread slice key
val BakeryEnterTeal = Color(0xFFA4DBE8)      // Pastel sky/mint enter key
val BakeryEnterBorder = Color(0xFF4C7B87)
val BakeryChocolateText = Color(0xFF3C2415)  // Rich dark chocolate typography
val BakeryMeltedButter = Color(0xFFE8B872)   // Pressed key highlight / honey butter
val BakeryAltHint = Color(0xFF8C6647)        // Hint / secondary text
val BakeryIvoryTicket = Color(0xFFFFF9E6)    // Shift / tag card
val BakeryDeleteCrumb = Color(0xFFFADBB7)    // Crumb loaf

private val bakeryColorScheme = lightColorScheme(
    primary = Color(0xFFC78446),             // Warm caramel amber accent (buttons, highlights)
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = BakeryBiscuitGold,
    onPrimaryContainer = BakeryChocolateText,
    secondary = BakeryEnterTeal,
    onSecondary = Color(0xFF204953),
    secondaryContainer = BakeryBreadCream,
    onSecondaryContainer = BakeryChocolateText,
    tertiary = BakeryMeltedButter,
    onTertiary = BakeryChocolateText,
    background = BakeryTopCoffee,            // Suggestion strip / action bar background = warm coffee banner
    onBackground = Color(0xFFFFF7EB),        // Soft cream text & icons on the coffee banner
    surface = BakeryBgCream,                 // Keyboard surface = warm custard cream
    onSurface = BakeryChocolateText,         // Chocolate brown text
    surfaceVariant = BakeryBreadCream,
    onSurfaceVariant = BakeryChocolateText,
    outline = BakeryToastCrust,              // Toast crust border
    outlineVariant = BakeryAltHint
)

val BakeryTheme = ThemeOption(
    dynamic = false,
    key = "BakeryTheme",
    name = R.string.theme_bakery,
    available = { true }
) {
    bakeryColorScheme
}

@Composable
fun BakeryPreviewKeyboard(
    modifier: Modifier = Modifier,
    keyHeight: androidx.compose.ui.unit.Dp = 16.dp
) {
    val biscuit = BakeryBiscuitGold
    val bread = BakeryBreadCream
    val teal = BakeryEnterTeal
    val ivory = BakeryIvoryTicket
    val crumb = BakeryDeleteCrumb
    val stroke = BorderStroke(1.dp, BakeryToastCrust)
    val tealStroke = BorderStroke(1.dp, BakeryEnterBorder)

    val row1 = listOf(biscuit, bread, biscuit, bread, biscuit, bread, biscuit, bread, biscuit, crumb)
    val row2 = listOf(bread, biscuit, bread, biscuit, bread, biscuit, bread, biscuit, bread)
    val row3 = listOf(ivory, biscuit, bread, biscuit, bread, biscuit, crumb)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BakeryBgCream)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row1.forEach { color ->
                Surface(
                    modifier = Modifier.weight(1f).height(keyHeight),
                    color = color,
                    border = stroke,
                    shape = RoundedCornerShape(4.dp)
                ) {}
            }
        }
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row2.forEach { color ->
                Surface(
                    modifier = Modifier.weight(1f).height(keyHeight),
                    color = color,
                    border = stroke,
                    shape = RoundedCornerShape(4.dp)
                ) {}
            }
        }
        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row3.forEachIndexed { idx, color ->
                val weight = if (idx == 0 || idx == row3.size - 1) 1.5f else 1f
                Surface(
                    modifier = Modifier.weight(weight).height(keyHeight),
                    color = color,
                    border = stroke,
                    shape = RoundedCornerShape(4.dp)
                ) {}
            }
        }
        // Bottom Row: 123, spacebar, enter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            Surface(
                modifier = Modifier.weight(1.3f).height(keyHeight),
                color = ivory,
                border = stroke,
                shape = RoundedCornerShape(4.dp)
            ) {}
            Surface(
                modifier = Modifier.weight(4.5f).height(keyHeight),
                color = biscuit,
                border = stroke,
                shape = RoundedCornerShape(6.dp)
            ) {}
            Surface(
                modifier = Modifier.weight(1.5f).height(keyHeight),
                color = teal,
                border = tealStroke,
                shape = RoundedCornerShape(4.dp)
            ) {}
        }
    }
}
