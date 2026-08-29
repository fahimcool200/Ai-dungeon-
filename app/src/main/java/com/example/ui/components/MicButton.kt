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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AssistantState
import com.example.ui.theme.Blue400
import com.example.ui.theme.Blue500
import com.example.ui.theme.Blue600
import com.example.ui.theme.Blue700
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo700
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekElevated
import com.example.ui.theme.SleekErrorRed
import com.example.ui.theme.SleekGold
import com.example.ui.theme.SleekMint

@Composable
fun MicButton(
    state: AssistantState,
    onClick: () -> Unit,
    onInterruptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_outer_pulse"
    )

    val gradientColors = when (state) {
        AssistantState.DISCONNECTED -> listOf(Blue600, Indigo700)
        AssistantState.CONNECTING -> listOf(SleekGold, Color(0xFFD97706))
        AssistantState.LISTENING -> listOf(Blue600, Indigo700)
        AssistantState.THINKING -> listOf(Indigo600, Color(0xFF6D28D9))
        AssistantState.SPEAKING -> listOf(SleekMint, Color(0xFF059669))
        AssistantState.ERROR -> listOf(SleekErrorRed, Color(0xFFDC2626))
    }

    val glowColor = when (state) {
        AssistantState.SPEAKING -> SleekMint
        AssistantState.ERROR -> SleekErrorRed
        AssistantState.CONNECTING -> SleekGold
        AssistantState.THINKING -> Indigo600
        else -> Blue600
    }

    val scaleModifier = if (isPressed) 0.94f else 1.0f

    Box(
        modifier = modifier
            .size(96.dp)
            .scale(scaleModifier),
        contentAlignment = Alignment.Center
    ) {
        // Outer Sleek Ring: border border-blue-500/20 (absolute -inset-2)
        Box(
            modifier = Modifier
                .size(92.dp)
                .scale(if (state != AssistantState.DISCONNECTED) pulseScale else 1f)
                .clip(CircleShape)
                .border(1.dp, Blue500.copy(alpha = 0.25f), CircleShape)
        )

        // Soft outer ambient aura
        if (state != AssistantState.DISCONNECTED) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = 0.15f))
            )
        }

        // Core Gradient Mic Action Button: bg-gradient-to-br from-blue-600 to-indigo-700
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .shadow(16.dp, CircleShape, spotColor = glowColor.copy(alpha = 0.6f))
                .background(
                    Brush.linearGradient(
                        colors = gradientColors
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = {
                        if (state == AssistantState.SPEAKING) {
                            onInterruptClick()
                        } else {
                            onClick()
                        }
                    }
                )
                .testTag("mic_button"),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (state) {
                AssistantState.DISCONNECTED -> Icons.Default.Mic
                AssistantState.CONNECTING -> Icons.Default.PowerSettingsNew
                AssistantState.LISTENING -> Icons.Default.Mic
                AssistantState.THINKING -> Icons.Default.Psychology
                AssistantState.SPEAKING -> Icons.Default.GraphicEq
                AssistantState.ERROR -> Icons.Default.Refresh
            }

            Icon(
                imageVector = icon,
                contentDescription = when (state) {
                    AssistantState.DISCONNECTED -> "Start Voice Conversation"
                    AssistantState.SPEAKING -> "Tap to Interrupt Auron"
                    AssistantState.ERROR -> "Tap to Retry Connection"
                    else -> "Microphone Active"
                },
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

