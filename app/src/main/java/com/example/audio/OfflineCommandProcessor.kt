package com.example.audio

import android.content.Context
import com.example.model.AssistantLanguage
import com.example.model.AuronConfig
import com.example.model.CustomTrainingRule
import com.example.tools.ToolManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class OfflineExecutionResult(
    val handled: Boolean,
    val responseText: String,
    val actionName: String? = null
)

class OfflineCommandProcessor(
    private val context: Context,
    private val toolManager: ToolManager
) {
    suspend fun processCommand(
        query: String,
        config: AuronConfig
    ): OfflineExecutionResult = withContext(Dispatchers.Main) {
        val clean = query.trim().lowercase()
        val isBengali = config.language == AssistantLanguage.BENGALI

        // 1. Check custom trained rules first
        for (rule in config.customRules) {
            if (clean.contains(rule.triggerPhrase.lowercase().trim())) {
                executeRuleAction(rule)
                val reply = if (rule.customReply.isNotBlank()) rule.customReply else {
                    if (isBengali) "আপনার শেখানো কমান্ড কার্যকর করা হয়েছে।" else "Executed custom learned command."
                }
                return@withContext OfflineExecutionResult(true, reply, rule.actionType)
            }
        }

        // 2. YouTube
        if (clean.contains("youtube") || clean.contains("ইউটিউব")) {
            if (clean.contains("search") || clean.contains("সার্চ") || clean.contains("খুঁজ")) {
                val searchQuery = clean.replace("youtube", "").replace("ইউটিউব", "").replace("search", "").replace("সার্চ", "").trim()
                toolManager.executeTool("searchYouTube", JSONObject().put("query", searchQuery))
                val reply = if (isBengali) "ইউটিউবে '$searchQuery' অনুসন্ধান করছি।" else "Searching YouTube for '$searchQuery'"
                return@withContext OfflineExecutionResult(true, reply, "searchYouTube")
            } else {
                toolManager.launchNamedApp("youtube")
                val reply = if (isBengali) "ইউটিউব ওপেন করছি।" else "Opening YouTube for you."
                return@withContext OfflineExecutionResult(true, reply, "openApp")
            }
        }

        // 3. Facebook
        if (clean.contains("facebook") || clean.contains("ফেসবুক") || clean.contains("fb")) {
            toolManager.launchNamedApp("facebook")
            val reply = if (isBengali) "ফেসবুক ওপেন করছি।" else "Opening Facebook for you."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 4. WhatsApp
        if (clean.contains("whatsapp") || clean.contains("হোয়াটসঅ্যাপ") || clean.contains("হোয়াটসএপ")) {
            toolManager.launchNamedApp("whatsapp")
            val reply = if (isBengali) "হোয়াটসঅ্যাপ ওপেন করছি।" else "Opening WhatsApp."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 5. Video Downloader
        if (clean.contains("download") || clean.contains("ডাউনলোড") || clean.contains("সেভ কর")) {
            val platform = when {
                clean.contains("facebook") || clean.contains("ফেসবুক") -> "facebook"
                clean.contains("youtube") || clean.contains("ইউটিউব") -> "youtube"
                clean.contains("insta") || clean.contains("ইন্সটা") -> "instagram"
                else -> "general"
            }
            toolManager.executeTool("downloadVideoHelper", JSONObject().put("platform", platform))
            val reply = if (isBengali) "ভিডিও ডাউনলোড হেল্পার ওপেন করা হয়েছে। লিংক পেস্ট করে ডাউনলোড করুন।" else "Opening video download helper tool."
            return@withContext OfflineExecutionResult(true, reply, "downloadVideoHelper")
        }

        // 6. Phone Call
        if (clean.contains("call") || clean.contains("ফোন") || clean.contains("ডায়াল") || clean.contains("ডায়াল")) {
            // Extract potential phone digits
            val digits = clean.filter { it.isDigit() || it == '+' }
            val phoneNum = if (digits.length >= 3) digits else ""
            toolManager.executeTool("makePhoneCall", JSONObject().put("phoneNumber", phoneNum))
            val reply = if (isBengali) {
                if (phoneNum.isNotEmpty()) "$phoneNum নম্বরে ডায়াল করছি।" else "ফোন ডায়ালার ওপেন করছি।"
            } else {
                if (phoneNum.isNotEmpty()) "Dialing $phoneNum." else "Opening phone dialer."
            }
            return@withContext OfflineExecutionResult(true, reply, "makePhoneCall")
        }

        // 7. Message / SMS
        if (clean.contains("message") || clean.contains("মেসেজ") || clean.contains("sms") || clean.contains("এসএমএস")) {
            toolManager.executeTool("sendMessage", JSONObject().put("phoneNumber", "").put("message", ""))
            val reply = if (isBengali) "মেসেজ পাঠানোর স্ক্রিন ওপেন করছি।" else "Opening messaging."
            return@withContext OfflineExecutionResult(true, reply, "sendMessage")
        }

        // 8. Flashlight / Torch
        if (clean.contains("flash") || clean.contains("torch") || clean.contains("লাইট") || clean.contains("ফ্ল্যাশ")) {
            val turnOn = !clean.contains("off") && !clean.contains("বন্ধ") && !clean.contains("নিভ")
            toolManager.executeTool("toggleFlashlight", JSONObject().put("state", turnOn))
            val reply = if (isBengali) {
                if (turnOn) "ফ্ল্যাশলাইট অন করা হয়েছে।" else "ফ্ল্যাশলাইট বন্ধ করা হয়েছে।"
            } else {
                if (turnOn) "Flashlight turned on." else "Flashlight turned off."
            }
            return@withContext OfflineExecutionResult(true, reply, "toggleFlashlight")
        }

        // 9. Battery / Device Status
        if (clean.contains("battery") || clean.contains("চার্জ") || clean.contains("ব্যাটারি") || clean.contains("চার্জ কত")) {
            val statusResult = toolManager.executeTool("getDeviceStatus", JSONObject())
            val level = statusResult.optString("batteryLevel", "unknown")
            val charging = statusResult.optBoolean("isCharging", false)
            val reply = if (isBengali) {
                "আপনার ডিভাইসের ব্যাটারি চার্জ $level ${if (charging) "(চার্জ হচ্ছে)" else ""}।"
            } else {
                "Your battery level is $level ${if (charging) "(currently charging)" else ""}."
            }
            return@withContext OfflineExecutionResult(true, reply, "getDeviceStatus")
        }

        // 10. Camera
        if (clean.contains("camera") || clean.contains("ক্যামেরা") || clean.contains("ছবি তুলব")) {
            toolManager.launchNamedApp("camera")
            val reply = if (isBengali) "ক্যামেরা ওপেন করছি।" else "Opening camera."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 11. Settings
        if (clean.contains("setting") || clean.contains("সেটিংস") || clean.contains("সেটিং")) {
            toolManager.launchNamedApp("settings")
            val reply = if (isBengali) "ডিভাইস সেটিংস ওপেন করছি।" else "Opening device settings."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 12. Identity / General query offline
        if (clean.contains("who are you") || clean.contains("কে তুমি") || clean.contains("তোমার নাম কি") || clean.contains("নাম কি")) {
            val reply = if (isBengali) {
                "আমি নোভা এআই (Nova AI)। আমি অফলাইনে এবং অনলাইনে আপনার ভয়েস কমান্ড অনুযায়ী বিভিন্ন অ্যাপ চালানো ও কাজ করতে প্রস্তুত।"
            } else {
                "I am Nova AI, your intelligent voice assistant capable of running commands both online and offline."
            }
            return@withContext OfflineExecutionResult(true, reply, "identity")
        }

        // Default offline response
        val fallback = if (isBengali) {
            "অফলাইন মোডে আছি। আপনি বলতে পারেন: 'ইউটিউব ওপেন কর', 'ফেসবুক ওপেন কর', 'ফোন দাও', 'ভিডিও ডাউনলোড', 'ব্যাটারি কত' ইত্যাদি।"
        } else {
            "I'm in offline mode. You can say 'Open YouTube', 'Open Facebook', 'Make a Call', 'Download Video', or check battery."
        }
        return@withContext OfflineExecutionResult(false, fallback)
    }

    private suspend fun executeRuleAction(rule: CustomTrainingRule) {
        when (rule.actionType) {
            "OPEN_APP" -> toolManager.launchNamedApp(rule.actionParam)
            "OPEN_URL" -> toolManager.executeTool("openWebsite", JSONObject().put("url", rule.actionParam))
            "PHONE_CALL" -> toolManager.executeTool("makePhoneCall", JSONObject().put("phoneNumber", rule.actionParam))
            "SEND_SMS" -> toolManager.executeTool("sendMessage", JSONObject().put("phoneNumber", rule.actionParam).put("message", ""))
            "DOWNLOAD_HELPER" -> toolManager.executeTool("downloadVideoHelper", JSONObject().put("platform", rule.actionParam))
            "TIMER" -> toolManager.executeTool("setTimer", JSONObject().put("seconds", rule.actionParam.toIntOrNull() ?: 300))
        }
    }
}
