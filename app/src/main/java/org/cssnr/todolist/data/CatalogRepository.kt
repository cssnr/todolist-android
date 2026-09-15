package org.cssnr.todolist.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

private const val ITEMS_ASSET = "items.json"

class CatalogRepository(
    private val context: Context,
    private val dao: CatalogDao,
) {

    private val json: Json = Json { ignoreUnknownKeys = true }
    private val mutateLock = Mutex()
    private var cachedFile: ItemsFile? = null

    fun observeCategories(): Flow<List<CategoryEntity>> = dao.observeCategories()

    fun autocomplete(prefix: String, limit: Int = 50): Flow<List<CatalogSuggestion>> =
        dao.autocomplete(prefix, limit)

    suspend fun ensureSeeded() = mutateLock.withLock {
        withContext(Dispatchers.IO) {
            val file = cachedItemsFile()
            val current = dao.dataVersion(CATALOG_DATA_VERSION) ?: 0L
            if (file.version.toLong() > current) {
                val categories = file.categories.mapIndexed { index, category ->
                    CategoryEntity(id = (index + 1).toLong(), name = category.name)
                }
                val items = file.categories.flatMapIndexed { index, category ->
                    category.items.map {
                        CatalogItemEntity(name = it, categoryId = (index + 1).toLong())
                    }
                }
                dao.reseed(categories, items, file.version.toLong())
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readItemsFile(): ItemsFile {
        val input = context.assets.open(ITEMS_ASSET)
        return input.use { json.decodeFromStream<ItemsFile>(it) }
    }

    private fun cachedItemsFile(): ItemsFile {
        cachedFile?.let { return it }
        return readItemsFile().also { cachedFile = it }
    }
}