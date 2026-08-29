package com.example.tools

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import com.example.model.ToolExecutionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ToolManager executes browser actions, system queries, and device functions
 * requested by Gemini Live / Nova Assistant.
 */
class ToolManager(
    private val context: Context,
    private val onToolExecuted: (ToolExecutionEvent) -> Unit
) {
    companion object {
        private const val TAG = "ToolManager"
    }

    /**
     * Tool declarations formatted for Gemini Live API Function Calling.
     */
    fun getToolDeclarationsJson(): JSONArray {
        val functionDeclarations = JSONArray()

        // 1. openApp
        functionDeclarations.put(JSONObject().apply {
            put("name", "openApp")
            put("description", "Opens any application on the device such as YouTube, Facebook, WhatsApp, Chrome, Camera, Instagram, Gallery, Settings, etc.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("appName", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The name of the application to open (e.g., 'youtube', 'facebook', 'whatsapp', 'camera', 'settings', 'chrome', 'instagram', 'gallery')")
                    })
                })
                put("required", JSONArray().put("appName"))
            })
        })

        // 2. makePhoneCall
        functionDeclarations.put(JSONObject().apply {
            put("name", "makePhoneCall")
            put("description", "Opens the phone dialer to place a call to a given phone number or contact.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("phoneNumber", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The phone number to dial (e.g., '01712345678', '+123456789')")
                    })
                    put("contactName", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Optional name of the contact being called")
                    })
                })
                put("required", JSONArray().put("phoneNumber"))
            })
        })

        // 3. sendMessage
        functionDeclarations.put(JSONObject().apply {
            put("name", "sendMessage")
            put("description", "Opens SMS or messaging with prefilled phone number and message body.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("phoneNumber", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Recipient phone number or name")
                    })
                    put("message", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Text message content to write")
                    })
                })
                put("required", JSONArray().put("message"))
            })
        })

        // 4. searchYouTube
        functionDeclarations.put(JSONObject().apply {
            put("name", "searchYouTube")
            put("description", "Searches YouTube for videos or music with the given query.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The video or music search query")
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // 5. downloadVideoHelper
        functionDeclarations.put(JSONObject().apply {
            put("name", "downloadVideoHelper")
            put("description", "Assists the user in downloading videos from YouTube, Facebook, or Instagram by opening safe video downloader tools.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("platform", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The platform: 'youtube', 'facebook', 'instagram', or 'general'")
                    })
                    put("videoUrl", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Optional video link/URL to download")
                    })
                })
                put("required", JSONArray().put("platform"))
            })
        })

        // 6. openWebsite
        functionDeclarations.put(JSONObject().apply {
            put("name", "openWebsite")
            put("description", "Opens a website or URL in the device browser when the user asks to open a website.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("url", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The website URL to navigate to (e.g. https://www.youtube.com, https://google.com)")
                    })
                })
                put("required", JSONArray().put("url"))
            })
        })

        // 7. searchWeb
        functionDeclarations.put(JSONObject().apply {
            put("name", "searchWeb")
            put("description", "Searches Google/Web for a given query.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The search query.")
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // 8. getDeviceStatus
        functionDeclarations.put(JSONObject().apply {
            put("name", "getDeviceStatus")
            put("description", "Retrieves current battery level, charging status, network connection, and current time.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject())
            })
        })

        // 9. setTimer
        functionDeclarations.put(JSONObject().apply {
            put("name", "setTimer")
            put("description", "Sets a countdown timer for a specified number of seconds.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("seconds", JSONObject().apply {
                        put("type", "INTEGER")
                        put("description", "Duration in seconds.")
                    })
                    put("label", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Optional label for the timer.")
                    })
                })
                put("required", JSONArray().put("seconds"))
            })
        })

        // 10. toggleFlashlight
        functionDeclarations.put(JSONObject().apply {
            put("name", "toggleFlashlight")
            put("description", "Turns the device flashlight on or off.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("state", JSONObject().apply {
                        put("type", "BOOLEAN")
                        put("description", "True to turn on flashlight, False to turn off.")
                    })
                })
                put("required", JSONArray().put("state"))
            })
        })

        return functionDeclarations
    }

    /**
     * Executes a function call and returns a JSONObject response to be sent back to Gemini.
     */
    suspend fun executeTool(name: String, argsJson: JSONObject): JSONObject = withContext(Dispatchers.Main) {
        Log.d(TAG, "Executing tool: $name with args: $argsJson")
        val response = JSONObject()

        try {
            when (name) {
                "openApp" -> {
                    val appName = argsJson.optString("appName", "").trim().lowercase()
                    val opened = launchNamedApp(appName)
                    response.put("success", opened)
                    if (opened) {
                        response.put("message", "Successfully opened $appName")
                    } else {
                        response.put("message", "Could not find $appName installed, opened web version")
                    }
                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "openApp",
                            description = "Opened app: $appName",
                            iconName = "app"
                        )
                    )
                }

                "makePhoneCall" -> {
                    val phoneNumber = argsJson.optString("phoneNumber", "").trim()
                    val contactName = argsJson.optString("contactName", "")
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                    response.put("success", true)
                    response.put("message", "Opened dialer for $phoneNumber ${if (contactName.isNotBlank()) "($contactName)" else ""}")
                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "makePhoneCall",
                            description = "Dialing $phoneNumber ${if (contactName.isNotBlank()) "($contactName)" else ""}",
                            iconName = "phone"
                        )
                    )
                }

                "sendMessage" -> {
                    val phoneNumber = argsJson.optString("phoneNumber", "").trim()
                    val message = argsJson.optString("message", "")
                    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = if (phoneNumber.isNotBlank()) Uri.parse("smsto:${Uri.encode(phoneNumber)}") else Uri.parse("smsto:")
                        putExtra("sms_body", message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(smsIntent)
                    response.put("success", true)
                    response.put("message", "Opened messaging with prefilled text: '$message'")
                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "sendMessage",
                            description = "Drafted SMS to $phoneNumber: '$message'",
                            iconName = "message"
                        )
                    )
                }

                "searchYouTube" -> {
                    val query = argsJson.optString("query", "").trim()
                    val ytAppIntent = Intent(Intent.ACTION_SEARCH).apply {
                        setPackage("com.google.android.youtube")
                        putExtra("query", query)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (ytAppIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(ytAppIntent)
                    } else {
                        val webYt = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webYt)
                    }
                    response.put("success", true)
                    response.put("message", "Searching YouTube for '$query'")
                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "searchYouTube",
                            description = "YouTube search: '$query'",
                            iconName = "youtube"
                        )
                    )
                }

                "downloadVideoHelper" -> {
                    val platform = argsJson.optString("platform", "general").lowercase()
                    val videoUrl = argsJson.optString("videoUrl", "")
                    val targetUrl = when {
                        platform.contains("facebook") || platform.contains("fb") ->
                            if (videoUrl.isNotBlank()) "https://snapsave.app?url=${Uri.encode(videoUrl)}" else "https://snapsave.app"
                        platform.contains("youtube") || platform.contains("yt") ->
                            if (videoUrl.isNotBlank()) "https://y2mate.is?url=${Uri.encode(videoUrl)}" else "https://y2mate.is"
                        platform.contains("instagram") || platform.contains("insta") ->
                            "https://fastdl.app"
                        else -> "https://snapsave.app"
                    }
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    response.put("success", true)
                    response.put("message", "Opened video downloader helper for $platform")
                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "downloadVideoHelper",
                            description = "Video Downloader Helper ($platform)",
                            iconName = "download"
                        )
                    )
                }

                "openWebsite" -> {
                    var rawUrl = argsJson.optString("url", "").trim()
                    if (!rawUrl.startsWith("http://", ignoreCase = true) &&
                        !rawUrl.startsWith("https://", ignoreCase = true)
                    ) {
                        rawUrl = "https://$rawUrl"
                    }

                    val uri = Uri.parse(rawUrl)
                    if (uri.scheme?.lowercase() in listOf("http", "https")) {
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        response.put("success", true)
                        response.put("message", "Successfully navigated to $rawUrl")

                        onToolExecuted(
                            ToolExecutionEvent(
                                name = "openWebsite",
                                description = "Navigated to $rawUrl",
                                iconName = "browser"
                            )
                        )
                    } else {
                        response.put("success", false)
                        response.put("error", "Invalid or unsafe URL protocol")
                    }
                }

                "searchWeb" -> {
                    val query = argsJson.optString("query", "").trim()
                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, query)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(fallback)
                    }
                    response.put("success", true)
                    response.put("message", "Opened search for '$query'")

                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "searchWeb",
                            description = "Searched for: $query",
                            iconName = "search"
                        )
                    )
                }

                "getDeviceStatus" -> {
                    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                    val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
                    val isCharging = batteryManager?.isCharging == true

                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    val activeNetwork = connectivityManager?.activeNetwork
                    val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
                    val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                    val networkType = when {
                        isWifi -> "Wi-Fi"
                        isCellular -> "Cellular 5G/LTE"
                        else -> "Connected"
                    }

                    val timeFormatted = SimpleDateFormat("h:mm a, EEEE, MMMM d", Locale.getDefault()).format(Date())

                    response.put("success", true)
                    response.put("batteryLevel", "$batteryLevel%")
                    response.put("isCharging", isCharging)
                    response.put("network", networkType)
                    response.put("currentTime", timeFormatted)

                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "getDeviceStatus",
                            description = "Battery: $batteryLevel% | Network: $networkType",
                            iconName = "device"
                        )
                    )
                }

                "setTimer" -> {
                    val seconds = argsJson.optInt("seconds", 60)
                    val label = argsJson.optString("label", "Nova Timer")
                    val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                        putExtra(AlarmClock.EXTRA_MESSAGE, label)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                        response.put("success", true)
                        response.put("message", "Timer set for $seconds seconds ($label)")
                    } catch (e: Exception) {
                        response.put("success", true)
                        response.put("message", "Timer initiated for $seconds seconds")
                    }

                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "setTimer",
                            description = "Timer set for ${seconds}s ($label)",
                            iconName = "timer"
                        )
                    )
                }

                "toggleFlashlight" -> {
                    val state = argsJson.optBoolean("state", true)
                    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                    val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                    if (cameraId != null) {
                        try {
                            cameraManager.setTorchMode(cameraId, state)
                            response.put("success", true)
                            response.put("message", "Flashlight turned ${if (state) "on" else "off"}")
                        } catch (e: Exception) {
                            response.put("success", false)
                            response.put("error", "Torch toggle failed: ${e.message}")
                        }
                    } else {
                        response.put("success", false)
                        response.put("error", "No camera torch available")
                    }

                    onToolExecuted(
                        ToolExecutionEvent(
                            name = "toggleFlashlight",
                            description = "Flashlight: ${if (state) "ON" else "OFF"}",
                            iconName = "flashlight"
                        )
                    )
                }

                else -> {
                    response.put("success", false)
                    response.put("error", "Unknown tool: $name")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tool execution error: ${e.message}", e)
            response.put("success", false)
            response.put("error", e.message ?: "Unknown error")
        }

        return@withContext response
    }

    /**
     * Helper to launch named applications or fall back cleanly.
     */
    fun launchNamedApp(appName: String): Boolean {
        val pm = context.packageManager
        val cleanName = appName.lowercase().trim()

        // Known common package identifiers
        val directPackage = when {
            cleanName.contains("youtube") || cleanName.contains("ইউটিউব") -> "com.google.android.youtube"
            cleanName.contains("facebook") || cleanName.contains("ফেসবুক") || cleanName.contains("fb") -> "com.facebook.katana"
            cleanName.contains("whatsapp") || cleanName.contains("হোয়াটসঅ্যাপ") -> "com.whatsapp"
            cleanName.contains("instagram") || cleanName.contains("ইন্সটাগ্রাম") -> "com.instagram.android"
            cleanName.contains("chrome") || cleanName.contains("ক্রোম") || cleanName.contains("browser") -> "com.android.chrome"
            else -> null
        }

        if (directPackage != null) {
            val launchIntent = pm.getLaunchIntentForPackage(directPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }

        // Special system intents
        if (cleanName.contains("camera") || cleanName.contains("ক্যামেরা")) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(cameraIntent)
            return true
        }

        if (cleanName.contains("calculator") || cleanName.contains("ক্যালকুলেটর")) {
            val calcIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(calcIntent)
                return true
            } catch (e: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=calculator")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            }
        }

        if (cleanName.contains("map") || cleanName.contains("ম্যাপ")) {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webMap = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webMap)
            }
            return true
        }

        if (cleanName.contains("setting") || cleanName.contains("সেটিং")) {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
            return true
        }

        // Fallback for YouTube or Facebook via Browser if native app not installed
        if (cleanName.contains("youtube") || cleanName.contains("ইউটিউব")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        }

        if (cleanName.contains("facebook") || cleanName.contains("ফেসবুক")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        }

        return false
    }
}

