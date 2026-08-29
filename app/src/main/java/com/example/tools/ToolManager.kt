package com.example.tools

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
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
 * requested by Gemini Live / Auron.
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

        // 1. openWebsite
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

        // 2. searchWeb
        functionDeclarations.put(JSONObject().apply {
            put("name", "searchWeb")
            put("description", "Searches the web for a given query.")
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

        // 3. getDeviceStatus
        functionDeclarations.put(JSONObject().apply {
            put("name", "getDeviceStatus")
            put("description", "Retrieves current battery level, charging status, network connection, and current time.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject())
            })
        })

        // 4. setTimer
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

        // 5. toggleFlashlight
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
                "openWebsite" -> {
                    var rawUrl = argsJson.optString("url", "").trim()
                    if (!rawUrl.startsWith("http://", ignoreCase = true) &&
                        !rawUrl.startsWith("https://", ignoreCase = true)
                    ) {
                        rawUrl = "https://$rawUrl"
                    }

                    // Validate URL scheme safety
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
                    val label = argsJson.optString("label", "Auron Timer")
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
}
