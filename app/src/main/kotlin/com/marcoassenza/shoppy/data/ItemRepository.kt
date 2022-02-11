package com.marcoassenza.shoppy.data

import android.content.Context
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.data.local.ItemDao
import com.marcoassenza.shoppy.helpers.DataWithStatus
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ItemRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val itemDao: ItemDao) {

    suspend fun insertItems(items: List<Item>) {
        itemDao.insertItems(items)
    }

    suspend fun insertItem(item: Item) {
        itemDao.insert(item)
    }

    suspend fun deleteItem(item: Item) {
        itemDao.delete(item)
    }

    fun getItems(): DataWithStatus<Flow<List<Item>>> {
        return DataWithStatus.success(null, itemDao.getAllItem())
    }

    fun getCategories(): DataWithStatus<List<Category>> {
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

        return DataWithStatus.success(null, categoryList)
    }
}