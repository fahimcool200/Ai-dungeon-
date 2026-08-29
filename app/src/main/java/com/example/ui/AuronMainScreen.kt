package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AssistantLanguage
import com.example.model.AssistantState
import com.example.ui.components.AuronAvatar
import com.example.ui.components.HUDQuickActions
import com.example.ui.components.MicButton
import com.example.ui.components.OnboardingTutorialDialog
import com.example.ui.components.SettingsDrawer
import com.example.ui.components.ToolExecutionBanner
import com.example.ui.components.TopBar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekErrorRed
import com.example.ui.theme.SleekHeaderTop

@Composable
fun AuronMainScreen(
    viewModel: AuronViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isBengali = uiState.config.language == AssistantLanguage.BENGALI

    // Microphone Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setMicPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val hasAudio = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setMicPermissionGranted(hasAudio)
        if (!hasAudio) {
            // Auto prompt for microphone on first startup
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SleekHeaderTop,
                            SleekBg,
                            SleekBg
                        )
                    )
                )
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Navigation Bar
                TopBar(
                    state = uiState.state,
                    language = uiState.config.language,
                    sessionDurationSeconds = uiState.sessionDurationSeconds,
                    onLanguageToggle = {
                        val nextLang = if (isBengali) AssistantLanguage.ENGLISH else AssistantLanguage.BENGALI
                        viewModel.toggleLanguage(nextLang)
                    },
                    onSettingsClick = { viewModel.toggleDrawer(true) }
                )

                // Tool Execution HUD Toast Banner
                ToolExecutionBanner(
                    event = uiState.latestToolEvent,
                    onDismiss = { viewModel.dismissToolBanner() }
                )

                // Central Character Area (Realistic 3D Talking Robot Head & Subtitle)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AuronAvatar(
                        state = uiState.state,
                        micAmplitude = uiState.micVolume,
                        speakerAmplitude = uiState.speakerVolume,
                        subtitle = uiState.subtitleText,
                        language = uiState.config.language,
                        onAvatarClick = {
                            if (!uiState.hasMicPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.toggleSession()
                            }
                        }
                    )
                }

                // Error State Recovery Card (if any)
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SleekErrorRed.copy(alpha = 0.12f))
                            .border(1.dp, SleekErrorRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SleekErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = uiState.errorMessage ?: if (isBengali) "কানেকশন সমস্যা হয়েছে। পুনরায় চেষ্টা করুন।" else "Connection error. Tap to retry.",
                                    fontSize = 12.sp,
                                    color = Slate100
                                )
                            }
                            Button(
                                onClick = { viewModel.clearError(); viewModel.startSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekErrorRed),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(if (isBengali) "পুনরায় চেষ্টা" else "Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Waveform Audio Visualizer
                WaveformVisualizer(
                    state = uiState.state,
                    bars = uiState.visualizerBars
                )

                // Quick Action HUD Automation Pills
                HUDQuickActions(
                    language = uiState.config.language,
                    onPromptSelected = { prompt ->
                        viewModel.sendQuickVoicePrompt(prompt)
                    }
                )

                // Bottom Controls: Microphone Command Button & State Caption
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 6.dp)
                ) {
                    MicButton(
                        state = uiState.state,
                        onClick = {
                            if (!uiState.hasMicPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.toggleSession()
                            }
                        },
                        onInterruptClick = {
                            viewModel.interrupt()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (uiState.state) {
                            AssistantState.DISCONNECTED -> if (isBengali) "\"Hello Nova\" বলুন অথবা কথা বলতে মাইকে চাপ দিন" else "Say \"Hello Nova\" or tap microphone to speak"
                            AssistantState.SPEAKING -> if (isBengali) "কথা থামানোর জন্য বোতামে ট্যাপ করুন বা কথা বলুন" else "Tap button or speak to interrupt Nova"
                            AssistantState.LISTENING -> if (isBengali) "নোভা আপনার কথা শুনছে..." else "Nova is listening to you..."
                            AssistantState.THINKING -> if (isBengali) "নোভা কাজ সম্পন্ন করছে..." else "Nova is processing..."
                            AssistantState.CONNECTING -> if (isBengali) "সার্ভারের সাথে কানেক্ট হচ্ছে..." else "Connecting to live voice engine..."
                            AssistantState.ERROR -> if (isBengali) "অফলাইন মোড সক্রিয় রয়েছে" else "Offline mode active"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Onboarding & Permission Tutorial Dialog
            if (uiState.showOnboardingTutorial) {
                OnboardingTutorialDialog(
                    language = uiState.config.language,
                    hasMicPermission = uiState.hasMicPermission,
                    onRequestMicPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onVoiceEnrollmentComplete = { voiceName ->
                        viewModel.enrollUserVoice(voiceName)
                    },
                    onFinishTutorial = {
                        viewModel.completeOnboardingTutorial()
                    }
                )
            }

            // Settings Bottom Drawer
            if (uiState.isDrawerOpen) {
                SettingsDrawer(
                    config = uiState.config,
                    hasMicPermission = uiState.hasMicPermission,
                    latencyMs = uiState.latencyMs,
                    onConfigChange = { newConfig ->
                        viewModel.updateConfig(newConfig)
                    },
                    onRequestMicPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onTestVoice = { sampleText ->
                        viewModel.testNovaVoice(sampleText)
                    },
                    onAddCustomRule = { rule ->
                        viewModel.addCustomTrainingRule(rule)
                    },
                    onRemoveCustomRule = { id ->
                        viewModel.removeCustomTrainingRule(id)
                    },
                    onRestartTutorial = {
                        viewModel.restartTutorial()
                    },
                    onDismiss = {
                        viewModel.toggleDrawer(false)
                    }
                )
            }
        }
    }
}
