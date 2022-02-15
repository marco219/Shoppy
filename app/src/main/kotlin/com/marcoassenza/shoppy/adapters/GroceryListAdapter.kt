package com.marcoassenza.shoppy.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.marcoassenza.shoppy.adapters.helpers.AutoNotify.autoNotify
import com.marcoassenza.shoppy.databinding.GroceryItemAdapterBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item

class GroceryListAdapter(private val groceryItemListener: GroceryItemListener) :
    RecyclerView.Adapter<GroceryItemViewHolder>() {

    interface GroceryItemListener {
        fun onItemCardClick(item: Item)
        fun onItemCheckButtonClick(item: Item)
        fun onItemStorageButtonClick(item: Item)
    }

    private var filterText = ""
    private var categoriesFilters: MutableList<Int> = mutableListOf()

    private var originalGroceryList: MutableList<Item> = mutableListOf()
    private val filteredGroceryList: List<Item>
        get() {
            return originalGroceryList
                .filter { item ->
                    item.name.contains(filterText)
                }.filter { item ->
                    if (categoriesFilters.isEmpty()) true
                    else categoriesFilters.contains(item.category.categoryId)
                }.sortedByDescending { item -> item.id }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GroceryItemAdapterBinding.inflate(inflater, parent, false)
        return GroceryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroceryItemViewHolder, position: Int) {
        val item = filteredGroceryList[position]
        holder.binding.cardTitle.text = item.displayName
        item.category.color.let { color ->
            val isColorLight = MaterialColors.isColorLight(color)
            val textColor = if (isColorLight) Color.BLACK
            else Color.WHITE

            holder.binding.cardTitle.setTextColor(textColor)
            holder.binding.card.setCardBackgroundColor(ColorStateList.valueOf(color))
        }

        holder.binding.itemCheckButton.setOnClickListener {
            groceryItemListener.onItemCheckButtonClick(item)
        }
        holder.binding.itemStorageButton.setOnClickListener {
            groceryItemListener.onItemStorageButtonClick(item)
        }
        holder.binding.card.setOnClickListener {
            groceryItemListener.onItemCardClick(item)
        }
    }

    override fun getItemCount(): Int {
        return filteredGroceryList.size
    }

    fun setGroceryList(newList: List<Item>): Boolean {
        var firstItemChanged = false
        newList.forEach { item ->
            if (!originalGroceryList.contains(item)) {
                originalGroceryList.add(item)
                val index = filteredGroceryList.indexOf(item)
                if (index == 0) firstItemChanged = true
                notifyItemInserted(index)
            }
        }

        originalGroceryList.removeIf { item ->
            val removeIf = !newList.contains(item)
            if (removeIf) {
                notifyItemRemoved(filteredGroceryList.indexOf(item))
            }
            removeIf
        }
        return firstItemChanged
    }

    fun filter(text: String) {
        val oldFilteredGroceryList = filteredGroceryList.toMutableList()
        filterText = text
        autoNotify(oldFilteredGroceryList, filteredGroceryList) { o, n -> o.id == n.id }
    }

    fun filter(category: Category, isChecked: Boolean) {
        val oldFilteredGroceryList = filteredGroceryList.toMutableList()
        if (categoriesFilters.contains(category.categoryId) and !isChecked)
            categoriesFilters.remove(category.categoryId)

        if (!categoriesFilters.contains(category.categoryId) and isChecked)
            categoriesFilters.add(category.categoryId)

        autoNotify(oldFilteredGroceryList, filteredGroceryList) { o, n -> o.id == n.id }
    }
}

class GroceryItemViewHolder(val binding: GroceryItemAdapterBinding) :
    RecyclerView.ViewHolder(binding.root)