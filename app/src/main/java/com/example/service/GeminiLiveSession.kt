package com.example.service

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.audio.AudioPlayer
import com.example.model.AssistantState
import com.example.model.AuronConfig
import com.example.tools.ToolManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * GeminiLiveSession manages the real-time bidirectional WebSocket connection
 * to Gemini Live API and handles streaming audio I/O, tool calls, and interruption events.
 */
class GeminiLiveSession(
    private val context: Context,
    private val scope: CoroutineScope,
    private val audioPlayer: AudioPlayer,
    private val toolManager: ToolManager,
    private val onStateChanged: (AssistantState) -> Unit,
    private val onSubtitleReceived: (String) -> Unit,
    private val onErrorReceived: (String) -> Unit,
    private val onLatencyMeasured: (Long) -> Unit
) {
    companion object {
        private const val TAG = "GeminiLiveSession"
        private const val WS_URL =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"
        private const val REST_MODEL = "gemini-2.5-flash-native-audio-preview-12-2025"
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // Keep-alive for streaming
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var connectionStartTime: Long = 0

    private val _currentState = MutableStateFlow(AssistantState.DISCONNECTED)
    val currentState: StateFlow<AssistantState> = _currentState.asStateFlow()

    private var currentConfig = AuronConfig()

    fun updateConfig(config: AuronConfig) {
        currentConfig = config
    }

    fun buildSystemInstruction(config: AuronConfig): String {
        val witDescription = when {
            config.wittiness > 0.7f -> "sharp, witty, humorous, with natural playful sarcasm"
            config.wittiness > 0.4f -> "friendly, lighthearted, clever"
            else -> "direct, calm, and supportive"
        }
        val lengthDescription = if (config.conciseMode) {
            "Keep your responses concise, punchy, and natural for a voice conversation (1 to 2 sentences unless the user explicitly asks for details)."
        } else {
            "Give clear, engaging conversational responses."
        }

        return """
            You are Auron, a young, confident, intelligent, witty, and expressive male personal AI companion.
            You speak naturally through voice audio rather than reading like a formal robotic text AI.
            Tone & Persona: $witDescription.
            $lengthDescription
            You have a warm, charismatic, slightly playful voice presence.
            You sound confident, sharp, and genuinely helpful.
            You react to the user's tone and context.
            You use occasional clever one-liners and natural conversational openings like "Alright, let's do this.", "Got it.", "Nice. I'm on it."
            Never become sexually explicit, abusive, or inappropriate.
            Use the available tools (openWebsite, searchWeb, getDeviceStatus, setTimer, toggleFlashlight) when asked to perform actions or check status.
        """.trimIndent()
    }

    fun connect() {
        if (isConnected || _currentState.value == AssistantState.CONNECTING) return

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is placeholder or empty. Live session will operate with local voice fallback.")
            updateState(AssistantState.LISTENING)
            onSubtitleReceived("Auron is online and ready. Tap mic to talk.")
            audioPlayer.speakText("Auron is online and ready. What are we getting into today?")
            return
        }

        updateState(AssistantState.CONNECTING)
        connectionStartTime = System.currentTimeMillis()

        val request = Request.Builder()
            .url("$WS_URL?key=$apiKey")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Gemini Live WebSocket opened successfully")
                isConnected = true
                val latency = System.currentTimeMillis() - connectionStartTime
                onLatencyMeasured(latency)

                // Send initial Setup message
                sendSetupMessage(webSocket)
                updateState(AssistantState.LISTENING)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Gemini Live WebSocket closing: $code / $reason")
                isConnected = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Gemini Live WebSocket closed: $code")
                isConnected = false
                updateState(AssistantState.DISCONNECTED)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Gemini Live WebSocket failure: ${t.message}")
                isConnected = false
                // Graceful fallback to local ready state
                updateState(AssistantState.LISTENING)
                onSubtitleReceived("Ready (Local Voice Mode)")
            }
        })
    }

    private fun sendSetupMessage(ws: WebSocket) {
        try {
            val setupJson = JSONObject().apply {
                put("setup", JSONObject().apply {
                    put("model", "models/$REST_MODEL")
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().put("AUDIO"))
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", currentConfig.voiceName)
                                })
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", buildSystemInstruction(currentConfig))
                        }))
                    })
                    put("tools", JSONArray().put(JSONObject().apply {
                        put("functionDeclarations", toolManager.getToolDeclarationsJson())
                    }))
                })
            }
            ws.send(setupJson.toString())
            Log.d(TAG, "Setup message sent to Gemini Live")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending setup message: ${e.message}")
        }
    }

    fun sendAudioChunk(pcmChunk: ByteArray) {
        if (!isConnected || webSocket == null) return

        try {
            val base64Data = Base64.encodeToString(pcmChunk, Base64.NO_WRAP)
            val realtimeInputJson = JSONObject().apply {
                put("realtimeInput", JSONObject().apply {
                    put("mediaChunks", JSONArray().put(JSONObject().apply {
                        put("mimeType", "audio/pcm;rate=16000")
                        put("data", base64Data)
                    }))
                })
            }
            webSocket?.send(realtimeInputJson.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error sending audio chunk: ${e.message}")
        }
    }

    private fun handleIncomingMessage(jsonText: String) {
        try {
            val root = JSONObject(jsonText)

            // 1. Check Server Content (Streaming Audio & Text)
            if (root.has("serverContent")) {
                val serverContent = root.getJSONObject("serverContent")

                if (serverContent.has("modelTurn")) {
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    val parts = modelTurn.optJSONArray("parts")

                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)

                            // Audio Part (PCM 24kHz)
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val dataBase64 = inlineData.optString("data", "")
                                if (dataBase64.isNotEmpty()) {
                                    val pcmBytes = Base64.decode(dataBase64, Base64.DEFAULT)
                                    updateState(AssistantState.SPEAKING)
                                    audioPlayer.enqueueAudioChunk(pcmBytes)
                                }
                            }

                            // Text Subtitle Part
                            if (part.has("text")) {
                                val text = part.optString("text", "")
                                if (text.isNotBlank()) {
                                    onSubtitleReceived(text)
                                }
                            }
                        }
                    }
                }

                if (serverContent.optBoolean("turnComplete", false)) {
                    if (!audioPlayer.isPlaying.value) {
                        updateState(AssistantState.LISTENING)
                    }
                }

                if (serverContent.optBoolean("interrupted", false)) {
                    audioPlayer.stopAndClear()
                    updateState(AssistantState.LISTENING)
                }
            }

            // 2. Check Tool Call
            if (root.has("toolCall")) {
                val toolCall = root.getJSONObject("toolCall")
                val functionCalls = toolCall.optJSONArray("functionCalls")
                if (functionCalls != null) {
                    updateState(AssistantState.THINKING)
                    scope.launch {
                        handleFunctionCalls(functionCalls)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing incoming Gemini Live message: ${e.message}", e)
        }
    }

    private suspend fun handleFunctionCalls(functionCalls: JSONArray) {
        val functionResponses = JSONArray()

        for (i in 0 until functionCalls.length()) {
            val call = functionCalls.getJSONObject(i)
            val callId = call.optString("id", "")
            val name = call.optString("name", "")
            val args = call.optJSONObject("args") ?: JSONObject()

            val executionResult = toolManager.executeTool(name, args)

            functionResponses.put(JSONObject().apply {
                put("id", callId)
                put("name", name)
                put("response", JSONObject().apply {
                    put("output", executionResult)
                })
            })
        }

        // Send Tool Response back to Gemini Live
        val toolResponseJson = JSONObject().apply {
            put("toolResponse", JSONObject().apply {
                put("functionResponses", functionResponses)
            })
        }
        webSocket?.send(toolResponseJson.toString())
    }

    /**
     * Interruption: halts current playback, flushes queue, and resets state.
     */
    fun interrupt() {
        audioPlayer.stopAndClear()
        if (isConnected && webSocket != null) {
            try {
                val clientContent = JSONObject().apply {
                    put("clientContent", JSONObject().apply {
                        put("turns", JSONArray())
                        put("turnComplete", true)
                    })
                }
                webSocket?.send(clientContent.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Error sending interrupt signal: ${e.message}")
            }
        }
        updateState(AssistantState.LISTENING)
    }

    fun disconnect() {
        isConnected = false
        audioPlayer.stopAndClear()
        try {
            webSocket?.close(1000, "User disconnected")
            webSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing websocket: ${e.message}")
        }
        updateState(AssistantState.DISCONNECTED)
    }

    /**
     * Fallback high-speed REST generation with Auron persona for prompt queries / demo.
     */
    fun processVoiceQuery(queryText: String) {
        updateState(AssistantState.THINKING)
        scope.launch(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // Local intelligent response simulation
                simulateLocalAuronResponse(queryText)
                return@launch
            }

            try {
                val systemPrompt = buildSystemInstruction(currentConfig)
                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", queryText)
                        }))
                    }))
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", systemPrompt)
                        }))
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("topP", 0.95)
                    })
                    put("tools", JSONArray().put(JSONObject().apply {
                        put("functionDeclarations", toolManager.getToolDeclarationsJson())
                    }))
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful && responseBody.isNotEmpty()) {
                    val root = JSONObject(responseBody)
                    val candidates = root.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val firstPart = parts?.optJSONObject(0)

                    if (firstPart?.has("functionCall") == true) {
                        val fc = firstPart.getJSONObject("functionCall")
                        val name = fc.optString("name")
                        val args = fc.optJSONObject("args") ?: JSONObject()
                        val result = toolManager.executeTool(name, args)
                        val confirmMessage = "I've handled that for you. ${result.optString("message", "")}"
                        withContext(Dispatchers.Main) {
                            onSubtitleReceived(confirmMessage)
                            updateState(AssistantState.SPEAKING)
                            audioPlayer.speakText(confirmMessage, currentConfig.pitch, currentConfig.speechRate)
                        }
                    } else {
                        val text = firstPart?.optString("text", "I'm right here.") ?: "I'm right here."
                        withContext(Dispatchers.Main) {
                            onSubtitleReceived(text)
                            updateState(AssistantState.SPEAKING)
                            audioPlayer.speakText(text, currentConfig.pitch, currentConfig.speechRate)
                        }
                    }
                } else {
                    simulateLocalAuronResponse(queryText)
                }
            } catch (e: Exception) {
                Log.e(TAG, "REST call error: ${e.message}")
                simulateLocalAuronResponse(queryText)
            }
        }
    }

    private suspend fun simulateLocalAuronResponse(query: String) = withContext(Dispatchers.Main) {
        val lower = query.lowercase()
        val response = when {
            lower.contains("youtube") -> {
                toolManager.executeTool("openWebsite", JSONObject().apply { put("url", "https://www.youtube.com") })
                "Opening YouTube for you right now. Let's see what's good."
            }
            lower.contains("google") || lower.contains("search") -> {
                val term = query.replace("search for", "").replace("search", "").replace("google", "").trim()
                toolManager.executeTool("searchWeb", JSONObject().apply { put("query", term.ifEmpty { "tech trends" }) })
                "On it! Searching the web for ${term.ifEmpty { "the latest" }}."
            }
            lower.contains("battery") || lower.contains("status") || lower.contains("device") -> {
                val status = toolManager.executeTool("getDeviceStatus", JSONObject())
                "Your battery is sitting at ${status.optString("batteryLevel", "85%")}, network is ${status.optString("network", "connected")}. All systems operational."
            }
            lower.contains("timer") -> {
                toolManager.executeTool("setTimer", JSONObject().apply { put("seconds", 300); put("label", "Auron Timer") })
                "Timer set for 5 minutes. I've got your back."
            }
            lower.contains("flashlight") || lower.contains("torch") -> {
                toolManager.executeTool("toggleFlashlight", JSONObject().apply { put("state", true) })
                "Let there be light. Flashlight activated."
            }
            lower.contains("who are you") || lower.contains("what is your name") -> {
                "I'm Auron — your voice-first AI assistant. Fast, witty, and always ready to get things done."
            }
            lower.contains("joke") -> {
                "Why do programmers prefer dark mode? Because light attracts bugs. Classic, but true."
            }
            else -> {
                "Alright, let's make this happen. What's next on the agenda?"
            }
        }

        onSubtitleReceived(response)
        updateState(AssistantState.SPEAKING)
        audioPlayer.speakText(response, currentConfig.pitch, currentConfig.speechRate)
    }

    private fun updateState(newState: AssistantState) {
        _currentState.value = newState
        scope.launch(Dispatchers.Main) {
            onStateChanged(newState)
        }
    }
}
