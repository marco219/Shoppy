package com.marcoassenza.shoppy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marcoassenza.shoppy.models.Item

@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class ItemDatabase : RoomDatabase() {

    abstract fun getItemDao(): ItemDao
}