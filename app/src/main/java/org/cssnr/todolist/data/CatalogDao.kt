package org.cssnr.todolist.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

internal const val CATALOG_DATA_VERSION = "catalogDataVersion"

@Dao
interface CatalogDao {

    @Query("SELECT * FROM categories ORDER BY id")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT ci.id AS id, ci.name AS name, c.name AS categoryName
        FROM catalog_items ci
        JOIN categories c ON c.id = ci.categoryId
        WHERE ci.name LIKE :prefix || '%'
        ORDER BY c.id, ci.id
        LIMIT :limit
        """,
    )
    fun autocomplete(prefix: String, limit: Int = 50): Flow<List<CatalogSuggestion>>

    @Query("SELECT dataVersion FROM catalog_meta WHERE metaKey = :key")
    suspend fun dataVersion(key: String): Long?

    @Transaction
    suspend fun reseed(
        categories: List<CategoryEntity>,
        items: List<CatalogItemEntity>,
        dataVersion: Long,
    ) {
        deleteItems()
        deleteCategories()
        insertCategories(categories)
        insertItems(items)
        setDataVersion(CatalogMetaEntity(CATALOG_DATA_VERSION, dataVersion))
    }

    @Insert
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert
    suspend fun insertItems(items: List<CatalogItemEntity>)

    @Query("DELETE FROM catalog_items")
    suspend fun deleteItems()

    @Query("DELETE FROM categories")
    suspend fun deleteCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setDataVersion(meta: CatalogMetaEntity)
}