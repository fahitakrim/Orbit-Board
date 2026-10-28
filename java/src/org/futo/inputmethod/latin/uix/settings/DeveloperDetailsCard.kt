package org.futo.inputmethod.latin.uix.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import org.futo.inputmethod.latin.R
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter

private val NothingRed = Color(0xFFE5252A)
private val CardBorder = Color(0xFF26262B)
private val SurfaceVariant = Color(0xFF1B1B1F)
private val TextSecondary = Color(0xFF8E8E93)

@Composable
fun FacebookBrandIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.65f, h * 0.05f)
            lineTo(w * 0.45f, h * 0.05f)
            cubicTo(w * 0.32f, h * 0.05f, w * 0.28f, h * 0.16f, w * 0.28f, h * 0.28f)
            lineTo(w * 0.28f, h * 0.40f)
            lineTo(w * 0.15f, h * 0.40f)
            lineTo(w * 0.15f, h * 0.58f)
            lineTo(w * 0.28f, h * 0.58f)
            lineTo(w * 0.28f, h * 0.95f)
            lineTo(w * 0.46f, h * 0.95f)
            lineTo(w * 0.46f, h * 0.58f)
            lineTo(w * 0.62f, h * 0.58f)
            lineTo(w * 0.65f, h * 0.40f)
            lineTo(w * 0.46f, h * 0.40f)
            lineTo(w * 0.46f, h * 0.30f)
            cubicTo(w * 0.46f, h * 0.24f, w * 0.49f, h * 0.20f, w * 0.56f, h * 0.20f)
            lineTo(w * 0.65f, h * 0.20f)
            close()
        }
        drawPath(path, color = tint)
    }
}

@Composable
fun XBrandIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.08f, h * 0.08f)
            lineTo(w * 0.42f, h * 0.54f)
            lineTo(w * 0.08f, h * 0.92f)
            lineTo(w * 0.22f, h * 0.92f)
            lineTo(w * 0.48f, h * 0.62f)
            lineTo(w * 0.73f, h * 0.92f)
            lineTo(w * 0.92f, h * 0.92f)
            lineTo(w * 0.56f, h * 0.44f)
            lineTo(w * 0.88f, h * 0.08f)
            lineTo(w * 0.74f, h * 0.08f)
            lineTo(w * 0.50f, h * 0.35f)
            lineTo(w * 0.27f, h * 0.08f)
            close()
        }
        drawPath(path, color = tint)
    }
}

@Composable
fun EmailBrandIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.08f, h * 0.18f),
            size = Size(w * 0.84f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.12f, h * 0.12f),
            style = stroke
        )
        val flapPath = Path().apply {
            moveTo(w * 0.14f, h * 0.28f)
            lineTo(w * 0.50f, h * 0.55f)
            lineTo(w * 0.86f, h * 0.28f)
        }
        drawPath(flapPath, color = tint, style = stroke)
    }
}

@Composable
fun DeveloperDetailsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    NeuCard(modifier = modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariant)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.people_and_body),
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "Fahim Takrim",
                        fontFamily = Doto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Developer & Designer",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Social Profile Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Facebook
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                        .border(1.dp, CardBorder, CircleShape)
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.facebook.com/profile.php?id=61592810150809")
                            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    FacebookBrandIcon(modifier = Modifier.size(16.dp), tint = Color.White)
                }

                // X (Twitter)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                        .border(1.dp, CardBorder, CircleShape)
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://x.com/TakrimFahim")
                            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    XBrandIcon(modifier = Modifier.size(16.dp), tint = Color.White)
                }

                // GitHub
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                        .border(1.dp, CardBorder, CircleShape)
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/fahitakrim")
                            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.code),
                        contentDescription = "GitHub",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Email (Bug Report & Feedback)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                        .border(1.dp, CardBorder, CircleShape)
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:itz.takrim@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Orbit Board - Bug Report / Feedback")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(Intent.createChooser(intent, "Contact Developer").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    EmailBrandIcon(modifier = Modifier.size(16.dp), tint = Color.White)
                }
            }
        }
    }
}
