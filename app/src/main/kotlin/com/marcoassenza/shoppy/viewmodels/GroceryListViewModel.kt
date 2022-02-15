package com.marcoassenza.shoppy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassenza.shoppy.data.ItemRepository
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroceryListViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    val groceryList: StateFlow<List<Item>> = flow {
        emitAll(itemRepository.getGroceryItems())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val categoryList: StateFlow<List<Category>> = flow {
        emitAll(itemRepository.getGroceryListCategories())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val defaultCategoryList: StateFlow<List<Category>?> = flow {
        emit(itemRepository.getDefaultCategories())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val storageList: StateFlow<List<Item>> = flow {
        emitAll(itemRepository.getStorageItems())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val storageCategoryList: StateFlow<List<Category>> = flow {
        emitAll(itemRepository.getStorageCategories())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    private val _movedItem = MutableSharedFlow<Item?>(0)
    val movedItem: SharedFlow<Item?> = _movedItem.asSharedFlow()

    private val _addedItem = MutableSharedFlow<Item?>(0)
    val addedItem: SharedFlow<Item?> = _addedItem.asSharedFlow()

    private var _toBeTreatedItem = MutableStateFlow<Item?>(null)
    val toBeTreatedItem: StateFlow<Item?> = _toBeTreatedItem.asStateFlow()

    fun setToBeTreatedItem(item: Item) {
        viewModelScope.launch {
            _toBeTreatedItem.emit(item)
        }
    }

    fun insertNewItem(item: Item) {
        viewModelScope.launch {
            itemRepository.insertItem(item)
            _addedItem.emit(item)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemRepository.deleteItem(item)
        }
    }

    fun undoAddItem(item: Item) {
        viewModelScope.launch {
            itemRepository.deleteItemWithNameAndCategory(item)
        }
    }

    fun undoMoveItem(item: Item) {
        viewModelScope.launch {
            itemRepository.resetStockQuantityAndMoveToGroceryList(item)
        }
    }

    fun undoDeleteItem(item: Item) {
        viewModelScope.launch {
            itemRepository.insertItem(item)
        }
    }

    fun updateStockQuantityAndMoveToStorage(item: Item, quantity: Int) {
        viewModelScope.launch {
            itemRepository.updateStockQuantityAndMoveToStorage(item, quantity)
            _movedItem.emit(item)
        }
    }

    fun updateStockQuantity(item: Item, quantity: Int) {
        viewModelScope.launch {
            itemRepository.updateStockQuantity(item, quantity)
        }
    }

    fun resetStockQuantityAndMoveToGroceryList(item: Item) {
        viewModelScope.launch {
            itemRepository.resetStockQuantityAndMoveToGroceryList(item)
        }
    }
}