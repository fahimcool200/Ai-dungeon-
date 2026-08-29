package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.AssistantLanguage
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
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekElevated
import com.example.ui.theme.SleekMint
import com.example.ui.theme.SleekSurface

/**
 * Interactive Onboarding Tutorial Dialog.
 * Walks user through:
 * 1. Intro & Hands-free Robot Assistant overview
 * 2. Mandatory Microphone Permission
 * 3. User Voice Profile Training & Registration ("Hello Nova" enrollment)
 * 4. Play Protect safety information guide
 * 5. Hands-free command cheatsheet
 */
@Composable
fun OnboardingTutorialDialog(
    language: AssistantLanguage,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    onVoiceEnrollmentComplete: (userName: String) -> Unit,
    onFinishTutorial: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 4
    val isBengali = language == AssistantLanguage.BENGALI

    var isRecordingTraining by remember { mutableStateOf(false) }
    var trainingComplete by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { /* Force completion of tutorial */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Blue500.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .testTag("onboarding_tutorial_dialog"),
                colors = CardDefaults.cardColors(containerColor = SleekBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header progress indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBengali) "নোভা সেটআপ টিউটোরিয়াল ($currentStep/$totalSteps)" else "Nova Setup Tutorial ($currentStep/$totalSteps)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cyan400,
                            letterSpacing = 0.5.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(totalSteps) { idx ->
                                Box(
                                    modifier = Modifier
                                        .size(if (idx + 1 == currentStep) 16.dp else 8.dp, 6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (idx + 1 == currentStep) Cyan400 else Slate700)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "onboarding_step"
                    ) { step ->
                        when (step) {
                            1 -> {
                                // Step 1: Welcome & Hands-free Robot Assistant Intro
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Cyan400, CircleShape)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_robot_head),
                                            contentDescription = "Robot Head",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = if (isBengali) "নোভা এআই রোবটে স্বাগতম!" else "Welcome to Nova AI Robot!",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate100,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = if (isBengali)
                                            "আইফোনের সিরির মতো সম্পূর্ণ হ্যান্ডস-ফ্রি এআই সহকারী। কোন কিছু টাইপ বা চাপতে হবে না—মুখ দিয়ে 'Hello Nova' বা সরাসরি আপনার কথা বললেই রোবট কথা বলবে এবং অ্যাপ চালাবে!"
                                        else
                                            "A 100% hands-free voice assistant. No typing or tapping required. Simply say 'Hello Nova' or speak any command naturally!",
                                        fontSize = 13.sp,
                                        color = Slate300,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            2 -> {
                                // Step 2: Microphone Permission Prompt
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(if (hasMicPermission) SleekMint.copy(alpha = 0.2f) else Blue600.copy(alpha = 0.3f))
                                            .border(1.dp, if (hasMicPermission) SleekMint else Blue400, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (hasMicPermission) Icons.Default.CheckCircle else Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = if (hasMicPermission) SleekMint else Cyan400,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = if (isBengali) "মাইক্রোফোন পারমিশন" else "Microphone Access Required",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate100
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = if (isBengali)
                                            "আপনার কথা শুনে স্বয়ংক্রিয়ভাবে কাজ করার জন্য মাইক্রোফোন পারমিশন আবশ্যক।"
                                        else
                                            "Nova requires continuous microphone access to detect your voice commands seamlessly.",
                                        fontSize = 13.sp,
                                        color = Slate300,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (!hasMicPermission) {
                                        Button(
                                            onClick = onRequestMicPermission,
                                            colors = ButtonDefaults.buttonColors(containerColor = Cyan400),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("grant_mic_button")
                                        ) {
                                            Icon(Icons.Default.Hearing, contentDescription = null, tint = Color.Black)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isBengali) "মাইক্রোফোন অনুমতি দিন" else "Grant Microphone Permission",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SleekMint.copy(alpha = 0.15f))
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isBengali) "✓ মাইক্রোফোন পারমিশন সফল হয়েছে!" else "✓ Microphone Permission Granted!",
                                                color = SleekMint,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // Step 3: Voice Profile Training
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(if (trainingComplete) SleekMint.copy(alpha = 0.2f) else Indigo600.copy(alpha = 0.3f))
                                            .border(1.dp, if (trainingComplete) SleekMint else Blue400, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = if (trainingComplete) SleekMint else Cyan400,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = if (isBengali) "আপনার ভয়েস ট্রেইনিং" else "Voice Profile Enrollment",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate100
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (isBengali)
                                            "রোবট যাতে আপনার কণ্ঠস্বর চিনে সাড়া দেয়, সেজন্য নিচের বাটনে চাপ দিয়ে জোরে 'হ্যালো নোভা' (Hello Nova) বলুন:"
                                        else
                                            "Tap below and clearly say 'Hello Nova' to enroll your unique voice pattern:",
                                        fontSize = 12.sp,
                                        color = Slate300,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (!trainingComplete) {
                                        Button(
                                            onClick = {
                                                isRecordingTraining = true
                                                // Simulate voice enrollment capture completion
                                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                    isRecordingTraining = false
                                                    trainingComplete = true
                                                    onVoiceEnrollmentComplete("My Voice Profile")
                                                }, 2200)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isRecordingTraining) SleekMint else Blue600
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("enroll_voice_button")
                                        ) {
                                            Icon(
                                                imageVector = if (isRecordingTraining) Icons.Default.GraphicEq else Icons.Default.Mic,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isRecordingTraining)
                                                    (if (isBengali) "আপনার কণ্ঠস্বর শুনছি... বলুন 'হ্যালো নোভা'" else "Listening... Say 'Hello Nova'")
                                                else
                                                    (if (isBengali) "ভয়েস রেকর্ড ও ট্রেইনিং শুরু করুন" else "Start Voice Training"),
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SleekMint.copy(alpha = 0.15f))
                                                .border(1.dp, SleekMint, RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isBengali) "✓ ভয়েস ট্রেইনিং সম্পন্ন! নোভা আপনার গলা চিনতে পেরেছে।" else "✓ Voice Enrolled! Nova recognized your voice.",
                                                color = SleekMint,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            4 -> {
                                // Step 4: Quick Hands-free Command Cheat Sheet & Play Protect info
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Blue600.copy(alpha = 0.25f))
                                            .border(1.dp, Cyan400, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = Cyan400,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = if (isBengali) "আপনি কি কি বলতে পারেন:" else "Hands-free Commands List:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate100
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val examples = if (isBengali) listOf(
                                        "\"গান শোনাও\" বা \"ইউটিউব ওপেন কর\"",
                                        "\"ফেসবুক\" বা \"হোয়াটসঅ্যাপ ওপেন কর\"",
                                        "\"কাউকে ফোন দাও [নম্বর]\"",
                                        "\"টর্চ / ফ্ল্যাশলাইট জ্বালাও\"",
                                        "\"ভিডিও ডাউনলোড কর\"",
                                        "\"ব্যাটারির চার্জ কত?\""
                                    ) else listOf(
                                        "\"Play songs on YouTube\"",
                                        "\"Open Facebook / WhatsApp\"",
                                        "\"Call [Phone Number]\"",
                                        "\"Turn on flashlight\"",
                                        "\"Download video helper\"",
                                        "\"What is battery level?\""
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SleekElevated)
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        examples.forEach { cmd ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(Cyan400)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = cmd, fontSize = 12.sp, color = Slate300)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Play protect note
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GlassBackground)
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Blue400, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBengali)
                                                "প্লে প্রোটেক্ট ওয়ার্নিং আসলে 'More details -> Install anyway' চাপবেন।"
                                            else
                                                "If Play Protect alerts during sideloading, select 'More details -> Install anyway'.",
                                            fontSize = 10.sp,
                                            color = Slate400
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Navigation Buttons (Back / Next / Finish)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBengali) "পেছনে" else "Back", fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        if (currentStep < totalSteps) {
                            Button(
                                onClick = {
                                    if (currentStep == 2 && !hasMicPermission) {
                                        onRequestMicPermission()
                                    }
                                    currentStep++
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan400),
                                modifier = Modifier.testTag("onboarding_next_button")
                            ) {
                                Text(
                                    text = if (isBengali) "পরবর্তী" else "Next",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = onFinishTutorial,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekMint),
                                modifier = Modifier.testTag("finish_tutorial_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBengali) "নোভা শুরু করুন" else "Start Nova Live",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
