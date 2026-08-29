package com.example.audio

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.example.model.AssistantLanguage
import java.util.Locale

/**
 * WakeWordDetector provides continuous, robust hands-free keyword detection
 * for "Hello Nova", "Hey Nova", "হ্যালো নোভা", "নোভা", etc.
 */
class WakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: (command: String) -> Unit,
    private val onVolumeChanged: (rmsDb: Float) -> Unit = {}
) {
    companion object {
        private const val TAG = "WakeWordDetector"
        private val WAKE_PATTERNS = listOf(
            "hello nova",
            "hey nova",
            "ok nova",
            "hi nova",
            "nova",
            "হ্যালো নোভা",
            "হাই নোভা",
            "নোভা",
            "শোন নোভা",
            "নোবা",
            "নভা",
            "halo nova",
            "helo nova"
        )
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isEnabled = true
    private var currentLanguage: AssistantLanguage = AssistantLanguage.BENGALI
    private var sensitivity: Float = 0.85f
    private val mainHandler = Handler(Looper.getMainLooper())
    private var retryCount = 0

    fun setLanguage(language: AssistantLanguage) {
        currentLanguage = language
        if (isListening) {
            restartListening()
        }
    }

    fun setSensitivity(value: Float) {
        sensitivity = value.coerceIn(0.1f, 1.0f)
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
        if (!isEnabled) return
        
        // Permission check
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot start WakeWordDetector: RECORD_AUDIO permission not granted")
            return
        }

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
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    
                    val primaryLang = if (currentLanguage == AssistantLanguage.BENGALI) "bn-BD" else "en-US"
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, primaryLang)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, primaryLang)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "bn-BD", "bn-IN"))
                }
                
                speechRecognizer?.startListening(intent)
                isListening = true
                retryCount = 0
                Log.d(TAG, "WakeWordDetector actively listening for 'Hello Nova' / 'হ্যালো নোভা'...")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start wake word detector: ${e.message}", e)
                isListening = false
                scheduleRestart(1500)
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
            isListening = false
        }
    }

    private fun restartListening() {
        stop()
        if (isEnabled) {
            scheduleRestart(200)
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        mainHandler.removeCallbacksAndMessages(null)
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
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(100)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic vibration failed: ${e.message}")
        }
    }

    private fun checkForWakeWord(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT).trim()
        if (lower.isEmpty()) return false

        for (wake in WAKE_PATTERNS) {
            if (lower.contains(wake)) {
                triggerHapticFeedback()
                val command = lower.substringAfter(wake).trim()
                Log.d(TAG, "Wake word matched! Pattern: '$wake', Trailing command: '$command'")
                destroyRecognizer()
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
            // Speech chunk ended, awaiting results
        }

        override fun onError(error: Int) {
            Log.d(TAG, "SpeechRecognizer wake listener status code: $error")
            isListening = false
            
            val delay = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> 150L
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 400L
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> 5000L
                else -> 800L
            }
            
            if (isEnabled) {
                scheduleRestart(delay)
            }
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            var matched = false
            if (!matches.isNullOrEmpty()) {
                for (match in matches) {
                    if (checkForWakeWord(match)) {
                        matched = true
                        break
                    }
                }
            }
            if (!matched && isEnabled) {
                scheduleRestart(150)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                for (match in matches) {
                    if (checkForWakeWord(match)) {
                        break
                    }
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
