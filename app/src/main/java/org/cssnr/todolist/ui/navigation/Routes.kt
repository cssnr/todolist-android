package org.cssnr.todolist.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ListsSection

@Serializable
data object Lists

@Serializable
data object Settings

@Serializable
data class ListDetail(val listId: Long)