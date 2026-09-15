package org.cssnr.todolist.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [TodoListEntity::class, TodoItemEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class TodoListDatabase : RoomDatabase() {
    abstract fun todoListDao(): TodoListDao

    companion object {

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE todo_items ADD COLUMN category TEXT")
            }
        }

        @Volatile
        private var instance: TodoListDatabase? = null

        fun get(context: Context): TodoListDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TodoListDatabase::class.java,
                    "todolist.db",
                ).addMigrations(MIGRATION_3_4).build().also { instance = it }
            }
    }
}