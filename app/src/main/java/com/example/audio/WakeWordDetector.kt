package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.model.AssistantLanguage
import java.util.Locale

/**
 * WakeWordDetector provides continuous hands-free keyword detection
 * for "Hello Nova", "Hey Nova", "হ্যালো নোভা", and "নোভা".
 */
class WakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: (command: String) -> Unit,
    private val onVolumeChanged: (rmsDb: Float) -> Unit = {}
) {
    companion object {
        private const val TAG = "WakeWordDetector"
        private val WAKE_WORDS = listOf(
            "hello nova",
            "hey nova",
            "ok nova",
            "nova",
            "হ্যালো নোভা",
            "হাই নোভা",
            "নোভা",
            "শোন নোভা",
            "নভা"
        )
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isEnabled = true
    private var currentLanguage: AssistantLanguage = AssistantLanguage.BENGALI
    private val mainHandler = Handler(Looper.getMainLooper())

    fun setLanguage(language: AssistantLanguage) {
        currentLanguage = language
        if (isListening) {
            restartListening()
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            stop()
        } else if (!isListening) {
            start()
        }
    }

    fun start() {
        if (!isEnabled || isListening) return
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    Log.w(TAG, "Speech recognition not available on device")
                    return@post
                }
                destroyRecognizer()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    val langTag = if (currentLanguage == AssistantLanguage.BENGALI) "bn-BD" else "en-US"
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                }
                speechRecognizer?.startListening(intent)
                isListening = true
                Log.d(TAG, "WakeWordDetector started listening in $currentLanguage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start wake word detector: ${e.message}", e)
                isListening = false
                scheduleRestart(2000)
            }
        }
    }

    fun stop() {
        isListening = false
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.post {
            destroyRecognizer()
        }
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying speech recognizer: ${e.message}")
        } finally {
            speechRecognizer = null
        }
    }

    private fun restartListening() {
        stop()
        if (isEnabled) {
            scheduleRestart(300)
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        mainHandler.postDelayed({
            if (isEnabled) {
                start()
            }
        }, delayMs)
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(80)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic vibration failed: ${e.message}")
        }
    }

    private fun checkForWakeWord(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT).trim()
        for (wake in WAKE_WORDS) {
            if (lower.contains(wake)) {
                triggerHapticFeedback()
                // Extract whatever came after the wake word as command
                val command = lower.substringAfter(wake).trim()
                Log.d(TAG, "Wake word matched! Trigger: '$wake', Command: '$command'")
                onWakeWordDetected(command)
                return true
            }
        }
        return false
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {
            onVolumeChanged(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            // Speech segment finished, will restart in onResults or onError
        }

        override fun onError(error: Int) {
            Log.d(TAG, "SpeechRecognizer error code: $error")
            isListening = false
            // Restart silently after brief delay to maintain continuous wake-word standby
            val delay = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                250L
            } else {
                1500L
            }
            if (isEnabled) {
                scheduleRestart(delay)
            }
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                for (match in matches) {
                    if (checkForWakeWord(match)) {
                        break
                    }
                }
            }
            if (isEnabled) {
                scheduleRestart(250)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                for (match in matches) {
                    if (checkForWakeWord(match)) {
                        // Stop immediately once wake word is hit in partial
                        destroyRecognizer()
                        break
                    }
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
