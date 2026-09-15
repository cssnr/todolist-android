package org.cssnr.todolist.data

import java.io.File
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun decodeSampleItemsFile() {
        val file = File("src/main/assets/items.json")
        assertTrue("items.json not found", file.exists())

        val itemsFile = json.decodeFromStream<ItemsFile>(file.inputStream())

        assertTrue("version must be positive", itemsFile.version > 0)
        assertTrue("must contain categories", itemsFile.categories.isNotEmpty())
        assertTrue("category names must not be blank", itemsFile.categories.all { it.name.isNotBlank() })
        assertTrue("categories must contain items", itemsFile.categories.all { it.items.isNotEmpty() })

        val allItems = itemsFile.categories.flatMap { it.items }
        assertTrue("must contain items", allItems.isNotEmpty())
        assertTrue("item names must not be blank", allItems.all { it.isNotBlank() })
        assertEquals("item names must be unique", allItems.size, allItems.distinct().size)
    }
}