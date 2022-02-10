package com.marcoassenza.shoppy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassenza.shoppy.data.ItemRepository
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

    val categoryList: StateFlow<List<String>?> = flow {
        emit(itemRepository.getCategories().data)
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000), // Or Lazily because it's a one-shot
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch{
            itemRepository.insertItems(listOf(
                Item("Bamboo Shoots", "Légumes", 10),
                Item("Banana Squash", "Légumes", 10),
                Item("Beetroot", "Légumes", 10),
                Item("Belgian Endive", "Légumes", 10),
                Item("Bell Peppers", "Légumes", 10),
                Item("Black Eyed Pea", "Légumes", 10),
                Item("Black Radish", "Légumes", 10),
                Item("Black Salsify", "Légumes", 10),
                Item("Bok Choy", "Légumes", 10),
                Item("Broadleaf Arrowhea", "Légumes", 10),
                Item("Broccoflower", "Légumes", 10),
                Item("Broccoli", "Légumes", 10),
                Item("Broccolini", "Légumes", 10),
                Item("Brussel Sprouts", "Légumes", 10),
                Item("Burdock Roots", "Légumes", 10),
                Item("Buttercup Squash", "Légumes", 10),
                Item("Butternut Squash", "Légumes", 10),
                Item("Banane", "Fruits", 10),
                Item("Pomme", "Fruits", 10),
                Item("Poire", "Fruits", 10),
                Item("Kiwi", "Fruits", 10),
            ))
            itemRepository.getItems().data?.collect{
                _itemList.value = it
            }
        }
    }

    suspend fun insertNewItem(item:Item){
        viewModelScope.launch {
            itemRepository.insertItem(item)
        }
    }
}