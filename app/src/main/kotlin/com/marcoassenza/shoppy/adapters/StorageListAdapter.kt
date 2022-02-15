package com.marcoassenza.shoppy.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.marcoassenza.shoppy.adapters.helpers.AutoNotify.autoNotify
import com.marcoassenza.shoppy.databinding.StorageItemAdapterBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item

class StorageAdapter(private val storageItemListener: StorageItemListener) :
    RecyclerView.Adapter<StorageItemViewHolder>() {

    interface StorageItemListener {
        fun onItemCardClick(item: Item)
        fun onItemMinusButtonClick(item: Item)
        fun onItemPlusButtonClick(item: Item)
    }

    private var filterText = ""
    private var categoriesFilters: MutableList<Int> = mutableListOf()

    private var originalStorageList: MutableList<Item> = mutableListOf()
    private val filteredStorageList: List<Item>
        get() {
            return originalStorageList
                .filter { item ->
                    item.name.contains(filterText)
                }.filter { item ->
                    if (categoriesFilters.isEmpty()) true
                    else categoriesFilters.contains(item.category.categoryId)
                }.sortedByDescending { item -> item.id }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StorageItemAdapterBinding.inflate(inflater, parent, false)
        return StorageItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StorageItemViewHolder, position: Int) {
        val item = filteredStorageList[position]
        holder.binding.cardTitle.text = item.displayName
        holder.binding.stockQuantityText.text = item.stockQuantity.toString()
        item.category.color.let { color ->
            val isColorLight = MaterialColors.isColorLight(color)
            val textColor = if (isColorLight) Color.BLACK
            else Color.WHITE

            holder.binding.cardTitle.setTextColor(textColor)
            holder.binding.stockQuantityText.setTextColor(textColor)

            holder.binding.card.setCardBackgroundColor(ColorStateList.valueOf(color))
        }

        holder.binding.minusButton.setOnClickListener {
            storageItemListener.onItemMinusButtonClick(item)
        }
        holder.binding.plusButton.setOnClickListener {
            storageItemListener.onItemPlusButtonClick(item)
        }
        holder.binding.card.setOnClickListener {
            storageItemListener.onItemCardClick(item)
        }
    }

    override fun getItemCount(): Int {
        return filteredStorageList.size
    }

    fun setStorageList(newList: List<Item>): Boolean {
        var firstItemChanged = false
        newList.forEach { item ->
            if (!originalStorageList.contains(item)) {
                originalStorageList.add(item)
                val index = filteredStorageList.indexOf(item)
                if (index == 0) firstItemChanged = true
                notifyItemInserted(index)
            }
        }

        originalStorageList.removeIf { item ->
            val removeIf = !newList.contains(item)
            if (removeIf) {
                notifyItemRemoved(filteredStorageList.indexOf(item))
            }
            removeIf
        }
        return firstItemChanged
    }

    fun filter(text: String) {
        val oldFilteredGroceryList = filteredStorageList.toMutableList()
        filterText = text
        autoNotify(oldFilteredGroceryList, filteredStorageList) { o, n -> o.id == n.id }
    }

    fun filter(category: Category, isChecked: Boolean) {
        val oldFilteredGroceryList = filteredStorageList.toMutableList()
        if (categoriesFilters.contains(category.categoryId) and !isChecked)
            categoriesFilters.remove(category.categoryId)

        if (!categoriesFilters.contains(category.categoryId) and isChecked)
            categoriesFilters.add(category.categoryId)

        autoNotify(oldFilteredGroceryList, filteredStorageList) { o, n -> o.id == n.id }
    }
}

class StorageItemViewHolder(val binding: StorageItemAdapterBinding) :
    RecyclerView.ViewHolder(binding.root)