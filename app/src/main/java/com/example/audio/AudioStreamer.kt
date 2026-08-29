package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * AudioStreamer captures microphone audio using Android AudioRecord.
 * Specifications: PCM 16-bit, 16000 Hz, Mono channel.
 * Computes live RMS amplitude for visualizer and interruption detection.
 */
class AudioStreamer(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onAudioChunkReady: (ByteArray) -> Unit,
    private val onSpeechDetected: () -> Unit
) {
    companion object {
        private const val TAG = "AudioStreamer"
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val CHUNK_DURATION_MS = 100 // 100ms chunks = 1600 samples = 3200 bytes
        private const val SPEECH_THRESHOLD = 0.18f
    }

    private var audioRecord: AudioRecord? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var recordingJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _micAmplitude = MutableStateFlow(0f)
    val micAmplitude: StateFlow<Float> = _micAmplitude.asStateFlow()

    private var speechConsecutiveFrames = 0

    @SuppressLint("MissingPermission")
    fun startStreaming(enableAec: Boolean = true, enableNs: Boolean = true): Boolean {
        if (_isRecording.value) return true

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            val chunkBytes = (SAMPLE_RATE * 2 * CHUNK_DURATION_MS) / 1000
            val bufferSize = max(minBufferSize, chunkBytes * 2)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed, falling back to MIC source")
                audioRecord?.release()
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
                )
            }

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord completely failed to initialize")
                return false
            }

            val audioSessionId = audioRecord?.audioSessionId ?: 0
            if (audioSessionId != 0) {
                if (enableAec && AcousticEchoCanceler.isAvailable()) {
                    try {
                        echoCanceler = AcousticEchoCanceler.create(audioSessionId)?.apply {
                            enabled = true
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "AcousticEchoCanceler error: ${e.message}")
                    }
                }
                if (enableNs && NoiseSuppressor.isAvailable()) {
                    try {
                        noiseSuppressor = NoiseSuppressor.create(audioSessionId)?.apply {
                            enabled = true
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "NoiseSuppressor error: ${e.message}")
                    }
                }
            }

            audioRecord?.startRecording()
            _isRecording.value = true

            recordingJob = scope.launch(Dispatchers.IO) {
                val readBuffer = ByteArray(chunkBytes)
                while (isActive && _isRecording.value) {
                    val bytesRead = audioRecord?.read(readBuffer, 0, readBuffer.size) ?: -1
                    if (bytesRead > 0) {
                        val chunkCopy = readBuffer.copyOf(bytesRead)
                        val amplitude = calculateRms(chunkCopy)
                        _micAmplitude.value = amplitude

                        // Speech detection for natural interruption
                        if (amplitude > SPEECH_THRESHOLD) {
                            speechConsecutiveFrames++
                            if (speechConsecutiveFrames >= 2) {
                                onSpeechDetected()
                            }
                        } else {
                            speechConsecutiveFrames = max(0, speechConsecutiveFrames - 1)
                        }

                        onAudioChunkReady(chunkCopy)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioStreamer: ${e.message}", e)
            stopStreaming()
            return false
        }
    }

    fun stopStreaming() {
        _isRecording.value = false
        _micAmplitude.value = 0f
        speechConsecutiveFrames = 0
        recordingJob?.cancel()
        recordingJob = null

        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
            audioRecord = null

            echoCanceler?.release()
            echoCanceler = null

            noiseSuppressor?.release()
            noiseSuppressor = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioStreamer: ${e.message}")
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
        return (rms * 3.5f).coerceIn(0f, 1f)
    }
}
