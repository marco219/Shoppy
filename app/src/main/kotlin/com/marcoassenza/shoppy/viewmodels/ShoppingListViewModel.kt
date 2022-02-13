package com.marcoassenza.shoppy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassenza.shoppy.data.ItemRepository
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    val itemList: StateFlow<List<Item>> = flow {
        itemRepository.getItems().data?.let { emitAll(it) }
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val categoryList: StateFlow<List<Category>?> = flow {
        emit(itemRepository.getCategories().data)
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    fun insertNewItem(item: Item) {
        viewModelScope.launch {
            itemRepository.insertItem(item)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemRepository.deleteItem(item)
        }
    }

    fun undoItem(item: Item) {
        viewModelScope.launch {
            itemRepository.deleteItemWithNameAndCategory(item)
        }
    }
}