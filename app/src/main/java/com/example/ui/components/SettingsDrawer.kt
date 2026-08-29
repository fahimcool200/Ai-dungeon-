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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.model.AssistantLanguage
import com.example.model.AuronConfig
import com.example.model.CustomTrainingRule
import com.example.ui.theme.Blue400
import com.example.ui.theme.Blue500
import com.example.ui.theme.Blue600
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekElevated
import com.example.ui.theme.SleekMint
import com.example.ui.theme.SleekSurface
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDrawer(
    config: AuronConfig,
    hasMicPermission: Boolean,
    latencyMs: Long,
    onConfigChange: (AuronConfig) -> Unit,
    onRequestMicPermission: () -> Unit,
    onAddCustomRule: (CustomTrainingRule) -> Unit = {},
    onRemoveCustomRule: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isBengali = config.language == AssistantLanguage.BENGALI

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var newTrigger by remember { mutableStateOf("") }
    var newActionType by remember { mutableStateOf("OPEN_APP") }
    var newActionParam by remember { mutableStateOf("youtube") }
    var newCustomReply by remember { mutableStateOf("") }

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
                .fillMaxHeight(0.92f)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top App Bar
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GlassBackground)
                            .border(1.dp, GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Blue400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isBengali) "নোভা কন্ট্রোল ও সেটিংস" else "NOVA AI CONTROL HUB",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isBengali) "ভয়েস, ভাষা, ও কাস্টম অ্যাকশন সেটিংস" else "Voice, Language, Wake Word & Training",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
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

            // 1. Language Selection (বাংলা / English / हिंदी)
            SettingsSectionHeader(
                title = if (isBengali) "সহকারী ভাষা (Language)" else "ASSISTANT LANGUAGE",
                icon = Icons.Default.Language
            )
            SettingsCard {
                Text(
                    text = if (isBengali) "আপনার পছন্দের ভাষা নির্বাচন করুন:" else "Select Primary Assistant Language:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate100
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistantLanguage.values().forEach { lang ->
                        val selected = config.language == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Blue600 else SleekElevated)
                                .border(1.dp, if (selected) Blue400 else GlassBorder, RoundedCornerShape(12.dp))
                                .clickable { onConfigChange(config.copy(language = lang)) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = lang.nativeName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else Slate300
                                )
                                Text(
                                    text = lang.displayName,
                                    fontSize = 10.sp,
                                    color = if (selected) Slate100 else Slate500
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Wake Word ("Hello Nova") & Hands-Free
            SettingsSectionHeader(
                title = if (isBengali) "হ্যান্ডস-ফ্রি ওয়েক ওয়ার্ড (\"Hello Nova\")" else "HANDS-FREE WAKE WORD",
                icon = Icons.Default.Hearing
            )
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "\"Hello Nova\" ওয়েক ওয়ার্ড ডিটেকশন" else "Continuous Wake Word Listening",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = if (isBengali)
                                "মাইকে হাত না দিয়ে 'Hello Nova' বা 'হ্যালো নোভা' বললে স্বয়ংক্রিয়ভাবে কথা শুরু হবে।"
                            else
                                "Say 'Hello Nova' anytime to trigger voice actions without tapping microphone.",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    Switch(
                        checked = config.wakeWordEnabled,
                        onCheckedChange = { onConfigChange(config.copy(wakeWordEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Blue500,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = SleekElevated
                        ),
                        modifier = Modifier.testTag("wake_word_switch")
                    )
                }

                if (config.wakeWordEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBengali) "ওয়েক ওয়ার্ড সংবেদনশীলতা: ${(config.wakeWordSensitivity * 100).toInt()}%" else "Wake Sensitivity: ${(config.wakeWordSensitivity * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate300
                    )
                    Slider(
                        value = config.wakeWordSensitivity,
                        onValueChange = { onConfigChange(config.copy(wakeWordSensitivity = it)) },
                        valueRange = 0.2f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Blue400,
                            activeTrackColor = Blue500,
                            inactiveTrackColor = SleekElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Theme Selection
            SettingsSectionHeader(
                title = if (isBengali) "অ্যাপ থিম (Theme)" else "APP THEME",
                icon = Icons.Default.DarkMode
            )
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.values().forEach { mode ->
                        val selected = config.themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) Indigo600 else SleekElevated)
                                .border(1.dp, if (selected) Blue400 else GlassBorder, RoundedCornerShape(10.dp))
                                .clickable { onConfigChange(config.copy(themeMode = mode)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (mode) {
                                    AppThemeMode.DARK -> if (isBengali) "ডার্ক মোড" else "Dark"
                                    AppThemeMode.LIGHT -> if (isBengali) "লাইট মোড" else "Light"
                                    AppThemeMode.SYSTEM -> if (isBengali) "সিস্টেম" else "System"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else Slate400
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Custom Training Rules (Teach Nova)
            SettingsSectionHeader(
                title = if (isBengali) "নোভাকে কাজ শেখান (Custom Training)" else "TEACH NOVA CUSTOM RULES",
                icon = Icons.Default.School
            )
            SettingsCard {
                Text(
                    text = if (isBengali)
                        "আপনার নিজের পছন্দমতো কমান্ড ও স্বয়ংক্রিয় অ্যাকশন তৈরি করুন:"
                    else
                        "Define custom trigger phrases and linked automations:",
                    fontSize = 12.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(10.dp))

                // List of current rules
                if (config.customRules.isEmpty()) {
                    Text(
                        text = if (isBengali) "কোন কাস্টম নিয়ম যুক্ত করা হয়নি।" else "No custom rules added yet.",
                        fontSize = 12.sp,
                        color = Slate500,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    config.customRules.forEach { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekElevated)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "\"${rule.triggerPhrase}\"",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue400
                                )
                                Text(
                                    text = "অ্যাকশন: ${rule.actionType} (${rule.actionParam})",
                                    fontSize = 11.sp,
                                    color = Slate300
                                )
                            }
                            IconButton(
                                onClick = { onRemoveCustomRule(rule.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Rule",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add rule section
                if (!showAddRuleDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GlassBackground)
                            .border(1.dp, Blue500.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { showAddRuleDialog = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Blue400, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isBengali) "+ নতুন কমান্ড শেখান" else "+ Add New Training Rule",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue400
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekElevated)
                            .padding(12.dp)
                    ) {
                        Text(text = if (isBengali) "কমান্ডের কথা (Trigger Phrase):" else "Spoken Phrase:", fontSize = 11.sp, color = Slate300)
                        OutlinedTextField(
                            value = newTrigger,
                            onValueChange = { newTrigger = it },
                            placeholder = { Text(if (isBengali) "যেমন: গান চালাও" else "e.g., play songs", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue400,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = if (isBengali) "অ্যাকশনের ধরন (Action Type):" else "Action Type:", fontSize = 11.sp, color = Slate300)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("OPEN_APP" to "App", "PHONE_CALL" to "Call", "DOWNLOAD_HELPER" to "Downloader").forEach { (type, label) ->
                                val sel = newActionType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) Blue600 else SleekSurface)
                                        .clickable {
                                            newActionType = type
                                            if (type == "OPEN_APP") newActionParam = "youtube"
                                            if (type == "PHONE_CALL") newActionParam = ""
                                            if (type == "DOWNLOAD_HELPER") newActionParam = "general"
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = label, fontSize = 11.sp, color = if (sel) Color.White else Slate400)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (newActionType == "OPEN_APP") (if (isBengali) "অ্যাপের নাম (যেমন: youtube, facebook):" else "App Name:")
                            else if (newActionType == "PHONE_CALL") (if (isBengali) "ফোন নম্বর:" else "Phone Number:")
                            else (if (isBengali) "প্ল্যাটফর্ম (facebook / youtube):" else "Platform:"),
                            fontSize = 11.sp,
                            color = Slate300
                        )
                        OutlinedTextField(
                            value = newActionParam,
                            onValueChange = { newActionParam = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue400,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = if (isBengali) "সহকারীর উত্তর (Custom Reply):" else "Assistant Reply:", fontSize = 11.sp, color = Slate300)
                        OutlinedTextField(
                            value = newCustomReply,
                            onValueChange = { newCustomReply = it },
                            placeholder = { Text(if (isBengali) "যেমন: আপনার নির্দেশ সম্পন্ন করছি।" else "Optional spoken reply", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue400,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { showAddRuleDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(text = if (isBengali) "বাতিল" else "Cancel", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    if (newTrigger.isNotBlank()) {
                                        val newRule = CustomTrainingRule(
                                            id = UUID.randomUUID().toString(),
                                            triggerPhrase = newTrigger.trim(),
                                            actionType = newActionType,
                                            actionParam = newActionParam.trim(),
                                            customReply = newCustomReply.trim()
                                        )
                                        onAddCustomRule(newRule)
                                        newTrigger = ""
                                        newCustomReply = ""
                                        showAddRuleDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                            ) {
                                Text(text = if (isBengali) "সংরক্ষণ করুন" else "Save Rule", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Persona & Voice Tuning
            SettingsSectionHeader(
                title = if (isBengali) "পারসোনালিটি ও সুর (Persona Tuning)" else "AI PERSONA & CHARACTER",
                icon = Icons.Default.Psychology
            )
            SettingsCard {
                Text(
                    text = if (isBengali) "রসবোধ ও স্মার্টনেস: ${(config.wittiness * 100).toInt()}%" else "Wittiness & Humor: ${(config.wittiness * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate100
                )
                Slider(
                    value = config.wittiness,
                    onValueChange = { onConfigChange(config.copy(wittiness = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Blue400,
                        activeTrackColor = Blue500,
                        inactiveTrackColor = SleekElevated
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "সংক্ষিপ্ত ও দ্রুত উত্তর (Concise Mode)" else "Voice-First Conciseness",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = if (isBengali) "অতিরিক্ত কথা না বলে ১-২ বাক্যে সরাসরি উত্তর।" else "Short, punchy 1-2 sentence voice answers.",
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
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Audio & Hardware Controls
            SettingsSectionHeader(
                title = if (isBengali) "অডিও ও সাউন্ড সেটিংস" else "AUDIO & SOUND HARDWARE",
                icon = Icons.Default.GraphicEq
            )
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "মাইক্রোফোন পারমিশন" else "Microphone Permission",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = if (hasMicPermission) (if (isBengali) "সক্রিয় রয়েছে · PCM 16kHz" else "Active · PCM 16kHz") else (if (isBengali) "কথা বলার জন্য পারমিশন দিন" else "Permission required"),
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
                                text = if (isBengali) "অনুমতি দিন" else "Grant",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "ইকো ক্যান্সেলেশন (AEC)" else "Acoustic Echo Cancellation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = if (isBengali) "স্পিকারের সাউন্ড যাতে মাইকে ফেরত না আসে।" else "Prevents speaker echo.",
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "নয়েজ সাপ্রেশন (Noise Reduction)" else "Hardware Noise Suppressor",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = if (isBengali) "আশেপাশের কোলাহল কমানো।" else "Reduces ambient noise.",
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

            Spacer(modifier = Modifier.height(18.dp))

            // 7. System Info & Engine Status
            SettingsSectionHeader(
                title = if (isBengali) "সিস্টেম ও ইঞ্জিন তথ্য" else "SYSTEM & ENGINE INFO",
                icon = Icons.Default.Info
            )
            SettingsCard {
                DetailRow(label = "Assistant Name", value = "Nova AI (নোভা এআই)")
                DetailRow(label = "Wake Word", value = "Hello Nova / হ্যালো নোভা")
                DetailRow(label = "Offline Mode", value = "Active & Ready")
                DetailRow(label = "Automation Tools", value = "YouTube, Facebook, Phone, SMS, Downloader")
                DetailRow(label = "Latency", value = if (latencyMs > 0) "$latencyMs ms" else "Real-time Ultra Low")
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
        modifier = Modifier.padding(bottom = 8.dp)
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
            letterSpacing = 0.5.sp
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
