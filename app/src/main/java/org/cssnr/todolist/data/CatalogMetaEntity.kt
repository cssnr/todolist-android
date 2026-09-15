package org.cssnr.todolist.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "catalog_meta")
data class CatalogMetaEntity(
    @PrimaryKey val metaKey: String,
    val dataVersion: Long,
)