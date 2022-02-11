package com.marcoassenza.shoppy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassenza.shoppy.data.ItemRepository
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val itemRepository: ItemRepository
): ViewModel() {

    private val  _itemList = MutableStateFlow<List<Item>>(emptyList())
    val itemList: StateFlow<List<Item>>
        get() = _itemList

    val categoryList: StateFlow<List<Category>?> = flow {
        emit(itemRepository.getCategories().data)
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000), // Or Lazily because it's a one-shot
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch{
            itemRepository.getItems().data?.collect{
                _itemList.value = it
            }
        }
    }

    suspend fun insertNewItem(item: Item) {
        viewModelScope.launch {
            itemRepository.insertItem(item)
        }
    }

    suspend fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemRepository.deleteItem(item)
        }
    }


}