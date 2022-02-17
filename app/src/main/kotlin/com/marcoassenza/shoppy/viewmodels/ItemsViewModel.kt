package com.marcoassenza.shoppy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassenza.shoppy.data.ItemsRepository
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.utils.NetworkStatus
import com.marcoassenza.shoppy.utils.NetworkStatusTracker
import com.marcoassenza.shoppy.utils.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val networkStatusTracker: NetworkStatusTracker,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    init {
        fetchRemoteDataAndUpdateLocal()
    }

    val networkStatus: SharedFlow<NetworkStatus?> = flow {
        networkStatusTracker.networkStatus.map(
            onLost = {
                NetworkStatus.Unavailable
            },
            onAvailable = {
                fetchRemoteDataAndUpdateLocal()
                NetworkStatus.Available
            },
            onUnavailable = {
                NetworkStatus.Unavailable
            }
        ).collect {
            emit(it)
        }
    }.shareIn(
        scope = viewModelScope,
        started = Lazily,
        replay = 0
    )

    val groceryList: StateFlow<List<Item>> = flow {
        emitAll(itemsRepository.getGroceryItems())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val categoryList: StateFlow<List<Category>> = flow {
        emitAll(itemsRepository.getGroceryListCategories())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val defaultCategoryList: StateFlow<List<Category>?> = flow {
        emit(itemsRepository.getDefaultCategories())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val storageList: StateFlow<List<Item>> = flow {
        emitAll(itemsRepository.getStorageItems())
    }.stateIn(
        scope = viewModelScope,
        started = Lazily,
        initialValue = emptyList()
    )

    val storageCategoryList: StateFlow<List<Category>> = flow {
        emitAll(itemsRepository.getStorageCategories())
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
            itemsRepository.insertItem(item)
            _addedItem.emit(item)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemsRepository.deleteItem(item)
        }
    }

    fun undoAddItem(item: Item) {
        viewModelScope.launch {
            itemsRepository.deleteItemWithNameAndCategory(item)
        }
    }

    fun undoMoveItemToStorage(item: Item) {
        viewModelScope.launch {
            itemsRepository.resetStockQuantityAndMoveToGroceryList(item)
        }
    }

    fun undoMoveItemToGroceryList(item: Item) {
        viewModelScope.launch {
            itemsRepository.updateStockQuantityAndMoveToStorage(item, 1)
        }
    }

    fun undoDeleteItem(item: Item) {
        viewModelScope.launch {
            itemsRepository.insertItem(item)
        }
    }

    fun updateStockQuantityAndMoveToStorage(item: Item, quantity: Int) {
        viewModelScope.launch {
            itemsRepository.updateStockQuantityAndMoveToStorage(item, quantity)
            _movedItem.emit(item)
        }
    }

    fun updateStockQuantity(item: Item, quantity: Int) {
        viewModelScope.launch {
            itemsRepository.updateStockQuantity(item, quantity)
        }
    }

    fun resetStockQuantityAndMoveToGroceryList(item: Item) {
        viewModelScope.launch {
            itemsRepository.resetStockQuantityAndMoveToGroceryList(item)
        }
    }

    private fun fetchRemoteDataAndUpdateLocal() {
        viewModelScope.launch {
            itemsRepository.fetchRemoteDataAndUpdateLocal()
        }
    }
}