package com.marcoassenza.shoppy.data.local

import androidx.room.*
import com.marcoassenza.shoppy.models.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * from item")
    fun getAllItem(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Delete
    suspend fun delete(item: Item)

    @Query("DELETE FROM item WHERE name = :name AND categoryId = :categoryId ")
    suspend fun deleteItemWithNameAndCategory(name: String, categoryId: Int)

    @Query("DELETE FROM item")
    suspend fun deleteAll()

}