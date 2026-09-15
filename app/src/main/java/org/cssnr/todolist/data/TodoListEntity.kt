package org.cssnr.todolist.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val hideCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)