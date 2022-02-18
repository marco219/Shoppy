package com.marcoassenza.shoppy.data

import android.content.Context
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.data.local.database.ItemDao
import com.marcoassenza.shoppy.data.local.remote.FirebaseDatabaseDao
import com.marcoassenza.shoppy.data.local.remote.RemoteDatabaseStatus
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.models.areIdEqual
import com.marcoassenza.shoppy.models.isInStorage
import com.marcoassenza.shoppy.utils.Constant
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ItemsRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val itemDao: ItemDao,
    private val firebaseDatabaseDao: FirebaseDatabaseDao
) {

    suspend fun insertItem(item: Item) {
        itemDao.insert(item)
        updateRemote()
    }

    private suspend fun updateRemote() {
        firebaseDatabaseDao.updateRemoteItems(itemDao.getAllItem())
    }

    suspend fun updateStockQuantity(item: Item, quantity: Int) {
        if (item.stockQuantity == Constant.MIN_ITEM_IN_STORAGE && quantity < 0) return
        if (item.stockQuantity == Constant.MAX_ITEM_IN_STORAGE && quantity > 0) return
        val newStockQuantity = quantity + item.stockQuantity
        itemDao.updateStockQuantity(newStockQuantity, item.id)
        updateRemote()
    }

    suspend fun resetStockQuantityAndMoveToGroceryList(item: Item) {
        itemDao.updateStockQuantity(0, item.id)
        itemDao.updateIsInGroceryList(true, item.id)
        updateRemote()
    }

    suspend fun updateStockQuantityAndMoveToStorage(item: Item, quantityToAdd: Int) {
        val newStockQuantity = quantityToAdd + item.stockQuantity
        itemDao.updateStockQuantity(newStockQuantity, item.id)
        itemDao.updateIsInGroceryList(false, item.id)
        updateRemote()
    }

    suspend fun deleteItem(item: Item) {
        itemDao.delete(item)
        updateRemote()
    }

    suspend fun resetLocalDb() {
        itemDao.deleteAll()
    }

    suspend fun deleteItemWithNameAndCategory(item: Item) {
        itemDao.deleteItemWithNameAndCategory(item.name, item.category.categoryId)
        updateRemote()
    }

    private suspend fun getAllItemFlow(): Flow<Result<List<Item>>> = flow {
        itemDao.getAllItemAsFlow().collect { items ->
            emit(Result.success(items))
        }
    }

    suspend fun getGroceryItems(): Flow<List<Item>> = flow {
        getAllItemFlow().collect { result ->
            emit(result.getOrElse { emptyList() }
                .filter { it.isInGroceryList })
        }
    }

    suspend fun getGroceryListCategories(): Flow<List<Category>> = flow {
        getAllItemFlow().collect { result ->
            emit(result.getOrElse { emptyList() }
                .filter { it.isInGroceryList }
                .map { it.category }
                .mapToDefault()
            )
        }
    }

    suspend fun getStorageItems(): Flow<List<Item>> = flow {
        getAllItemFlow().collect { result ->
            emit(result.getOrElse { emptyList() }
                .filter { it.isInStorage() })
        }
    }

    suspend fun getStorageCategories(): Flow<List<Category>> = flow {
        getAllItemFlow().collect { result ->
            emit(result.getOrElse { emptyList() }
                .filter { it.isInStorage() }
                .map { it.category }
                .mapToDefault()
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

    private fun List<Category>.mapToDefault(): List<Category> {
        return map { category ->
            getDefaultCategories().first { defaultCategory ->
                defaultCategory.categoryId == category.categoryId
            }
        }.distinct()
    }

    fun updateUser(userName: String) {
        firebaseDatabaseDao.userName = userName
    }

    val remoteDataListener = flow {
        firebaseDatabaseDao.remoteDataListenerCallBackFlow.onEach { result ->
            when {
                result.isSuccess -> {
                    val fetchedItems = result.getOrThrow().map { item ->
                        val category = getDefaultCategories().first { defaultCategory ->
                            item.category.areIdEqual(defaultCategory)
                        }
                        item.category = category
                        item
                    }
                    val itemsToDelete = itemDao.getAllItem().filterNot { localItem ->
                        fetchedItems.any { localItem.areIdEqual(it) }
                    }
                    itemDao.deleteItems(itemsToDelete)
                    itemDao.insertItems(fetchedItems)
                    emit(RemoteDatabaseStatus.Available)
                }
                result.isFailure -> {
                    emit(RemoteDatabaseStatus.Failure)
                    //TODO: handle failure exceptions
                }
            }
        }
            .catch { cause ->
                Timber.e(cause.localizedMessage)
                emit(RemoteDatabaseStatus.Failure)
            }
            .collect()
    }
}