package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Blue400
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.Slate100

data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val prompt: String
)

@Composable
fun HUDQuickActions(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickActions = listOf(
        QuickAction("Open YouTube", Icons.Default.OpenInBrowser, "Open YouTube"),
        QuickAction("Device Status", Icons.Default.Devices, "What is my battery level and device status?"),
        QuickAction("Tell Me a Joke", Icons.Default.AutoAwesome, "Tell me a witty joke!"),
        QuickAction("Who are you?", Icons.Default.Lightbulb, "Who are you and what makes you special?"),
        QuickAction("Set 5m Timer", Icons.Default.Alarm, "Set a timer for 5 minutes"),
        QuickAction("Search Trends", Icons.Default.Search, "Search Google for top tech breakthroughs")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickActions.forEachIndexed { index, action ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassBackground)
                    .border(1.dp, GlassBorder, CircleShape)
                    .clickable { onPromptSelected(action.prompt) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("quick_action_$index")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = Blue400,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = action.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate100
                    )
                }
            }
        }
    }
}

