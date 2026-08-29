package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayer
import com.example.audio.AudioStreamer
import com.example.audio.OfflineCommandProcessor
import com.example.audio.WakeWordDetector
import com.example.model.AppThemeMode
import com.example.model.AssistantLanguage
import com.example.model.AssistantState
import com.example.model.AuronConfig
import com.example.model.AuronLog
import com.example.model.CustomTrainingRule
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
    val statusText: String = "কথা বলতে ট্যাপ করুন",
    val subtitleText: String = "নোভা প্রস্তুত। \"Hello Nova\" বলুন অথবা কথা বলুন।",
    val errorMessage: String? = null,
    val latencyMs: Long = 0L,
    val config: AuronConfig = AuronConfig(),
    val hasMicPermission: Boolean = false,
    val recentLogs: List<AuronLog> = emptyList(),
    val latestToolEvent: ToolExecutionEvent? = null,
    val sessionDurationSeconds: Long = 0L,
    val isDrawerOpen: Boolean = false,
    val isWakeWordListening: Boolean = true,
    val showOnboardingTutorial: Boolean = false
)

class AuronViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("nova_ai_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        AuronUiState(
            config = loadConfigFromPrefs(),
            showOnboardingTutorial = !prefs.getBoolean("onboarding_completed", false)
        )
    )
    val uiState: StateFlow<AuronUiState> = _uiState.asStateFlow()

    private val toolManager = ToolManager(application) { event ->
        _uiState.update { current ->
            current.copy(
                latestToolEvent = event,
                recentLogs = listOf(
                    AuronLog(
                        title = "Tool: ${event.name}",
                        detail = event.description,
                        type = LogType.TOOL_CALL
                    )
                ) + current.recentLogs.take(20)
            )
        }
    }

    private val offlineProcessor = OfflineCommandProcessor(application, toolManager)

    private val audioPlayer = AudioPlayer(application, viewModelScope) {
        // When playback finishes
        if (_uiState.value.state == AssistantState.SPEAKING) {
            _uiState.update { it.copy(state = AssistantState.LISTENING, statusText = it.state.getLabel(it.config.language)) }
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
                    statusText = newState.getLabel(it.config.language)
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
            if (_uiState.value.state == AssistantState.SPEAKING) {
                interrupt()
            }
        }
    )

    // Continuous Hands-Free "Hello Nova" Wake Word Detector
    private val wakeWordDetector = WakeWordDetector(
        context = application,
        onWakeWordDetected = { command ->
            onWakeWordTriggered(command)
        },
        onVolumeChanged = { rms ->
            if (_uiState.value.state == AssistantState.DISCONNECTED && _uiState.value.isWakeWordListening) {
                _uiState.update { it.copy(micVolume = (rms / 10f).coerceIn(0f, 1f)) }
            }
        }
    )

    private var visualizerJob: Job? = null
    private var sessionTimerJob: Job? = null

    init {
        audioPlayer.setLanguage(_uiState.value.config.language)
        wakeWordDetector.setLanguage(_uiState.value.config.language)
        wakeWordDetector.setSensitivity(_uiState.value.config.wakeWordSensitivity)
        startVisualizerLoop()
        observeAudioAmplitudes()
    }

    private fun loadConfigFromPrefs(): AuronConfig {
        val langCode = prefs.getString("cfg_language", AssistantLanguage.BENGALI.name) ?: AssistantLanguage.BENGALI.name
        val lang = try { AssistantLanguage.valueOf(langCode) } catch (e: Exception) { AssistantLanguage.BENGALI }
        val wakeEnabled = prefs.getBoolean("cfg_wake_enabled", true)
        val wakeSens = prefs.getFloat("cfg_wake_sens", 0.85f)
        val pitch = prefs.getFloat("cfg_pitch", 1.0f)
        val speechRate = prefs.getFloat("cfg_rate", 1.0f)
        val isVoiceEnrolled = prefs.getBoolean("cfg_voice_enrolled", false)

        return AuronConfig(
            language = lang,
            wakeWordEnabled = wakeEnabled,
            wakeWordSensitivity = wakeSens,
            pitch = pitch,
            speechRate = speechRate,
            isVoiceEnrolled = isVoiceEnrolled
        )
    }

    private fun saveConfigToPrefs(config: AuronConfig) {
        prefs.edit()
            .putString("cfg_language", config.language.name)
            .putBoolean("cfg_wake_enabled", config.wakeWordEnabled)
            .putFloat("cfg_wake_sens", config.wakeWordSensitivity)
            .putFloat("cfg_pitch", config.pitch)
            .putFloat("cfg_rate", config.speechRate)
            .putBoolean("cfg_voice_enrolled", config.isVoiceEnrolled)
            .apply()
    }

    private fun observeAudioAmplitudes() {
        viewModelScope.launch {
            audioStreamer.micAmplitude.collect { amp ->
                if (_uiState.value.state != AssistantState.DISCONNECTED) {
                    _uiState.update { it.copy(micVolume = amp) }
                }
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
                    AssistantState.SPEAKING -> speakerVol.coerceAtLeast(0.25f)
                    AssistantState.LISTENING -> micVol.coerceAtLeast(0.1f)
                    AssistantState.THINKING -> 0.4f
                    AssistantState.CONNECTING -> 0.25f
                    else -> if (_uiState.value.isWakeWordListening) 0.08f else 0.03f
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
                delay(33)
            }
        }
    }

    fun setMicPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasMicPermission = granted) }
        if (granted && _uiState.value.config.wakeWordEnabled) {
            wakeWordDetector.start()
        }
    }

    fun completeOnboardingTutorial() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _uiState.update { it.copy(showOnboardingTutorial = false) }
        if (_uiState.value.hasMicPermission && _uiState.value.config.wakeWordEnabled) {
            wakeWordDetector.start()
        }
    }

    fun restartTutorial() {
        _uiState.update { it.copy(showOnboardingTutorial = true, isDrawerOpen = false) }
    }

    fun enrollUserVoice(voiceName: String) {
        val updated = _uiState.value.config.copy(isVoiceEnrolled = true, userVoiceName = voiceName)
        updateConfig(updated)
    }

    private fun onWakeWordTriggered(command: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val isBengali = _uiState.value.config.language == AssistantLanguage.BENGALI
            if (command.isNotBlank()) {
                val prompt = command.trim()
                _uiState.update {
                    it.copy(
                        subtitleText = "\"$prompt\"",
                        state = AssistantState.THINKING
                    )
                }
                // Try offline execution first for fast device actions
                val offlineResult = offlineProcessor.processCommand(prompt, _uiState.value.config)
                if (offlineResult.handled) {
                    _uiState.update {
                        it.copy(
                            subtitleText = offlineResult.responseText,
                            state = AssistantState.SPEAKING
                        )
                    }
                    audioPlayer.speakText(
                        text = offlineResult.responseText,
                        pitch = _uiState.value.config.pitch,
                        speechRate = _uiState.value.config.speechRate
                    )
                } else {
                    sendQuickVoicePrompt(prompt)
                }
            } else {
                // Just wake word spoken: greet and start listening
                val greeting = if (isBengali) "হ্যাঁ বলুন, নোভা শুনছে।" else "Yes, I'm listening."
                _uiState.update {
                    it.copy(
                        subtitleText = "\"$greeting\"",
                        state = AssistantState.SPEAKING
                    )
                }
                audioPlayer.speakText(
                    text = greeting,
                    pitch = _uiState.value.config.pitch,
                    speechRate = _uiState.value.config.speechRate
                )
                if (_uiState.value.hasMicPermission) {
                    startSession()
                }
            }
        }
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
                statusText = it.state.getLabel(it.config.language),
                sessionDurationSeconds = 0L
            )
        }
        if (_uiState.value.config.wakeWordEnabled && _uiState.value.hasMicPermission) {
            wakeWordDetector.start()
        }
    }

    fun interrupt() {
        liveSession.interrupt()
        audioPlayer.stopAndClear()
        _uiState.update {
            it.copy(
                state = AssistantState.LISTENING,
                statusText = it.state.getLabel(it.config.language),
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
        viewModelScope.launch(Dispatchers.Main) {
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

            // Check if command can be handled offline locally
            val offlineRes = offlineProcessor.processCommand(prompt, _uiState.value.config)
            if (offlineRes.handled) {
                _uiState.update {
                    it.copy(
                        subtitleText = offlineRes.responseText,
                        state = AssistantState.SPEAKING
                    )
                }
                audioPlayer.speakText(
                    text = offlineRes.responseText,
                    pitch = _uiState.value.config.pitch,
                    speechRate = _uiState.value.config.speechRate
                )
            } else {
                if (_uiState.value.state == AssistantState.DISCONNECTED) {
                    startSession()
                }
                liveSession.processVoiceQuery(prompt)
            }
        }
    }

    fun testNovaVoice(sampleText: String) {
        audioPlayer.speakText(
            text = sampleText,
            pitch = _uiState.value.config.pitch,
            speechRate = _uiState.value.config.speechRate
        )
    }

    fun updateConfig(newConfig: AuronConfig) {
        _uiState.update { it.copy(config = newConfig) }
        saveConfigToPrefs(newConfig)
        liveSession.updateConfig(newConfig)
        audioPlayer.setLanguage(newConfig.language)
        wakeWordDetector.setLanguage(newConfig.language)
        wakeWordDetector.setSensitivity(newConfig.wakeWordSensitivity)
        wakeWordDetector.setEnabled(newConfig.wakeWordEnabled)
    }

    fun toggleLanguage(language: AssistantLanguage) {
        val updated = _uiState.value.config.copy(language = language)
        updateConfig(updated)
    }

    fun toggleTheme(themeMode: AppThemeMode) {
        val updated = _uiState.value.config.copy(themeMode = themeMode)
        updateConfig(updated)
    }

    fun addCustomTrainingRule(rule: CustomTrainingRule) {
        val updatedList = _uiState.value.config.customRules + rule
        val updatedConfig = _uiState.value.config.copy(customRules = updatedList)
        updateConfig(updatedConfig)
    }

    fun removeCustomTrainingRule(id: String) {
        val updatedList = _uiState.value.config.customRules.filter { it.id != id }
        val updatedConfig = _uiState.value.config.copy(customRules = updatedList)
        updateConfig(updatedConfig)
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
        wakeWordDetector.stop()
        audioStreamer.stopStreaming()
        audioPlayer.release()
        liveSession.disconnect()
        visualizerJob?.cancel()
        sessionTimerJob?.cancel()
    }
}
