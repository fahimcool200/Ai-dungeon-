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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.Cyan400
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
    onTestVoice: (text: String) -> Unit = {},
    onAddCustomRule: (CustomTrainingRule) -> Unit = {},
    onRemoveCustomRule: (String) -> Unit = {},
    onRestartTutorial: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isBengali = config.language == AssistantLanguage.BENGALI

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var newTrigger by remember { mutableStateOf("") }
    var newActionType by remember { mutableStateOf("OPEN_APP") }
    var newActionParam by remember { mutableStateOf("youtube") }
    var newCustomReply by remember { mutableStateOf("") }
    var showPlayProtectInfo by remember { mutableStateOf(false) }
    var showVoiceTrainingSuccess by remember { mutableStateOf(false) }

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
                .fillMaxHeight(0.94f)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GlassBackground)
                            .border(1.dp, Cyan400.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(22.dp)
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
                            text = if (isBengali) "ভয়েস, ট্রেইনিং ও অটোমেশন নিয়ন্ত্রণ" else "Voice, Training & Automation",
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

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Language Selection (বাংলা / English / हिंदी)
            SettingsSectionHeader(
                title = if (isBengali) "সহকারী ভাষা (Language)" else "ASSISTANT LANGUAGE",
                icon = Icons.Default.Language
            )
            SettingsCard {
                Text(
                    text = if (isBengali) "আপনার পছন্দের ভাষা নির্বাচন করুন:" else "Select Primary Assistant Language:",
                    fontSize = 12.sp,
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
                                .border(1.dp, if (selected) Cyan400 else GlassBorder, RoundedCornerShape(12.dp))
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

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Hands-Free Wake Word ("Hello Nova")
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
                            text = if (isBengali) "\"Hello Nova\" ওয়েক ওয়ার্ড শোনা" else "Continuous Wake Word Listening",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = if (isBengali)
                                "স্ক্রিনে হাত না দিয়ে 'Hello Nova' বা 'হ্যালো নোভা' বললে স্বয়ংক্রিয়ভাবে কথা শুনবে।"
                            else
                                "Say 'Hello Nova' anytime to trigger voice actions without tapping screen.",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    Switch(
                        checked = config.wakeWordEnabled,
                        onCheckedChange = { onConfigChange(config.copy(wakeWordEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Cyan400,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = SleekElevated
                        ),
                        modifier = Modifier.testTag("wake_word_switch")
                    )
                }

                if (config.wakeWordEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isBengali) "সংবেদনশীলতা: ${(config.wakeWordSensitivity * 100).toInt()}%" else "Sensitivity: ${(config.wakeWordSensitivity * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate300
                    )
                    Slider(
                        value = config.wakeWordSensitivity,
                        onValueChange = { onConfigChange(config.copy(wakeWordSensitivity = it)) },
                        valueRange = 0.2f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Cyan400,
                            activeTrackColor = Cyan400,
                            inactiveTrackColor = SleekElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. User Voice Profile & Training (আপনার ভয়েস ট্রেইনিং)
            SettingsSectionHeader(
                title = if (isBengali) "ইউজারের ভয়েস ট্রেইনিং (Voice Profile)" else "USER VOICE TRAINING",
                icon = Icons.Default.RecordVoiceOver
            )
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "আপনার কণ্ঠস্বর ট্রেইনিং স্ট্যাটাস" else "Enrolled Voice Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Text(
                            text = if (config.isVoiceEnrolled)
                                (if (isBengali) "✓ আপনার ভয়েস প্রোফাইল সক্রিয় আছে" else "✓ Voice profile active")
                            else
                                (if (isBengali) "এখনই আপনার গলার ভয়েস রেকর্ড করুন" else "Train Nova with your voice"),
                            fontSize = 11.sp,
                            color = if (config.isVoiceEnrolled) SleekMint else Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        showVoiceTrainingSuccess = true
                        onConfigChange(config.copy(isVoiceEnrolled = true))
                        val speech = if (isBengali) "আপনার ভয়েস সফলভাবে ট্রেইনিং সম্পন্ন হয়েছে।" else "Your voice profile has been trained successfully."
                        onTestVoice(speech)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (config.isVoiceEnrolled) SleekMint else Blue600),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("retrain_voice_button")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "ভয়েস রেকর্ড / পুনরায় ট্রেইনিং করুন" else "Record / Re-train Voice Profile",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (showVoiceTrainingSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) "✓ ভয়েস প্রোফাইল আপডেট করা হয়েছে!" else "✓ Voice Profile Saved!",
                        color = SleekMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Voice Pitch & Speech Rate
            SettingsSectionHeader(
                title = if (isBengali) "রোবট ভয়েস স্পিড ও পিচ (Speech Rate)" else "VOICE PITCH & SPEED",
                icon = Icons.AutoMirrored.Filled.VolumeUp
            )
            SettingsCard {
                // Pitch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isBengali) "ভয়েস পিচ (Pitch):" else "Pitch:", fontSize = 12.sp, color = Slate300)
                    Text(text = String.format("%.2fx", config.pitch), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Cyan400)
                }
                Slider(
                    value = config.pitch,
                    onValueChange = { onConfigChange(config.copy(pitch = it)) },
                    valueRange = 0.6f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = Cyan400,
                        activeTrackColor = Cyan400,
                        inactiveTrackColor = SleekElevated
                    )
                )

                // Speech Rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isBengali) "কথা বলার গতি (Speed Rate):" else "Speech Rate:", fontSize = 12.sp, color = Slate300)
                    Text(text = String.format("%.2fx", config.speechRate), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Cyan400)
                }
                Slider(
                    value = config.speechRate,
                    onValueChange = { onConfigChange(config.copy(speechRate = it)) },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = Cyan400,
                        activeTrackColor = Cyan400,
                        inactiveTrackColor = SleekElevated
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Test Voice Button
                Button(
                    onClick = {
                        val testText = if (isBengali) "হ্যালো! আমি নোভা এআই রোবট। আপনার কি সেবা করতে পারি?" else "Hello! I am Nova AI robot. How can I help you today?"
                        onTestVoice(testText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("test_voice_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "ভয়েস টেস্ট শুনুন (Voice Preview)" else "Test Nova Voice Audio",
                        fontSize = 12.sp,
                        color = Slate100,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. App Theme Selection
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
                                .border(1.dp, if (selected) Cyan400 else GlassBorder, RoundedCornerShape(10.dp))
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

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Custom Action Training Rules (Teach Nova)
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
                                    color = Cyan400
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

                Spacer(modifier = Modifier.height(10.dp))

                // Add rule section
                if (!showAddRuleDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GlassBackground)
                            .border(1.dp, Cyan400.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { showAddRuleDialog = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isBengali) "+ নতুন কমান্ড শেখান" else "+ Add New Training Rule",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cyan400
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
                                focusedBorderColor = Cyan400,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = if (isBengali) "অ্যাকশনের ধরন (Action Type):" else "Action Type:", fontSize = 11.sp, color = Slate300)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("OPEN_APP" to "অ্যাপ খুলুন", "OPEN_URL" to "ওয়েবসাইট", "DOWNLOAD_HELPER" to "ভিডিও ডাউনলোড").forEach { (type, label) ->
                                val selected = newActionType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = { newActionType = type },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Cyan400,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = if (isBengali) "প্যারামিটার (App / URL / Phone):" else "Parameter:", fontSize = 11.sp, color = Slate300)
                        OutlinedTextField(
                            value = newActionParam,
                            onValueChange = { newActionParam = it },
                            placeholder = { Text("youtube, facebook, https://...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan400,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { showAddRuleDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isBengali) "বাতিল" else "Cancel", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newTrigger.isNotBlank()) {
                                        onAddCustomRule(
                                            CustomTrainingRule(
                                                triggerPhrase = newTrigger.trim(),
                                                actionType = newActionType,
                                                actionParam = newActionParam.trim(),
                                                customReply = newCustomReply.trim()
                                            )
                                        )
                                        newTrigger = ""
                                        showAddRuleDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan400),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isBengali) "সংরক্ষণ করুন" else "Save Rule", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Play Protect & APK Installation Guide
            SettingsSectionHeader(
                title = if (isBengali) "প্লে প্রোটেক্ট ওয়ার্নিং সাহায্য (Play Protect)" else "GOOGLE PLAY PROTECT HELP",
                icon = Icons.Default.Security
            )
            SettingsCard {
                Text(
                    text = if (isBengali)
                        "গুগল প্লে স্টোর ছাড়া যেকোনো এপিকে ইনস্টল করার সময় অ্যান্ড্রয়েড Play Protect সতর্কতা দেখায়। এটি বন্ধ করতে:"
                    else
                        "When sideloading APKs, Play Protect shows an unverified developer prompt. How to install smoothly:",
                    fontSize = 11.sp,
                    color = Slate300
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SleekElevated)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (isBengali)
                            "১. ইনস্টল বক্সে 'More details' (আরও বিবরণ) বাটনে চাপ দিন।\n২. তারপর 'Install anyway' (যেভাবেই হোক ইনস্টল করুন) চাপুন।"
                        else
                            "1. Tap 'More details' on the installation dialog.\n2. Tap 'Install anyway' to complete APK setup.",
                        fontSize = 11.sp,
                        color = Cyan400,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Restart Tutorial & Help
            SettingsSectionHeader(
                title = if (isBengali) "টিউটোরিয়াল ও সাহায্য" else "TUTORIAL & HELP",
                icon = Icons.Default.HelpOutline
            )
            SettingsCard {
                Button(
                    onClick = {
                        onDismiss()
                        onRestartTutorial()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekElevated),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("restart_tutorial_button")
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Cyan400)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "টিউটোরিয়াল পুনরায় দেখুন" else "Re-open Setup Tutorial",
                        fontSize = 12.sp,
                        color = Slate100,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Cyan400, letterSpacing = 0.5.sp)
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column { content() }
    }
}
