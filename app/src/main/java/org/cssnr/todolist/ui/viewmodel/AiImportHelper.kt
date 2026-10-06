package org.cssnr.todolist.ui.viewmodel

import android.util.Log
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class AiParsedItem(
    val name: String,
    val category: String? = null,
)

object AiImportHelper {

    private const val TAG = "AiImport"
    private const val MAX_ITEMS = 100
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedAvailable: Boolean = false

    fun statusName(status: Int): String = when (status) {
        FeatureStatus.AVAILABLE -> "AVAILABLE"
        FeatureStatus.DOWNLOADABLE -> "DOWNLOADABLE"
        FeatureStatus.DOWNLOADING -> "DOWNLOADING"
        FeatureStatus.UNAVAILABLE -> "UNAVAILABLE"
        else -> "UNKNOWN($status)"
    }

    fun describeError(e: Throwable): String {
        val message = e.message?.takeIf { it.isNotBlank() } ?: e::class.java.simpleName
        val code = (e as? GenAiException)?.errorCode ?: return message
        return "$message [AICore error $code]"
    }

    suspend fun checkStatusCode(): Int? {
        return try {
            withTimeoutOrNull(15_000.milliseconds) {
                val model = Generation.getClient()
                try {
                    model.checkStatus()
                } finally {
                    try {
                        model.close()
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "checkStatus failed: ${describeError(e)}", e)
            throw e
        }
    }

    suspend fun isAvailable(): Boolean {
        if (cachedAvailable) return true
        return try {
            withTimeoutOrNull(15_000.milliseconds) {
                val model = Generation.getClient()
                try {
                    val status = model.checkStatus()
                    Log.d(TAG, "isAvailable checkStatus=${statusName(status)}")
                    val available = status != FeatureStatus.UNAVAILABLE
                    if (available) {
                        cachedAvailable = true
                    }
                    available
                } finally {
                    try {
                        model.close()
                    } catch (_: Exception) {
                    }
                }
            } ?: false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "isAvailable failed: ${describeError(e)}", e)
            false
        }
    }

    suspend fun ensureDownloaded(
        initialStatus: Int? = null,
        onStarted: () -> Unit = {},
        onProgress: (downloaded: Long) -> Unit = {},
        onFailed: (message: String) -> Unit = {},
    ): Boolean {
        return try {
            val model = Generation.getClient()
            try {
                val status = initialStatus ?: model.checkStatus()
                Log.d(TAG, "checkStatus=${statusName(status)}")
                when (status) {
                    FeatureStatus.AVAILABLE -> true
                    FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                        onStarted()
                        val terminal = model.download()
                            .onEach { dl ->
                                Log.d(TAG, "download event=$dl")
                                if (dl is DownloadStatus.DownloadProgress) {
                                    onProgress(dl.totalBytesDownloaded)
                                }
                            }
                            .first { dl ->
                                dl is DownloadStatus.DownloadCompleted ||
                                        dl is DownloadStatus.DownloadFailed
                            }
                        if (terminal is DownloadStatus.DownloadFailed) {
                            val message =
                                "Model download failed: ${describeError(terminal.e)}. " +
                                        "Check your connection and retry."
                            Log.w(TAG, message)
                            onFailed(message)
                        }
                        terminal is DownloadStatus.DownloadCompleted
                    }

                    else -> {
                        val message =
                            "AICore status ${statusName(status)} - no model available. " +
                                    "Check that Google AICore is installed and updated."
                        Log.w(TAG, message)
                        onFailed(message)
                        false
                    }
                }
            } finally {
                try {
                    model.close()
                } catch (_: Exception) {
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "ensureDownloaded failed: ${describeError(e)}", e)
            onFailed("AICore error: ${describeError(e)}")
            false
        }
    }

    fun buildPrompt(input: String, existingCategories: List<String>): String {
        val categories = if (existingCategories.isEmpty()) {
            "none yet"
        } else {
            existingCategories.sorted().joinToString(", ")
        }
        return """
            You parse a shopping/todo list from plain language into items with categories.
            Existing categories (reuse when matching, case-insensitive): $categories.
            You may invent a new short category only when nothing matches.
            User text, delimited by <text> and </text>:
            <text>
            $input
            </text>
            Return ONLY a JSON array, no markdown, no explanation.
            Each element: {"name": "<item name>", "category": "<category>" or null}.
            Rules: trim names, one item per element, drop empty items, max $MAX_ITEMS items.
            Format each item name and any new category in Title Case (e.g. Green Peppers, Dish Soap).
            Reuse existing categories with their exact spelling.
        """.trimIndent()
    }

    suspend fun parseWithAi(
        input: String,
        existingCategories: List<String>
    ): List<Pair<String, String?>> {
        val result = run {
            val model = Generation.getClient()
            try {
                Log.d(TAG, "generateContent started, inputChars=${input.length}")
                val response = model.generateContent(buildPrompt(input, existingCategories))
                val text = response.candidates.firstOrNull()?.text.orEmpty()
                Log.d(TAG, "generateContent done, outputChars=${text.length}")
                decodeItems(text)
            } finally {
                try {
                    model.close()
                } catch (_: Exception) {
                }
            }
        }
        return result.map { it.name.trim() to it.category?.trim()?.takeIf { c -> c.isNotEmpty() } }
            .filter { it.first.isNotEmpty() }
    }

    fun decodeItems(raw: String): List<AiParsedItem> {
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']')
        if (start !in 0..end) {
            throw IllegalStateException("No JSON array found in AI response")
        }
        val items = json.decodeFromString<List<AiParsedItem>>(raw.substring(start, end + 1))
        return items.take(MAX_ITEMS)
    }
}
