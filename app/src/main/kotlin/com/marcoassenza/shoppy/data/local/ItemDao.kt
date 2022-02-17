package com.marcoassenza.shoppy.data.local

import androidx.room.*
import com.marcoassenza.shoppy.models.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * from item")
    fun getAllItemAsFlow(): Flow<List<Item>>

    @Query("SELECT * from item")
    suspend fun getAllItem(): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Query("UPDATE item SET stockQuantity=:newStockQuantity WHERE id = :itemId")
    suspend fun updateStockQuantity(newStockQuantity: Int?, itemId: Long)

    @Query("UPDATE item SET isInGroceryList=:isInGroceryLit WHERE id = :itemId")
    suspend fun updateIsInGroceryList(isInGroceryLit: Boolean, itemId: Long)

    @Delete
    suspend fun delete(item: Item)

    @Delete
    suspend fun deleteItems(items: List<Item>)

    @Query("DELETE FROM item WHERE name = :name AND categoryName = :categoryId ")
    suspend fun deleteItemWithNameAndCategory(name: String, categoryId: Int)

    @Query("DELETE FROM item")
    suspend fun deleteAll()

}