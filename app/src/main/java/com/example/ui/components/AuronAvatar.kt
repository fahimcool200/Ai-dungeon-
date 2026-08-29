package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AssistantState
import com.example.ui.theme.Blue400
import com.example.ui.theme.Blue500
import com.example.ui.theme.Blue600
import com.example.ui.theme.Blue700
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo700
import com.example.ui.theme.Indigo900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.SleekElevated
import com.example.ui.theme.SleekErrorRed
import com.example.ui.theme.SleekGold
import com.example.ui.theme.SleekMint
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import kotlin.math.cos
import kotlin.math.sin

/**
 * AuronAvatar renders the Sleek Interface central visual centerpiece.
 * Features concentric glowing blue halo rings, subtle cyber reticle arcs,
 * backdrop blur gradient disk, and refined typography.
 */
@Composable
fun AuronAvatar(
    state: AssistantState,
    micAmplitude: Float,
    speakerAmplitude: Float,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_motion")

    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slow_spin"
    )

    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fast_spin"
    )

    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )

    val activeAccent by animateColorAsState(
        targetValue = when (state) {
            AssistantState.DISCONNECTED -> Blue400.copy(alpha = 0.5f)
            AssistantState.CONNECTING -> SleekGold
            AssistantState.LISTENING -> Blue400
            AssistantState.THINKING -> Indigo600
            AssistantState.SPEAKING -> SleekMint
            AssistantState.ERROR -> SleekErrorRed
        },
        animationSpec = tween(300),
        label = "accent_color"
    )

    val reactiveScale = when (state) {
        AssistantState.SPEAKING -> 1f + (speakerAmplitude * 0.18f)
        AssistantState.LISTENING -> 1f + (micAmplitude * 0.14f)
        AssistantState.THINKING -> ambientPulse * 1.02f
        AssistantState.CONNECTING -> ambientPulse * 1.01f
        else -> ambientPulse
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Sleek Luminous Reticle and Avatar Disc
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("auron_avatar_center"),
            contentAlignment = Alignment.Center
        ) {
            // Background soft ambient glow (w-[300px] h-[300px] bg-blue-600/10 blur)
            Canvas(
                modifier = Modifier
                    .size(280.dp)
                    .scale(reactiveScale)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Deep ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeAccent.copy(alpha = 0.25f),
                            Blue600.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.25f
                    )
                )

                // Concentric Ring 1: Outer subtle ring (scale 125, border-blue-400/10)
                drawCircle(
                    color = Blue400.copy(alpha = 0.12f),
                    radius = radius * 0.98f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Concentric Ring 2: Mid ring (scale 110, border-blue-500/20)
                drawCircle(
                    color = Blue500.copy(alpha = 0.22f),
                    radius = radius * 0.88f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Orbiting Segmented Arcs
                drawArc(
                    color = activeAccent.copy(alpha = 0.6f),
                    startAngle = rotationSlow,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.88f, center.y - radius * 0.88f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.76f, radius * 1.76f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                drawArc(
                    color = Blue400.copy(alpha = 0.35f),
                    startAngle = rotationSlow + 180f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.88f, center.y - radius * 0.88f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.76f, radius * 1.76f),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Counter-Rotating Inner Ring with Dotted Pattern
                drawArc(
                    color = Blue400.copy(alpha = 0.4f),
                    startAngle = rotationFast,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.78f, center.y - radius * 0.78f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.56f, radius * 1.56f),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
                    )
                )
            }

            // Sleek Gradient Core Disc
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Blue700.copy(alpha = 0.35f),
                                Indigo900.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Central Slate-900 Disk with Radial Blue Light
                Box(
                    modifier = Modifier
                        .size(174.dp)
                        .clip(CircleShape)
                        .background(SleekSurfaceVariant)
                        .border(1.dp, GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar Portrait
                    Image(
                        painter = painterResource(id = R.drawable.auron_avatar),
                        contentDescription = "Auron AI Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Luminous Radial Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        activeAccent.copy(alpha = 0.25f),
                                        Color.Transparent,
                                        SleekSurface.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )
                }
            }

            // Sleek Floating State Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(SleekSurface.copy(alpha = 0.95f))
                    .border(1.dp, GlassBorder, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val icon = when (state) {
                        AssistantState.LISTENING -> Icons.Default.Hearing
                        AssistantState.SPEAKING -> Icons.Default.GraphicEq
                        AssistantState.THINKING -> Icons.Default.Psychology
                        AssistantState.CONNECTING -> Icons.Default.Sensors
                        AssistantState.ERROR -> Icons.Default.Warning
                        else -> Icons.Default.AutoAwesome
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = state.label.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Sleek Subtitle / Voice Caption
        Box(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GlassBackground)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (subtitle.isNotBlank()) "\"$subtitle\"" else "\"Alright, I'm listening.\"",
                    color = Slate100,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.3).sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (state) {
                        AssistantState.DISCONNECTED -> "Auron is in standby"
                        AssistantState.LISTENING -> "Auron is listening"
                        AssistantState.THINKING -> "Auron is processing"
                        AssistantState.SPEAKING -> "Auron is speaking"
                        AssistantState.CONNECTING -> "Auron is connecting"
                        AssistantState.ERROR -> "Connection offline"
                    }.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate400,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

