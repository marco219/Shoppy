package com.marcoassenza.shoppy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassenza.shoppy.data.ItemsRepository
import com.marcoassenza.shoppy.data.local.datastore.PreferencesDataStoreManager
import com.marcoassenza.shoppy.data.local.remote.RemoteDatabaseStatus
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.utils.NetworkStatus
import com.marcoassenza.shoppy.utils.NetworkStatusTracker
import com.marcoassenza.shoppy.utils.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val networkStatusTracker: NetworkStatusTracker,
    private val itemsRepository: ItemsRepository,
    private val preferencesDataStoreManager: PreferencesDataStoreManager
) : ViewModel() {

    private var hasLostNetworkConnection: Boolean = false

    private val _remoteDataStatus: MutableStateFlow<RemoteDatabaseStatus> =
        MutableStateFlow(RemoteDatabaseStatus.Unknown)
    val remoteDataStatus: StateFlow<RemoteDatabaseStatus> = _remoteDataStatus

    private val _movedItem = MutableSharedFlow<Item?>(0)
    val movedItem: SharedFlow<Item?> = _movedItem

    private val _addedItem = MutableSharedFlow<Item?>(0)
    val addedItem: SharedFlow<Item?> = _addedItem

    private var _toBeTreatedItem = MutableStateFlow<Item?>(null)
    val toBeTreatedItem: StateFlow<Item?> = _toBeTreatedItem

    private var _itemToAddFromSearchField = MutableStateFlow<String?>(null)
    val itemToAddFromSearchField: StateFlow<String?> = _itemToAddFromSearchField


    val username: StateFlow<String> = preferencesDataStoreManager.userName
        .onEach { user ->
            if (user.isNotEmpty()) {
                itemsRepository.updateUser(user)
                itemsRepository.remoteDataListener
                    .onEach {
                        _remoteDataStatus.emit(it)
                    }
                    .stateIn(viewModelScope, Eagerly, RemoteDatabaseStatus.Unknown)
            } else _remoteDataStatus.emit(RemoteDatabaseStatus.UserNameIsNull)
        }
        .stateIn(viewModelScope, Eagerly, "")

    //TODO: maybe use a state flow instead of a shared one
    val networkStatus: SharedFlow<NetworkStatus?> = flow {
        networkStatusTracker.networkStatus.map(
            onLost = {
                hasLostNetworkConnection = true
                _remoteDataStatus.emit(RemoteDatabaseStatus.Unavailable)
                NetworkStatus.Unavailable
            },
            onAvailable = {
                _remoteDataStatus.emit(RemoteDatabaseStatus.Available)
                if (hasLostNetworkConnection) {
                    hasLostNetworkConnection = false
                    NetworkStatus.Available
                }
                else {
                    hasLostNetworkConnection = false
                    null
                }
            },
            onUnavailable = {
                hasLostNetworkConnection = true
                _remoteDataStatus.emit(RemoteDatabaseStatus.Unavailable)
                NetworkStatus.Unavailable
            }
        ).collect {
            emit(it)
        }
    }.shareIn(viewModelScope, Lazily, 0)

    val groceryList: StateFlow<List<Item>> = flow {
        emitAll(itemsRepository.getGroceryItems())
    }.stateIn(viewModelScope, Lazily, emptyList())

    val categoryList: StateFlow<List<Category>> = flow {
        emitAll(itemsRepository.getGroceryListCategories())
    }.stateIn(viewModelScope, Lazily, emptyList())

    val defaultCategoryList: StateFlow<List<Category>?> = flow {
        emit(itemsRepository.getDefaultCategories())
    }.stateIn(viewModelScope, Lazily, emptyList())

    val storageList: StateFlow<List<Item>> = flow {
        emitAll(itemsRepository.getStorageItems())
    }.stateIn(viewModelScope, Lazily, emptyList())

    val storageCategoryList: StateFlow<List<Category>> = flow {
        emitAll(itemsRepository.getStorageCategories())
    }.stateIn(viewModelScope, Lazily, emptyList())

    fun setItemToAddFromSearchField(query: String?) {
        viewModelScope.launch {
            _itemToAddFromSearchField.emit(query)
        }
    }

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

    //TODO: it does not remove it because getAllItem from Dao is not updated instantly
    fun undoAddItem(item: Item) {
        /*viewModelScope.launch {
            itemsRepository.deleteItemWithNameAndCategory(item)
        }*/
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

    fun submitNewUsername(userName: String) {
        viewModelScope.launch {
            _remoteDataStatus.emit(RemoteDatabaseStatus.ChangingUser)
            itemsRepository.resetLocalDb()
            preferencesDataStoreManager.setUserName(userName)
        }
    }
}
