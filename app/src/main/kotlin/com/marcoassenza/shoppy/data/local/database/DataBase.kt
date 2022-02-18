package com.marcoassenza.shoppy.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item

@Database(entities = [Item::class, Category::class], version = 1, exportSchema = false)
abstract class ItemDatabase : RoomDatabase() {

    abstract fun getItemDao(): ItemDao
}