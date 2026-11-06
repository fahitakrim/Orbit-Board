package org.futo.inputmethod.latin.uix.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.futo.inputmethod.latin.R

@OptIn(ExperimentalTextApi::class)
val Doto = FontFamily(
    Font(
        resId = R.font.doto,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.doto,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.weight(900))
    ),
)

@OptIn(ExperimentalTextApi::class)
val Inter = FontFamily(
    Font(
        resId = R.font.inter,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.inter,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.inter,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.inter,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
)

val Qurova = FontFamily(
    Font(resId = R.font.qurova, weight = FontWeight.Normal),
    Font(resId = R.font.qurova, weight = FontWeight.Medium)
)

val Agency = FontFamily(
    Font(resId = R.font.agency, weight = FontWeight.Normal),
    Font(resId = R.font.agency, weight = FontWeight.Medium)
)

val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = Doto,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Doto,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Doto,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
