package com.example.model

enum class AssistantLanguage(val code: String, val displayName: String, val nativeName: String, val greeting: String) {
    BENGALI("bn-BD", "Bengali", "বাংলা", "হ্যালো! আমি নোভা এআই। আপনার কি সাহায্য করতে পারি?"),
    ENGLISH("en-US", "English", "English", "Hello! I am Nova AI. How can I assist you today?"),
    HINDI("hi-IN", "Hindi", "हिंदी", "नमस्ते! मैं नोवा एআই हूँ। मैं आपकी क्या मदद कर सकता हूँ?")
}

enum class AppThemeMode(val displayName: String) {
    DARK("ডার্ক থিম (Dark)"),
    LIGHT("লাইট থিম (Light)"),
    SYSTEM("সিস্টেম ডিফল্ট (System)")
}

enum class AssistantState {
    DISCONNECTED,
    CONNECTING,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR;

    fun getLabel(language: AssistantLanguage = AssistantLanguage.BENGALI): String = when (this) {
        DISCONNECTED -> if (language == AssistantLanguage.BENGALI) "কথা বলতে ট্যাপ করুন" else "Tap to talk"
        CONNECTING -> if (language == AssistantLanguage.BENGALI) "সংযুক্ত হচ্ছে..." else "Connecting..."
        LISTENING -> if (language == AssistantLanguage.BENGALI) "নোভা শুনছে..." else "Listening..."
        THINKING -> if (language == AssistantLanguage.BENGALI) "নোভা ভাবছে..." else "Thinking..."
        SPEAKING -> if (language == AssistantLanguage.BENGALI) "নোভা কথা বলছে" else "Nova is speaking"
        ERROR -> if (language == AssistantLanguage.BENGALI) "কানেকশন সমস্যা" else "Connection error"
    }

    val label: String
        get() = getLabel(AssistantLanguage.BENGALI)
}

data class CustomTrainingRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val triggerPhrase: String,
    val actionType: String, // "OPEN_APP", "OPEN_URL", "PHONE_CALL", "SEND_SMS", "DOWNLOAD_HELPER", "TIMER", "SPEAK"
    val actionParam: String = "",
    val customReply: String = ""
)

data class AuronConfig(
    val language: AssistantLanguage = AssistantLanguage.BENGALI,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val wakeWordEnabled: Boolean = true,
    val wakeWordSensitivity: Float = 0.85f,
    val offlineVoiceEnabled: Boolean = true,
    val wittiness: Float = 0.85f,
    val energyLevel: Float = 0.8f,
    val sarcasm: Boolean = false,
    val conciseMode: Boolean = true,
    val voiceName: String = "Puck",
    val echoCancellation: Boolean = true,
    val noiseSuppression: Boolean = true,
    val interruptSensitivity: Float = 0.5f,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val isVoiceEnrolled: Boolean = false,
    val userVoiceName: String = "User",
    val onboardingCompleted: Boolean = false,
    val customRules: List<CustomTrainingRule> = listOf(
        CustomTrainingRule(
            triggerPhrase = "ইউটিউব ওপেন কর",
            actionType = "OPEN_APP",
            actionParam = "youtube",
            customReply = "ইউটিউব ওপেন করছি।"
        ),
        CustomTrainingRule(
            triggerPhrase = "ফেসবুক ওপেন কর",
            actionType = "OPEN_APP",
            actionParam = "facebook",
            customReply = "ফেসবুক ওপেন করছি।"
        ),
        CustomTrainingRule(
            triggerPhrase = "ভিডিও ডাউনলোড",
            actionType = "DOWNLOAD_HELPER",
            actionParam = "all",
            customReply = "ভিডিও ডাউনলোড হেল্পার টুল চালু করছি।"
        )
    )
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
