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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SmartToy
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
import com.example.model.AssistantLanguage
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
    language: AssistantLanguage = AssistantLanguage.BENGALI,
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBengali = language == AssistantLanguage.BENGALI

    val quickActions = if (isBengali) {
        listOf(
            QuickAction("ইউটিউব চালান", Icons.Default.PlayCircle, "ইউটিউব ওপেন কর"),
            QuickAction("ফেসবুক ওপেন", Icons.Default.Public, "ফেসবুক ওপেন কর"),
            QuickAction("ভিডিও ডাউনলোড", Icons.Default.Download, "ভিডিও ডাউনলোড হেল্পার"),
            QuickAction("ফোন দিন", Icons.Default.Call, "ফোন ডায়াল কর"),
            QuickAction("মেসেজ লিখো", Icons.Default.Message, "মেসেজ লিখো"),
            QuickAction("ব্যাটারি চার্জ কত?", Icons.Default.BatteryChargingFull, "ব্যাটারি চার্জ কত?"),
            QuickAction("ফ্ল্যাশলাইট", Icons.Default.FlashlightOn, "ফ্ল্যাশলাইট জ্বালাও"),
            QuickAction("কে তুমি?", Icons.Default.SmartToy, "কে তুমি এবং তুমি কি কি করতে পারো?")
        )
    } else {
        listOf(
            QuickAction("Open YouTube", Icons.Default.PlayCircle, "Open YouTube"),
            QuickAction("Open Facebook", Icons.Default.Public, "Open Facebook"),
            QuickAction("Download Video", Icons.Default.Download, "Download video helper"),
            QuickAction("Make a Call", Icons.Default.Call, "Make a phone call"),
            QuickAction("Send SMS", Icons.Default.Message, "Send a message"),
            QuickAction("Battery Status", Icons.Default.BatteryChargingFull, "Check battery status"),
            QuickAction("Flashlight", Icons.Default.FlashlightOn, "Toggle flashlight"),
            QuickAction("Who are you?", Icons.Default.SmartToy, "Who are you and what can you do?")
        )
    }

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
                        modifier = Modifier.size(14.dp)
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
