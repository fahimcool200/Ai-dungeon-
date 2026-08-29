package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayer
import com.example.audio.AudioStreamer
import com.example.model.AssistantState
import com.example.model.AuronConfig
import com.example.model.AuronLog
import com.example.model.LogType
import com.example.model.ToolExecutionEvent
import com.example.service.GeminiLiveSession
import com.example.tools.ToolManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

data class AuronUiState(
    val state: AssistantState = AssistantState.DISCONNECTED,
    val micVolume: Float = 0f,
    val speakerVolume: Float = 0f,
    val visualizerBars: List<Float> = List(24) { 0.05f },
    val statusText: String = "Tap to talk",
    val subtitleText: String = "Auron is ready. Tap the microphone to start real-time voice.",
    val errorMessage: String? = null,
    val latencyMs: Long = 0L,
    val config: AuronConfig = AuronConfig(),
    val hasMicPermission: Boolean = false,
    val recentLogs: List<AuronLog> = emptyList(),
    val latestToolEvent: ToolExecutionEvent? = null,
    val sessionDurationSeconds: Long = 0L,
    val isDrawerOpen: Boolean = false
)

class AuronViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuronUiState())
    val uiState: StateFlow<AuronUiState> = _uiState.asStateFlow()

    private val toolManager = ToolManager(application) { event ->
        _uiState.update { current ->
            current.copy(
                latestToolEvent = event,
                recentLogs = listOf(
                    AuronLog(
                        title = "Tool Executed: ${event.name}",
                        detail = event.description,
                        type = LogType.TOOL_CALL
                    )
                ) + current.recentLogs.take(20)
            )
        }
    }

    private val audioPlayer = AudioPlayer(application, viewModelScope) {
        // When playback finishes
        if (_uiState.value.state == AssistantState.SPEAKING) {
            _uiState.update { it.copy(state = AssistantState.LISTENING, statusText = "Listening") }
        }
    }

    private val liveSession: GeminiLiveSession = GeminiLiveSession(
        context = application,
        scope = viewModelScope,
        audioPlayer = audioPlayer,
        toolManager = toolManager,
        onStateChanged = { newState ->
            _uiState.update {
                it.copy(
                    state = newState,
                    statusText = newState.label
                )
            }
        },
        onSubtitleReceived = { subtitle ->
            _uiState.update { it.copy(subtitleText = subtitle) }
        },
        onErrorReceived = { err ->
            _uiState.update { it.copy(state = AssistantState.ERROR, errorMessage = err) }
        },
        onLatencyMeasured = { latency ->
            _uiState.update { it.copy(latencyMs = latency) }
        }
    )

    private val audioStreamer = AudioStreamer(
        context = application,
        scope = viewModelScope,
        onAudioChunkReady = { chunk ->
            liveSession.sendAudioChunk(chunk)
        },
        onSpeechDetected = {
            // Natural voice interruption: user started speaking while Auron was talking
            if (_uiState.value.state == AssistantState.SPEAKING) {
                interrupt()
            }
        }
    )

    private var visualizerJob: Job? = null
    private var sessionTimerJob: Job? = null

    init {
        startVisualizerLoop()
        observeAudioAmplitudes()
    }

    private fun observeAudioAmplitudes() {
        viewModelScope.launch {
            audioStreamer.micAmplitude.collect { amp ->
                _uiState.update { it.copy(micVolume = amp) }
            }
        }
        viewModelScope.launch {
            audioPlayer.outputAmplitude.collect { amp ->
                _uiState.update { it.copy(speakerVolume = amp) }
            }
        }
    }

    private fun startVisualizerLoop() {
        visualizerJob = viewModelScope.launch(Dispatchers.Default) {
            var phase = 0f
            while (isActive) {
                val state = _uiState.value.state
                val micVol = _uiState.value.micVolume
                val speakerVol = _uiState.value.speakerVolume

                val primaryAmp = when (state) {
                    AssistantState.SPEAKING -> speakerVol.coerceAtLeast(0.2f)
                    AssistantState.LISTENING -> micVol.coerceAtLeast(0.08f)
                    AssistantState.THINKING -> 0.4f
                    AssistantState.CONNECTING -> 0.25f
                    else -> 0.05f
                }

                val bars = List(24) { i ->
                    val wave = (sin(phase + i * 0.35f) + 1f) * 0.5f
                    val harmonic = (sin(phase * 1.5f + i * 0.6f) + 1f) * 0.5f
                    val value = (primaryAmp * 0.65f * wave + primaryAmp * 0.35f * harmonic)
                        .coerceIn(0.04f, 1f)
                    value
                }

                phase += 0.18f
                _uiState.update { it.copy(visualizerBars = bars) }
                delay(33) // ~30 fps visualizer animation
            }
        }
    }

    fun setMicPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasMicPermission = granted) }
    }

    fun toggleSession() {
        val currentState = _uiState.value.state
        if (currentState == AssistantState.DISCONNECTED || currentState == AssistantState.ERROR) {
            startSession()
        } else {
            stopSession()
        }
    }

    fun startSession() {
        _uiState.update { it.copy(errorMessage = null) }
        liveSession.updateConfig(_uiState.value.config)
        liveSession.connect()

        if (_uiState.value.hasMicPermission) {
            audioStreamer.startStreaming(
                enableAec = _uiState.value.config.echoCancellation,
                enableNs = _uiState.value.config.noiseSuppression
            )
        }

        startSessionTimer()
    }

    fun stopSession() {
        liveSession.disconnect()
        audioStreamer.stopStreaming()
        audioPlayer.stopAndClear()
        sessionTimerJob?.cancel()
        sessionTimerJob = null
        _uiState.update {
            it.copy(
                state = AssistantState.DISCONNECTED,
                statusText = "Tap to talk",
                sessionDurationSeconds = 0L
            )
        }
    }

    fun interrupt() {
        liveSession.interrupt()
        audioPlayer.stopAndClear()
        _uiState.update {
            it.copy(
                state = AssistantState.LISTENING,
                statusText = "Listening",
                recentLogs = listOf(
                    AuronLog(
                        title = "Interrupted",
                        detail = "User interrupted playback",
                        type = LogType.INTERRUPTION
                    )
                ) + it.recentLogs.take(20)
            )
        }
    }

    fun sendQuickVoicePrompt(prompt: String) {
        if (_uiState.value.state == AssistantState.DISCONNECTED) {
            startSession()
        }
        _uiState.update {
            it.copy(
                subtitleText = "\"$prompt\"",
                recentLogs = listOf(
                    AuronLog(
                        title = "Voice Input",
                        detail = prompt,
                        type = LogType.USER_VOICE
                    )
                ) + it.recentLogs.take(20)
            )
        }
        liveSession.processVoiceQuery(prompt)
    }

    fun updateConfig(newConfig: AuronConfig) {
        _uiState.update { it.copy(config = newConfig) }
        liveSession.updateConfig(newConfig)
    }

    fun toggleDrawer(isOpen: Boolean) {
        _uiState.update { it.copy(isDrawerOpen = isOpen) }
    }

    fun dismissToolBanner() {
        _uiState.update { it.copy(latestToolEvent = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, state = AssistantState.DISCONNECTED) }
    }

    private fun startSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            var seconds = 0L
            while (isActive) {
                delay(1000)
                seconds++
                _uiState.update { it.copy(sessionDurationSeconds = seconds) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioStreamer.stopStreaming()
        audioPlayer.release()
        liveSession.disconnect()
        visualizerJob?.cancel()
        sessionTimerJob?.cancel()
    }
}
