package org.cssnr.todolist.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoListDao {
    @Query("SELECT * FROM todo_lists ORDER BY createdAt")
    fun observeAll(): Flow<List<TodoListEntity>>

    @Query("SELECT * FROM todo_lists WHERE id = :id")
    fun observeById(id: Long): Flow<TodoListEntity?>

    @Query("SELECT * FROM todo_items WHERE listId = :listId ORDER BY createdAt")
    fun observeItems(listId: Long): Flow<List<TodoItemEntity>>

    @Query("SELECT * FROM todo_items WHERE listId = :listId AND LOWER(text) = LOWER(:text) LIMIT 1")
    suspend fun findItem(listId: Long, text: String): TodoItemEntity?

    @Query("SELECT DISTINCT category FROM todo_items WHERE listId = :listId AND category IS NOT NULL")
    suspend fun listCategories(listId: Long): List<String>

    @Insert
    suspend fun insert(list: TodoListEntity): Long

    @Delete
    suspend fun delete(list: TodoListEntity)

    @Insert
    suspend fun insertItem(item: TodoItemEntity)

    @Query("UPDATE todo_items SET done = :done WHERE id = :id")
    suspend fun setItemDone(id: Long, done: Boolean)

    @Query("UPDATE todo_items SET text = :text WHERE id = :id")
    suspend fun updateItemText(id: Long, text: String)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("UPDATE todo_items SET done = 0, category = COALESCE(:category, category) WHERE id = :id")
    suspend fun resurrectItem(id: Long, category: String?)

    @Query("UPDATE todo_lists SET hideCompleted = :hideCompleted WHERE id = :id")
    suspend fun setHideCompleted(id: Long, hideCompleted: Boolean)

    @Query("UPDATE todo_items SET done = :done WHERE listId = :listId")
    suspend fun setAllItemsDone(listId: Long, done: Boolean)
}