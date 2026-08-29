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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.model.AssistantLanguage
import com.example.model.AssistantState
import com.example.ui.theme.Blue400
import com.example.ui.theme.Blue500
import com.example.ui.theme.Blue600
import com.example.ui.theme.Blue700
import com.example.ui.theme.Cyan400
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.SleekErrorRed
import com.example.ui.theme.SleekGold
import com.example.ui.theme.SleekMint
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import kotlin.math.sin

/**
 * Realistic 3D Talking Robot Head Avatar for Nova AI.
 * Includes synchronized robotic mouth speech lip-sync, glowing cybernetic eye sensors,
 * dynamic audio frequency halos, and responsive holographic reticles.
 */
@Composable
fun AuronAvatar(
    state: AssistantState,
    micAmplitude: Float,
    speakerAmplitude: Float,
    subtitle: String,
    language: AssistantLanguage = AssistantLanguage.BENGALI,
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "robot_motion")

    // Slow orbital rotation for HUD outer ring
    val hudRotationSlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slow_spin"
    )

    // Fast counter-spin for inner laser reticle
    val hudRotationFast by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fast_spin"
    )

    // Breathing idle pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    // Active accent color based on robot status
    val activeAccent by animateColorAsState(
        targetValue = when (state) {
            AssistantState.DISCONNECTED -> Cyan400.copy(alpha = 0.8f)
            AssistantState.CONNECTING -> SleekGold
            AssistantState.LISTENING -> Cyan400
            AssistantState.THINKING -> Indigo600
            AssistantState.SPEAKING -> SleekMint
            AssistantState.ERROR -> SleekErrorRed
        },
        animationSpec = tween(300),
        label = "accent_color"
    )

    // Dynamic scale driven by speech audio amplitude
    val reactiveScale = when (state) {
        AssistantState.SPEAKING -> 1f + (speakerAmplitude * 0.12f)
        AssistantState.LISTENING -> 1f + (micAmplitude * 0.10f)
        AssistantState.THINKING -> breathingPulse * 1.02f
        else -> breathingPulse
    }

    // Dynamic mouth opening calculation based on speaker amplitude
    val mouthOpening = when (state) {
        AssistantState.SPEAKING -> (speakerAmplitude.coerceIn(0.15f, 1f) * 18f).dp
        AssistantState.LISTENING -> (micAmplitude.coerceIn(0.05f, 0.5f) * 6f).dp
        AssistantState.THINKING -> 3.dp
        else -> 2.dp
    }

    val mouthWidth = when (state) {
        AssistantState.SPEAKING -> (34f + speakerAmplitude * 20f).dp
        AssistantState.LISTENING -> 36.dp
        else -> 32.dp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Centerpiece: Realistic 3D Robot Head with Holographic Ring System
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("auron_avatar_center")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAvatarClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // 1. Holographic HUD Canvas (Laser Arcs, Scanning Circles, Particle Halo)
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
                            activeAccent.copy(alpha = 0.35f),
                            Blue600.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.25f
                    )
                )

                // Concentric Ring 1 (Outer perimeter)
                drawCircle(
                    color = Blue400.copy(alpha = 0.18f),
                    radius = radius * 0.98f,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Concentric Ring 2 (Inner guide)
                drawCircle(
                    color = activeAccent.copy(alpha = 0.25f),
                    radius = radius * 0.88f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Orbiting Segmented HUD Arcs
                drawArc(
                    color = activeAccent.copy(alpha = 0.75f),
                    startAngle = hudRotationSlow,
                    sweepAngle = 65f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.88f, center.y - radius * 0.88f),
                    size = Size(radius * 1.76f, radius * 1.76f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                drawArc(
                    color = Cyan400.copy(alpha = 0.5f),
                    startAngle = hudRotationSlow + 180f,
                    sweepAngle = 55f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.88f, center.y - radius * 0.88f),
                    size = Size(radius * 1.76f, radius * 1.76f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Dotted High-Frequency Counter-Rotating Ring
                drawArc(
                    color = Blue400.copy(alpha = 0.45f),
                    startAngle = hudRotationFast,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.78f, center.y - radius * 0.78f),
                    size = Size(radius * 1.56f, radius * 1.56f),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                    )
                )

                // Speech Wave Arcs radiating from bottom mouth area when speaking
                if (state == AssistantState.SPEAKING && speakerAmplitude > 0.1f) {
                    val waveAmp = speakerAmplitude * 15f
                    drawArc(
                        color = SleekMint.copy(alpha = 0.7f),
                        startAngle = 70f,
                        sweepAngle = 40f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius * 0.65f, center.y - radius * 0.65f + waveAmp),
                        size = Size(radius * 1.3f, radius * 1.3f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 2. Realistic 3D Robot Head Container with Cybernetic Rim
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Blue700.copy(alpha = 0.45f),
                                Indigo900.copy(alpha = 0.4f),
                                SleekSurface
                            )
                        )
                    )
                    .border(2.dp, activeAccent.copy(alpha = 0.6f), CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Inner Robot Head Disc
                Box(
                    modifier = Modifier
                        .size(178.dp)
                        .clip(CircleShape)
                        .background(SleekSurfaceVariant)
                        .border(1.dp, GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Realistic 3D Robot Head Image
                    Image(
                        painter = painterResource(id = R.drawable.img_robot_head),
                        contentDescription = "Nova 3D Realistic Robot Head",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Holographic cybernetic scanning overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        activeAccent.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 3. Realistic Talking Mouth Aperture & Mechanical Lip-Sync Overlay
                    // Positioned exactly over the robot's lower jaw / mouth region
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer glowing mouth slot
                        Box(
                            modifier = Modifier
                                .width(mouthWidth)
                                .height(mouthOpening.coerceAtLeast(3.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            activeAccent.copy(alpha = 0.4f),
                                            activeAccent,
                                            activeAccent.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                                .border(1.dp, activeAccent.copy(alpha = 0.8f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner high-luminance speech frequency bar
                            if (state == AssistantState.SPEAKING) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(5) { index ->
                                        val barHeight = (mouthOpening * (0.5f + (index % 3) * 0.25f)).coerceAtLeast(2.dp)
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(barHeight)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Glowing Cybernetic Ocular Eyes Light Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Cyber Eye Glow
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(activeAccent.copy(alpha = if (state == AssistantState.SPEAKING) 0.9f else 0.7f))
                                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            )
                            // Right Cyber Eye Glow
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(activeAccent.copy(alpha = if (state == AssistantState.SPEAKING) 0.9f else 0.7f))
                                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            )
                        }
                    }
                }
            }

            // 5. Sleek Floating State Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(SleekSurface.copy(alpha = 0.96f))
                    .border(1.dp, activeAccent.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
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
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = state.getLabel(language).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Voice Subtitle / Spoken Transcript Card
        Box(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GlassBackground)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (subtitle.isNotBlank()) subtitle else if (language == AssistantLanguage.BENGALI) "\"নোভা প্রস্তুত। \"Hello Nova\" বলুন অথবা কথা বলুন\"" else "\"Nova is ready. Say 'Hello Nova' or speak.\"",
                    color = Slate100,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (state) {
                        AssistantState.DISCONNECTED -> if (language == AssistantLanguage.BENGALI) "হ্যান্ডস-ফ্রি ভয়েস চালু আছে" else "HANDS-FREE LISTENING ACTIVE"
                        AssistantState.LISTENING -> if (language == AssistantLanguage.BENGALI) "নোভা আপনার কথা শুনছে" else "NOVA IS LISTENING"
                        AssistantState.THINKING -> if (language == AssistantLanguage.BENGALI) "কমান্ড প্রসেস হচ্ছে..." else "PROCESSING COMMAND"
                        AssistantState.SPEAKING -> if (language == AssistantLanguage.BENGALI) "নোভা রোবট কথা বলছে" else "NOVA IS SPEAKING"
                        AssistantState.CONNECTING -> if (language == AssistantLanguage.BENGALI) "সার্ভারে যুক্ত হচ্ছে..." else "CONNECTING..."
                        AssistantState.ERROR -> if (language == AssistantLanguage.BENGALI) "অফলাইন কমান্ড মোড" else "OFFLINE MODE ACTIVE"
                    }.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
