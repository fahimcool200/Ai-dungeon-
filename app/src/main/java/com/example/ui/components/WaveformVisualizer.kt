package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.AssistantState
import com.example.ui.theme.Blue400
import com.example.ui.theme.Blue500
import com.example.ui.theme.IceBlue
import com.example.ui.theme.Indigo600
import com.example.ui.theme.SleekErrorRed
import com.example.ui.theme.SleekGold
import com.example.ui.theme.SleekMint

@Composable
fun WaveformVisualizer(
    state: AssistantState,
    bars: List<Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.DISCONNECTED -> Blue400.copy(alpha = 0.35f)
            AssistantState.CONNECTING -> SleekGold
            AssistantState.LISTENING -> Blue400
            AssistantState.THINKING -> Indigo600
            AssistantState.SPEAKING -> SleekMint
            AssistantState.ERROR -> SleekErrorRed
        },
        animationSpec = tween(250),
        label = "waveform_color"
    )

    val secondaryColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.DISCONNECTED -> Blue500.copy(alpha = 0.2f)
            AssistantState.CONNECTING -> Blue400
            AssistantState.LISTENING -> IceBlue
            AssistantState.THINKING -> Blue400
            AssistantState.SPEAKING -> Blue400
            AssistantState.ERROR -> SleekGold
        },
        animationSpec = tween(250),
        label = "waveform_sec_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .matchParentSize()
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val count = bars.size
            if (count == 0) return@Canvas

            val barSpacing = width / count
            val barWidth = (barSpacing * 0.45f).coerceIn(3.5f, 6.5f)

            // Draw sleek vertical EQ pill bars
            for (i in 0 until count) {
                val amp = bars[i]
                val barHeight = (amp * height * 0.9f).coerceAtLeast(6f)
                val x = i * barSpacing + (barSpacing - barWidth) / 2f
                val top = centerY - barHeight / 2f

                // Staggered opacity / tinting based on bar position (center is whiter/brighter)
                val centerDistance = kotlin.math.abs(i - count / 2f) / (count / 2f)
                val barTopColor = if (centerDistance < 0.35f && state != AssistantState.DISCONNECTED) {
                    Color.White
                } else {
                    secondaryColor
                }

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(barTopColor, primaryColor),
                        startY = top,
                        endY = top + barHeight
                    ),
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }

            // Draw subtle continuous Bezier line across top
            val path = Path()
            path.moveTo(0f, centerY)

            for (i in 0 until count) {
                val amp = bars[i]
                val barHeight = amp * height * 0.9f
                val x = i * barSpacing + barSpacing / 2f
                val y = centerY - barHeight / 2f
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (i - 1) * barSpacing + barSpacing / 2f
                    val prevY = centerY - (bars[i - 1] * height * 0.9f) / 2f
                    val midX = (prevX + x) / 2f
                    path.cubicTo(midX, prevY, midX, y, x, y)
                }
            }

            drawPath(
                path = path,
                color = primaryColor.copy(alpha = 0.3f),
                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

