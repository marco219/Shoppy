package com.marcoassenza.shoppy.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.marcoassenza.shoppy.adapters.AutoNotify.autoNotify
import com.marcoassenza.shoppy.databinding.ItemCardAdapterBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item

class ShoppingListAdapter(private val shoppingListRecyclerViewListener: ShoppingListRecyclerViewListener) :
    RecyclerView.Adapter<ShoppingItemViewHolder>() {

    interface ShoppingListRecyclerViewListener {
        fun onItemCardClick(item: Item)
        fun onItemCardLongClick(item: Item)
        fun onItemCheckButtonClick(item: Item)
        fun onItemInventoryButtonClick(item: Item)
    }

    private var filterText = ""
    private var categoriesFilters: MutableList<Int> = mutableListOf()

    private var originalShoppingList: MutableList<Item> = mutableListOf()
    private val filteredShoppingList: List<Item>
        get() {
            return originalShoppingList
                .filter { item ->
                    item.name.contains(filterText)
                }.filter { item ->
                    if (categoriesFilters.isEmpty()) true
                    else categoriesFilters.contains(item.category.categoryId)
                }.sortedByDescending { item -> item.id }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCardAdapterBinding.inflate(inflater, parent, false)
        return ShoppingItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShoppingItemViewHolder, position: Int) {
        val item = filteredShoppingList[position]
        holder.binding.cardTitle.text = item.displayName
        item.category.color.let {
            val isColorLight = MaterialColors.isColorLight(it)
            if (isColorLight) holder.binding.cardTitle.setTextColor(Color.BLACK)
            else holder.binding.cardTitle.setTextColor(Color.WHITE)
            holder.binding.card.setCardBackgroundColor(ColorStateList.valueOf(it))
        }

        holder.binding.itemCheckButton.setOnClickListener {
            shoppingListRecyclerViewListener.onItemCheckButtonClick(item)
        }
        holder.binding.itemInventoryButton.setOnClickListener {
            shoppingListRecyclerViewListener.onItemInventoryButtonClick(item)
        }
        holder.binding.card.setOnClickListener {
            shoppingListRecyclerViewListener.onItemCardClick(item)
        }
        holder.binding.card.setOnLongClickListener {
            true
        }
    }

    override fun getItemCount(): Int {
        return filteredShoppingList.size
    }

    fun setShoppingList(newList: List<Item>) {
        newList.forEach { item ->
            if (!originalShoppingList.contains(item)) {
                originalShoppingList.add(item)
                notifyItemInserted(filteredShoppingList.indexOf(item))
            }
        }

        originalShoppingList.removeIf { item ->
            val removeIf = !newList.contains(item)
            if (removeIf) {
                notifyItemRemoved(filteredShoppingList.indexOf(item))
            }
            removeIf
        }
    }

    fun filter(text: String) {
        val oldFilteredShoppingList = filteredShoppingList.toMutableList()
        filterText = text
        autoNotify(oldFilteredShoppingList, filteredShoppingList) { o, n -> o.id == n.id }
    }

    fun filter(category: Category, isChecked: Boolean) {
        val oldFilteredShoppingList = filteredShoppingList.toMutableList()
        if (categoriesFilters.contains(category.categoryId) and !isChecked)
            categoriesFilters.remove(category.categoryId)

        if (!categoriesFilters.contains(category.categoryId) and isChecked)
            categoriesFilters.add(category.categoryId)

        autoNotify(oldFilteredShoppingList, filteredShoppingList) { o, n -> o.id == n.id }
    }
}

class ShoppingItemViewHolder(val binding: ItemCardAdapterBinding) :
    RecyclerView.ViewHolder(binding.root)