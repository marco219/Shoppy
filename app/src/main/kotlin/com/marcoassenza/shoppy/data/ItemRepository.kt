package com.marcoassenza.shoppy.data

import com.marcoassenza.shoppy.data.local.ItemDao
import com.marcoassenza.shoppy.helpers.DataWithStatus
import com.marcoassenza.shoppy.models.Item
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ItemRepository @Inject constructor(
    private val itemDao: ItemDao) {

    suspend fun insertItems(items: List<Item>){
        itemDao.insertItems(items)
    }

    suspend fun insertItem(item: Item){
        itemDao.insert(item)
    }

    fun getItems(): DataWithStatus<Flow<List<Item>>> {
        return DataWithStatus.success(null, itemDao.getAllItem())
    }

    fun getCategories(): DataWithStatus<List<String>> {
        return DataWithStatus.success(null, listOf(
            "Fruits",
            "Légumes",
            "Epices",
            "Boissons",
            "Condiments",
            "Autres"
        ))}
}