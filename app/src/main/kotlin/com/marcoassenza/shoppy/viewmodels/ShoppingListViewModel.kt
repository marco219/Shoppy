package com.marcoassenza.shoppy.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShoppingListViewModel : ViewModel() {

    val itemList = MutableLiveData<List<String>>().apply {
        value = listOf(
            "Bamboo Shoots",
            "Banana Squash",
            "Beetroot",
            "Belgian Endive",
            "Bell Peppers",
            "Black Eyed Pea",
            "Black Radish",
            "Black Salsify",
            "Bok Choy",
            "Broadleaf Arrowhea",
            "Broccoflower",
            "Broccoli",
            "Broccolini",
            "Brussel Sprouts",
            "Burdock Roots",
            "Buttercup Squash",
            "Butternut Squash"
        )
    }

    var listFilters = MutableLiveData<List<String>>().apply {
        value = listOf(
            "Fruits",
            "Légumes",
            "Epices",
            "Boissons",
            "Condiments",
            "Autres"
        )
    }
}