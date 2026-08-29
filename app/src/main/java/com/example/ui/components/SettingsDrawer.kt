package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuronConfig
import com.example.ui.theme.Blue400
import com.example.ui.theme.Blue500
import com.example.ui.theme.Blue600
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekElevated
import com.example.ui.theme.SleekMint
import com.example.ui.theme.SleekSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDrawer(
    config: AuronConfig,
    hasMicPermission: Boolean,
    latencyMs: Long,
    onConfigChange: (AuronConfig) -> Unit,
    onRequestMicPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SleekBg,
        dragHandle = null,
        modifier = Modifier.testTag("settings_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassBackground)
                            .border(1.dp, GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Blue400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "AURON CONTROL HUB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        letterSpacing = 0.8.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassBackground)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Settings",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Persona Tuning
            SettingsSectionHeader(title = "AI PERSONA & CHARACTER", icon = Icons.Default.Psychology)

            SettingsCard {
                // Wittiness Slider
                Text(
                    text = "Wittiness & Humor Level: ${(config.wittiness * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate100
                )
                Text(
                    text = "Controls Auron's clever banter, playful sarcasm, and quick wit.",
                    fontSize = 11.sp,
                    color = Slate400
                )
                Slider(
                    value = config.wittiness,
                    onValueChange = { onConfigChange(config.copy(wittiness = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Blue400,
                        activeTrackColor = Blue500,
                        inactiveTrackColor = SleekElevated
                    ),
                    modifier = Modifier.testTag("wittiness_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Energy Slider
                Text(
                    text = "Energy & Enthusiasm: ${(config.energyLevel * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate100
                )
                Slider(
                    value = config.energyLevel,
                    onValueChange = { onConfigChange(config.copy(energyLevel = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Indigo600,
                        activeTrackColor = Indigo600,
                        inactiveTrackColor = SleekElevated
                    ),
                    modifier = Modifier.testTag("energy_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Concise Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice-First Conciseness",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = "Short, punchy 1-2 sentence voice answers.",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    Switch(
                        checked = config.conciseMode,
                        onCheckedChange = { onConfigChange(config.copy(conciseMode = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Blue500,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = SleekElevated
                        ),
                        modifier = Modifier.testTag("concise_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Voice Selection
                Text(
                    text = "Prebuilt AI Voice Model",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate100
                )
                Spacer(modifier = Modifier.height(6.dp))

                val voiceOptions = listOf(
                    Pair("Puck", "Young, confident male (Default)"),
                    Pair("Fenrir", "Deep, authoritative male"),
                    Pair("Charon", "Calm, intellectual male"),
                    Pair("Aoede", "Vibrant, dynamic voice")
                )

                voiceOptions.forEach { (voiceKey, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.voiceName == voiceKey,
                            onClick = { onConfigChange(config.copy(voiceName = voiceKey)) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Blue400,
                                unselectedColor = Slate500
                            )
                        )
                        Column(modifier = Modifier.padding(start = 6.dp)) {
                            Text(text = voiceKey, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate100)
                            Text(text = desc, fontSize = 11.sp, color = Slate400)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Audio Pipeline & Interruption
            SettingsSectionHeader(title = "REAL-TIME AUDIO PIPELINE", icon = Icons.Default.GraphicEq)

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Microphone Permission",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = if (hasMicPermission) "Active · PCM 16kHz Mono" else "Permission required to stream voice",
                            fontSize = 11.sp,
                            color = if (hasMicPermission) SleekMint else Slate400
                        )
                    }
                    if (!hasMicPermission) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Blue600)
                                .clickable { onRequestMicPermission() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Grant",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Acoustic Echo Cancellation (AEC)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = "Prevents Auron's speaker from looping into mic.",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    Switch(
                        checked = config.echoCancellation,
                        onCheckedChange = { onConfigChange(config.copy(echoCancellation = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Blue500,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = SleekElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hardware Noise Suppressor",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = "Reduces background ambient audio noise.",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    Switch(
                        checked = config.noiseSuppression,
                        onCheckedChange = { onConfigChange(config.copy(noiseSuppression = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Blue500,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = SleekElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Engine Telemetry & About
            SettingsSectionHeader(title = "SYSTEM & ENGINE INFO", icon = Icons.Default.Info)

            SettingsCard {
                DetailRow(label = "AI Engine", value = "Gemini Live Multimodal API")
                DetailRow(label = "Model", value = "gemini-2.5-flash-native-audio")
                DetailRow(label = "Input Audio", value = "PCM16 / 16,000 Hz Mono")
                DetailRow(label = "Output Audio", value = "PCM / 24,000 Hz Low-Latency")
                DetailRow(label = "Latency", value = if (latencyMs > 0) "$latencyMs ms" else "Real-time active")
                DetailRow(label = "Function Calling", value = "Safe Browser & System Tools")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Blue400,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Blue400,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SleekSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Slate400)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate100)
    }
}

