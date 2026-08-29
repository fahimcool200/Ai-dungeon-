package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.AssistantLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.sqrt

/**
 * AudioPlayer handles continuous low-latency streaming PCM16 playback via AudioTrack,
 * and provides built-in fallback TTS for Auron's voice.
 */
class AudioPlayer(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onPlaybackFinished: () -> Unit
) {
    companion object {
        private const val TAG = "AudioPlayer"
        const val OUTPUT_SAMPLE_RATE = 24000 // 24kHz PCM for Gemini Live
        const val FALLBACK_SAMPLE_RATE = 16000
    }

    private var audioTrack: AudioTrack? = null
    private val audioQueue = ConcurrentLinkedQueue<ByteArray>()
    private var playbackJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _outputAmplitude = MutableStateFlow(0f)
    val outputAmplitude: StateFlow<Float> = _outputAmplitude.asStateFlow()

    // Fallback Android TTS for instant voice generation
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var currentLanguage: AssistantLanguage = AssistantLanguage.BENGALI

    init {
        initTts()
    }

    fun setLanguage(language: AssistantLanguage) {
        currentLanguage = language
        if (isTtsReady && tts != null) {
            val locale = if (language == AssistantLanguage.BENGALI) Locale("bn", "BD") else if (language == AssistantLanguage.HINDI) Locale("hi", "IN") else Locale.US
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            }
        }
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = if (currentLanguage == AssistantLanguage.BENGALI) Locale("bn", "BD") else Locale.US
                val result = tts?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.US
                }
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)

                // Try to find a good male English voice if available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val voices = tts?.voices
                    val maleVoice = voices?.firstOrNull { voice ->
                        voice.locale.language == "en" &&
                                (voice.name.contains("male", ignoreCase = true) ||
                                        voice.name.contains("en-us-x-sfg", ignoreCase = true) ||
                                        voice.name.contains("en-us-x-iom", ignoreCase = true))
                    }
                    if (maleVoice != null) {
                        tts?.voice = maleVoice
                    }
                }

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isPlaying.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isPlaying.value = false
                        _outputAmplitude.value = 0f
                        onPlaybackFinished()
                    }

                    override fun onError(utteranceId: String?) {
                        _isPlaying.value = false
                        _outputAmplitude.value = 0f
                        onPlaybackFinished()
                    }
                })
                isTtsReady = true
            }
        }
    }

    private fun ensureAudioTrack(sampleRate: Int = OUTPUT_SAMPLE_RATE): Boolean {
        if (audioTrack != null && audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
            return true
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = (minBufferSize * 4).coerceAtLeast(8192)

        return try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.state == AudioTrack.STATE_INITIALIZED
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create AudioTrack: ${e.message}")
            false
        }
    }

    /**
     * Enqueue a PCM chunk from Gemini Live audio stream.
     */
    fun enqueueAudioChunk(pcmChunk: ByteArray, sampleRate: Int = OUTPUT_SAMPLE_RATE) {
        if (pcmChunk.isEmpty()) return
        audioQueue.offer(pcmChunk)
        startPlaybackLoop(sampleRate)
    }

    private fun startPlaybackLoop(sampleRate: Int) {
        if (playbackJob?.isActive == true) return

        if (!ensureAudioTrack(sampleRate)) {
            Log.e(TAG, "Cannot start playback loop without initialized AudioTrack")
            return
        }

        playbackJob = scope.launch(Dispatchers.IO) {
            try {
                audioTrack?.play()
                _isPlaying.value = true

                while (isActive) {
                    val chunk = audioQueue.poll()
                    if (chunk != null) {
                        val amplitude = calculateRms(chunk)
                        _outputAmplitude.value = amplitude
                        audioTrack?.write(chunk, 0, chunk.size)
                    } else {
                        // Queue empty, brief wait to check if more chunks arrive
                        kotlinx.coroutines.delay(25)
                        if (audioQueue.isEmpty()) {
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Playback loop error: ${e.message}")
            } finally {
                _isPlaying.value = false
                _outputAmplitude.value = 0f
                playbackJob = null
                onPlaybackFinished()
            }
        }
    }

    /**
     * Fallback speak using TextToSpeech.
     */
    fun speakText(text: String, pitch: Float = 1.02f, speechRate: Float = 1.05f) {
        stopAndClear()
        if (isTtsReady && tts != null) {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speechRate)
            _isPlaying.value = true
            // Simulate lively amplitude while speaking
            startTtsAmplitudeSimulation()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "auron_utterance_${System.currentTimeMillis()}")
        }
    }

    private var ttsAmplitudeJob: Job? = null
    private fun startTtsAmplitudeSimulation() {
        ttsAmplitudeJob?.cancel()
        ttsAmplitudeJob = scope.launch(Dispatchers.Default) {
            while (isActive && _isPlaying.value) {
                val simulated = (0.25f + (Math.random() * 0.65f).toFloat())
                _outputAmplitude.value = simulated
                kotlinx.coroutines.delay(80)
            }
            _outputAmplitude.value = 0f
        }
    }

    /**
     * Instantly halt playback, purge queue, and reset audio track on interruption.
     */
    fun stopAndClear() {
        audioQueue.clear()
        playbackJob?.cancel()
        playbackJob = null
        ttsAmplitudeJob?.cancel()
        ttsAmplitudeJob = null

        try {
            if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack?.pause()
                audioTrack?.flush()
            }
            if (isTtsReady) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio player: ${e.message}")
        }

        _isPlaying.value = false
        _outputAmplitude.value = 0f
    }

    fun release() {
        stopAndClear()
        try {
            audioTrack?.release()
            audioTrack = null
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio player: ${e.message}")
        }
    }

    private fun calculateRms(pcmData: ByteArray): Float {
        if (pcmData.isEmpty()) return 0f
        var sumSquares = 0.0
        val sampleCount = pcmData.size / 2
        for (i in 0 until sampleCount) {
            val sample = ((pcmData[i * 2 + 1].toInt() shl 8) or (pcmData[i * 2].toInt() and 0xFF)).toShort()
            val normalized = sample / 32768.0
            sumSquares += normalized * normalized
        }
        val rms = sqrt(sumSquares / sampleCount).toFloat()
        return (rms * 3.2f).coerceIn(0f, 1f)
    }
}
