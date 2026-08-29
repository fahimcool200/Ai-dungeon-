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
                    if (isBengali) "আপনার শেখানো নিয়ম অনুযায়ী কাজ সম্পন্ন হয়েছে।" else "Executed custom learned command."
                }
                return@withContext OfflineExecutionResult(true, reply, rule.actionType)
            }
        }

        // 2. Play music / video on YouTube (e.g. "গান চালাও", "play music", "ইউটিউবে গান")
        if (clean.contains("গান") || clean.contains("music") || clean.contains("song") || clean.contains("ভিডিও চালাও") || clean.contains("play ")) {
            var searchSong = clean
                .replace("গান চালাও", "")
                .replace("গান শোনাও", "")
                .replace("গান বাজাও", "")
                .replace("ভিডিও চালাও", "")
                .replace("play", "")
                .replace("song", "")
                .replace("music", "")
                .replace("on youtube", "")
                .replace("ইউটিউবে", "")
                .replace("নোভা", "")
                .trim()
            if (searchSong.isEmpty()) {
                searchSong = if (isBengali) "বাংলা জনপ্রিয় গান" else "popular trending music"
            }
            toolManager.executeTool("searchYouTube", JSONObject().put("query", searchSong))
            val reply = if (isBengali) "ইউটিউবে '$searchSong' বাজাচ্ছি।" else "Playing '$searchSong' on YouTube."
            return@withContext OfflineExecutionResult(true, reply, "searchYouTube")
        }

        // 3. YouTube App / Search
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

        // 4. Facebook
        if (clean.contains("facebook") || clean.contains("ফেসবুক") || clean.contains("fb")) {
            toolManager.launchNamedApp("facebook")
            val reply = if (isBengali) "ফেসবুক ওপেন করছি।" else "Opening Facebook for you."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 5. WhatsApp
        if (clean.contains("whatsapp") || clean.contains("হোয়াটসঅ্যাপ") || clean.contains("হোয়াটসএপ")) {
            toolManager.launchNamedApp("whatsapp")
            val reply = if (isBengali) "হোয়াটসঅ্যাপ ওপেন করছি।" else "Opening WhatsApp."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 6. Video Downloader
        if (clean.contains("download") || clean.contains("ডাউনলোড") || clean.contains("সেভ কর")) {
            val platform = when {
                clean.contains("facebook") || clean.contains("ফেসবুক") -> "facebook"
                clean.contains("youtube") || clean.contains("ইউটিউব") -> "youtube"
                clean.contains("insta") || clean.contains("ইন্সটা") -> "instagram"
                else -> "general"
            }
            toolManager.executeTool("downloadVideoHelper", JSONObject().put("platform", platform))
            val reply = if (isBengali) "ভিডিও ডাউনলোডার টুল ওপেন করা হয়েছে। লিংক পেস্ট করে ডাউনলোড করুন।" else "Opening video download helper."
            return@withContext OfflineExecutionResult(true, reply, "downloadVideoHelper")
        }

        // 7. Phone Call / Dialing
        if (clean.contains("call") || clean.contains("ফোন") || clean.contains("ডায়াল") || clean.contains("ডায়াল")) {
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

        // 8. SMS / Messaging
        if (clean.contains("message") || clean.contains("মেসেজ") || clean.contains("sms") || clean.contains("এসএমএস")) {
            toolManager.executeTool("sendMessage", JSONObject().put("phoneNumber", "").put("message", ""))
            val reply = if (isBengali) "মেসেজ স্ক্রিন ওপেন করছি।" else "Opening messaging."
            return@withContext OfflineExecutionResult(true, reply, "sendMessage")
        }

        // 9. Flashlight / Torch
        if (clean.contains("flash") || clean.contains("torch") || clean.contains("লাইট") || clean.contains("ফ্ল্যাশ") || clean.contains("টর্চ")) {
            val turnOn = !clean.contains("off") && !clean.contains("বন্ধ") && !clean.contains("নিভ")
            toolManager.executeTool("toggleFlashlight", JSONObject().put("state", turnOn))
            val reply = if (isBengali) {
                if (turnOn) "ফ্ল্যাশলাইট অন করা হয়েছে।" else "ফ্ল্যাশলাইট বন্ধ করা হয়েছে।"
            } else {
                if (turnOn) "Flashlight turned on." else "Flashlight turned off."
            }
            return@withContext OfflineExecutionResult(true, reply, "toggleFlashlight")
        }

        // 10. Battery / Device Status
        if (clean.contains("battery") || clean.contains("চার্জ") || clean.contains("ব্যাটারি") || clean.contains("চার্জ কত")) {
            val statusResult = toolManager.executeTool("getDeviceStatus", JSONObject())
            val level = statusResult.optString("batteryLevel", "unknown")
            val charging = statusResult.optBoolean("isCharging", false)
            val reply = if (isBengali) {
                "আপনার ডিভাইসের ব্যাটারি চার্জ $level ${if (charging) "(চার্জ হচ্ছে)" else ""}।"
            } else {
                "Your device battery is at $level ${if (charging) "(charging)" else ""}."
            }
            return@withContext OfflineExecutionResult(true, reply, "getDeviceStatus")
        }

        // 11. Camera
        if (clean.contains("camera") || clean.contains("ক্যামেরা") || clean.contains("ছবি তুলব") || clean.contains("ছবি তোল")) {
            toolManager.launchNamedApp("camera")
            val reply = if (isBengali) "ক্যামেরা ওপেন করছি।" else "Opening camera."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 12. Calculator
        if (clean.contains("calculator") || clean.contains("ক্যালকুলেটর") || clean.contains("হিসাব")) {
            toolManager.launchNamedApp("calculator")
            val reply = if (isBengali) "ক্যালকুলেটর ওপেন করছি।" else "Opening calculator."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 13. Clock / Alarm / Timer
        if (clean.contains("alarm") || clean.contains("এলার্ম") || clean.contains("ঘড়ি") || clean.contains("clock") || clean.contains("timer") || clean.contains("টাইমার")) {
            val seconds = if (clean.contains("মিনিট")) {
                val num = clean.filter { it.isDigit() }.toIntOrNull() ?: 5
                num * 60
            } else 300
            toolManager.executeTool("setTimer", JSONObject().put("seconds", seconds))
            val reply = if (isBengali) "টাইমার ও ঘড়ি চালু করছি।" else "Starting timer and clock."
            return@withContext OfflineExecutionResult(true, reply, "setTimer")
        }

        // 14. Maps / Google Maps
        if (clean.contains("map") || clean.contains("ম্যাপ") || clean.contains("রাস্তা")) {
            toolManager.launchNamedApp("maps")
            val reply = if (isBengali) "গুগল ম্যাপস ওপেন করছি।" else "Opening Google Maps."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 15. Settings (WiFi, Bluetooth, Main Settings)
        if (clean.contains("setting") || clean.contains("সেটিংস") || clean.contains("সেটিং") || clean.contains("ওয়াইফাই") || clean.contains("wifi") || clean.contains("ব্লুটুথ") || clean.contains("bluetooth")) {
            toolManager.launchNamedApp("settings")
            val reply = if (isBengali) "ডিভাইস সেটিংস ওপেন করছি।" else "Opening device settings."
            return@withContext OfflineExecutionResult(true, reply, "openApp")
        }

        // 16. Identity / Nova intro
        if (clean.contains("who are you") || clean.contains("কে তুমি") || clean.contains("তোমার নাম কি") || clean.contains("নাম কি")) {
            val reply = if (isBengali) {
                "আমি নোভা এআই (Nova AI)। আইফোনের মতো সম্পূর্ণ হ্যান্ডস-ফ্রি ভয়েস অ্যাসিস্ট্যান্ট। আপনি মুখ দিয়ে যা বলবেন আমি তাই করব।"
            } else {
                "I am Nova AI, your hands-free intelligent voice robot assistant. Speak any command and I will execute it."
            }
            return@withContext OfflineExecutionResult(true, reply, "identity")
        }

        // Default offline response
        val fallback = if (isBengali) {
            "অফলাইন মোড সক্রিয়। বলতে পারেন: 'গান চালাও', 'ইউটিউব ওপেন কর', 'ফেসবুক', 'ফোন দাও', 'ভিডিও ডাউনলোড', 'টর্চ অন কর', 'ব্যাটারি কত' ইত্যাদি।"
        } else {
            "Offline mode active. You can say 'Play Music', 'Open YouTube', 'Open Facebook', 'Make a Call', 'Download Video', or 'Turn on Flashlight'."
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
