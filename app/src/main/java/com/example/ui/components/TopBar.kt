package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantLanguage
import com.example.model.AssistantState
import com.example.ui.theme.Blue400
import com.example.ui.theme.Blue500
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.SleekElevated
import com.example.ui.theme.SleekErrorRed
import com.example.ui.theme.SleekGold
import com.example.ui.theme.SleekHeaderTop
import com.example.ui.theme.SleekMint

@Composable
fun TopBar(
    state: AssistantState,
    language: AssistantLanguage,
    sessionDurationSeconds: Long,
    onLanguageToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBengali = language == AssistantLanguage.BENGALI

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SleekHeaderTop,
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Nova Branding
            Column {
                Text(
                    text = if (isBengali) "ভয়েস অটোমেশন এআই" else "HANDS-FREE AI AGENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Blue400.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.size(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NOVA AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Blue500.copy(alpha = 0.2f))
                            .border(1.dp, Blue400.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isBengali) "বাংলা" else "ENG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue400
                        )
                    }
                }
            }

            // Right: Status Pill, Language Switch Pill, Settings Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language quick toggle pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassBackground)
                        .border(1.dp, GlassBorder, CircleShape)
                        .clickable { onLanguageToggle() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isBengali) "EN" else "বাং",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue400
                    )
                }

                StatusPill(state = state, language = language, durationSeconds = sessionDurationSeconds)

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GlassBackground)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Nova Settings",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    state: AssistantState,
    language: AssistantLanguage,
    durationSeconds: Long
) {
    val isBengali = language == AssistantLanguage.BENGALI
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pill_dot_pulse"
    )

    val (statusColor, statusText) = when (state) {
        AssistantState.DISCONNECTED -> Pair(Slate500, if (isBengali) "প্রস্তুত" else "STANDBY")
        AssistantState.CONNECTING -> Pair(SleekGold, if (isBengali) "যুক্ত হচ্ছে" else "CONNECTING")
        AssistantState.LISTENING -> Pair(Blue400, if (isBengali) "শুনছে" else "LIVE")
        AssistantState.THINKING -> Pair(Blue500, if (isBengali) "প্রসেসিং" else "PROCESSING")
        AssistantState.SPEAKING -> Pair(SleekMint, if (isBengali) "বলছে" else "SPEAKING")
        AssistantState.ERROR -> Pair(SleekErrorRed, if (isBengali) "অফলাইন" else "OFFLINE")
    }

    val animatedColor by animateColorAsState(targetValue = statusColor, label = "status_color")

    val durationText = if (durationSeconds > 0 && state != AssistantState.DISCONNECTED) {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        String.format(" · %02d:%02d", minutes, seconds)
    } else ""

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(GlassBackground)
            .border(1.dp, GlassBorder, CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state != AssistantState.DISCONNECTED) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(animatedColor.copy(alpha = 0.45f))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(animatedColor)
                )
            }

            Text(
                text = "$statusText$durationText",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Slate100,
                letterSpacing = 0.5.sp
            )
        }
    }
}
