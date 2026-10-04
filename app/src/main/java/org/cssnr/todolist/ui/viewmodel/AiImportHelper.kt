package org.cssnr.todolist.ui.viewmodel

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
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
    private val json = Json { ignoreUnknownKeys = true }

    fun statusName(status: Int): String = when (status) {
        FeatureStatus.AVAILABLE -> "AVAILABLE"
        FeatureStatus.DOWNLOADABLE -> "DOWNLOADABLE"
        FeatureStatus.DOWNLOADING -> "DOWNLOADING"
        FeatureStatus.UNAVAILABLE -> "UNAVAILABLE"
        else -> "UNKNOWN($status)"
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
            android.util.Log.w(TAG, "checkStatus failed", e)
            null
        }
    }

    suspend fun isAvailable(): Boolean {
        return try {
            withTimeoutOrNull(15_000.milliseconds) {
                val model = Generation.getClient()
                try {
                    val status = model.checkStatus()
                    android.util.Log.d(TAG, "isAvailable checkStatus=${statusName(status)}")
                    status != FeatureStatus.UNAVAILABLE
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
            android.util.Log.w(TAG, "isAvailable failed", e)
            false
        }
    }

    suspend fun ensureDownloaded(
        onStarted: () -> Unit = {},
        onProgress: (downloaded: Long) -> Unit = {},
    ): Boolean {
        return try {
            withTimeoutOrNull(120_000.milliseconds) {
                val model = Generation.getClient()
                try {
                    val status = model.checkStatus()
                    android.util.Log.d(TAG, "checkStatus=${statusName(status)}")
                    when (status) {
                        FeatureStatus.AVAILABLE -> true
                        FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                            onStarted()
                            val terminal = model.download()
                                .onEach { dl ->
                                    android.util.Log.d(TAG, "download event=$dl")
                                    if (dl is DownloadStatus.DownloadProgress) {
                                        onProgress(dl.totalBytesDownloaded)
                                    }
                                }
                                .first { dl ->
                                    dl is DownloadStatus.DownloadCompleted ||
                                            dl is DownloadStatus.DownloadFailed
                                }
                            if (terminal is DownloadStatus.DownloadFailed) {
                                android.util.Log.w(TAG, "download failed: $terminal")
                            }
                            terminal is DownloadStatus.DownloadCompleted
                        }

                        else -> {
                            android.util.Log.w(TAG, "AICore status ${statusName(status)}, no model")
                            false
                        }
                    }
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
            android.util.Log.w(TAG, "ensureDownloaded failed", e)
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
            User text: "$input"
            Return ONLY a JSON array, no markdown, no explanation.
            Each element: {"name": "<item name>", "category": "<category>" or null}.
            Rules: trim names, one item per element, drop empty items, max 100 items.
            Format each item name and any new category in Title Case (e.g. Green Peppers, Dish Soap).
            Reuse existing categories with their exact spelling.
        """.trimIndent()
    }

    suspend fun parseWithAi(
        input: String,
        existingCategories: List<String>
    ): List<Pair<String, String?>> {
        val result = withTimeoutOrNull(120_000.milliseconds) {
            val model = Generation.getClient()
            try {
                android.util.Log.d(TAG, "generateContent started, inputChars=${input.length}")
                val response = model.generateContent(buildPrompt(input, existingCategories))
                val text = response.candidates.firstOrNull()?.text.orEmpty()
                android.util.Log.d(TAG, "generateContent done, outputChars=${text.length}")
                decodeItems(text)
            } finally {
                try {
                    model.close()
                } catch (_: Exception) {
                }
            }
        } ?: throw IllegalStateException("AI timed out")
        return result.map { it.name.trim() to it.category?.trim()?.takeIf { c -> c.isNotEmpty() } }
            .filter { it.first.isNotEmpty() }
    }

    fun decodeItems(raw: String): List<AiParsedItem> {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        return json.decodeFromString(cleaned)
    }
}
