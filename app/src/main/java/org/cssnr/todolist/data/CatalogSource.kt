package org.cssnr.todolist.data

import kotlinx.serialization.Serializable

@Serializable
data class ItemsFile(
    val version: Int,
    val categories: List<CategorySource> = emptyList(),
)

@Serializable
data class CategorySource(
    val name: String,
    val items: List<String> = emptyList(),
)