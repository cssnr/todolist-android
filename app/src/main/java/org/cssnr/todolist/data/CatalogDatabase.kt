package org.cssnr.todolist.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [CategoryEntity::class, CatalogItemEntity::class, CatalogMetaEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CatalogDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao

    companion object {
        @Volatile
        private var instance: CatalogDatabase? = null

        fun get(context: Context): CatalogDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CatalogDatabase::class.java,
                    "items.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}