package com.marcoassenza.shoppy.data

import android.content.Context
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.data.local.ItemDao
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.utils.Constant
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ItemRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val itemDao: ItemDao
) {

    suspend fun insertItems(items: List<Item>) = itemDao.insertItems(items)

    suspend fun insertItem(item: Item) = itemDao.insert(item)

    suspend fun updateStockQuantity(item: Item, quantity: Int) {
        if (item.stockQuantity == Constant.MIN_ITEM_IN_STORAGE && quantity < 0) return
        if (item.stockQuantity == Constant.MAX_ITEM_IN_STORAGE && quantity > 0) return
        val newStockQuantity = quantity + item.stockQuantity
        itemDao.updateStockQuantity(newStockQuantity, item.id)
    }

    suspend fun resetStockQuantityAndMoveToGroceryList(item: Item) {
        itemDao.updateStockQuantity(0, item.id)
        itemDao.updateIsInGroceryList(true, item.id)
    }

    suspend fun updateStockQuantityAndMoveToStorage(item: Item, quantityToAdd: Int) {
        val newStockQuantity = quantityToAdd + item.stockQuantity
        itemDao.updateStockQuantity(newStockQuantity, item.id)
        itemDao.updateIsInGroceryList(false, item.id)
    }

    suspend fun deleteItem(item: Item) = itemDao.delete(item)

    suspend fun deleteItemWithNameAndCategory(item: Item) =
        itemDao.deleteItemWithNameAndCategory(item.name, item.category.categoryId)

    suspend fun getGroceryItems(): Flow<List<Item>> = flow {
        itemDao.getAllItem().collect { itemList ->
            emit(itemList.filter { it.isInGroceryList })
        }
    }

    suspend fun getGroceryListCategories(): Flow<List<Category>> = flow {
        itemDao.getAllItem().collect { itemList ->
            emit(itemList
                .filter { it.isInGroceryList }
                .map { it.category }.distinct()
            )
        }
    }

    private fun Item.isInStorage(): Boolean = !isInGroceryList && (stockQuantity > 0)

    suspend fun getStorageItems(): Flow<List<Item>> = flow {
        itemDao.getAllItem().collect { itemList ->
            emit(itemList.filter { it.isInStorage() })
        }
    }

    suspend fun getStorageCategories(): Flow<List<Category>> = flow {
        itemDao.getAllItem().collect { itemList ->
            emit(itemList
                .filter { it.isInStorage() }
                .map { it.category }.distinct()
            )
        }
    }

    fun getDefaultCategories(): List<Category> {
        val categoryIdList = applicationContext.resources.getIntArray(R.array.category_id_array)
            .toList()
        val categoryNameList =
            applicationContext.resources.getStringArray(R.array.category_name_array)
                .toList()
        val categoryColorList =
            applicationContext.resources.getIntArray(R.array.category_color_array)
                .toList()
        val categoryList = categoryIdList.mapIndexed { i, id ->
            val name = categoryNameList[i]
            val colorInt = categoryColorList[i]

            Category(id, name, colorInt)
        }

        return categoryList
    }
}