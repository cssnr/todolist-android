package org.cssnr.todolist.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val digitRuns = Regex("\\d+|\\D+")

private val naturalComparator: Comparator<String> = Comparator { a, b ->
    val aParts = digitRuns.findAll(a).map { it.value }.toList()
    val bParts = digitRuns.findAll(b).map { it.value }.toList()
    val size = minOf(aParts.size, bParts.size)
    for (i in 0 until size) {
        val x = aParts[i]
        val y = bParts[i]
        if (x == y) continue
        val xNum = x.toLongOrNull()
        val yNum = y.toLongOrNull()
        if (xNum != null && yNum != null) {
            val cmp = xNum.compareTo(yNum)
            if (cmp != 0) return@Comparator cmp
        } else {
            val cmp = x.compareTo(y, ignoreCase = true)
            if (cmp != 0) return@Comparator cmp
        }
    }
    aParts.size.compareTo(bParts.size)
}

class TodoListRepository(private val dao: TodoListDao) {

    fun observeLists(): Flow<List<TodoListEntity>> = dao.observeAll()
        .map { lists -> lists.sortedWith(compareBy(naturalComparator, TodoListEntity::name)) }

    fun observeList(id: Long): Flow<TodoListEntity?> = dao.observeById(id)

    fun observeItems(listId: Long): Flow<List<TodoItemEntity>> = dao.observeItems(listId)
        .map { items ->
            items.sortedWith { a, b ->
                val aCategorized = a.category != null
                val bCategorized = b.category != null
                if (aCategorized != bCategorized) {
                    return@sortedWith if (aCategorized) -1 else 1
                }
                val categoryCmp = naturalComparator.compare(a.category ?: "", b.category ?: "")
                if (categoryCmp != 0) return@sortedWith categoryCmp
                naturalComparator.compare(a.text, b.text)
            }
        }

    suspend fun addList(name: String) {
        dao.insert(TodoListEntity(name = name))
    }

    suspend fun addItem(listId: Long, text: String, category: String? = null) {
        val existing = dao.findItem(listId, text)
        if (existing == null) {
            dao.insertItem(TodoItemEntity(listId = listId, text = text, category = category))
        } else if (existing.done) {
            dao.resurrectItem(existing.id, category)
        }
    }

    suspend fun toggleItem(item: TodoItemEntity) {
        dao.setItemDone(item.id, !item.done)
    }

    suspend fun updateItem(item: TodoItemEntity, newText: String) {
        val text = newText.trim()
        if (text.isNotEmpty() && text != item.text) {
            dao.updateItemText(item.id, text)
        }
    }

    suspend fun deleteItem(item: TodoItemEntity) {
        dao.deleteItem(item.id)
    }

    suspend fun deleteList(list: TodoListEntity) {
        dao.delete(list)
    }

    suspend fun renameList(list: TodoListEntity, newName: String) {
        val name = newName.trim()
        if (name.isNotEmpty() && name != list.name) {
            dao.renameList(list.id, name)
        }
    }

    suspend fun listCategories(listId: Long): List<String> = dao.listCategories(listId)

    suspend fun setHideCompleted(listId: Long, hideCompleted: Boolean) {
        dao.setHideCompleted(listId, hideCompleted)
    }

    suspend fun crossAllItems(listId: Long) {
        dao.setAllItemsDone(listId, done = true)
    }

    suspend fun uncrossAllItems(listId: Long) {
        dao.setAllItemsDone(listId, done = false)
    }
}