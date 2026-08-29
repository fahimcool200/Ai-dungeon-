package com.example.model

enum class AssistantState {
    DISCONNECTED,
    CONNECTING,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR;

    val label: String
        get() = when (this) {
            DISCONNECTED -> "Tap to talk"
            CONNECTING -> "Connecting..."
            LISTENING -> "Listening"
            THINKING -> "Thinking..."
            SPEAKING -> "Auron is speaking"
            ERROR -> "Connection error"
        }
}

data class AuronConfig(
    val wittiness: Float = 0.85f,
    val energyLevel: Float = 0.8f,
    val sarcasm: Boolean = true,
    val conciseMode: Boolean = true,
    val voiceName: String = "Puck",
    val echoCancellation: Boolean = true,
    val noiseSuppression: Boolean = true,
    val interruptSensitivity: Float = 0.5f,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.05f
)

enum class LogType {
    INFO,
    USER_VOICE,
    AURON_VOICE,
    TOOL_CALL,
    INTERRUPTION,
    ERROR
}

data class AuronLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val detail: String,
    val type: LogType
)

data class ToolExecutionEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean = true,
    val iconName: String = "tool"
)
