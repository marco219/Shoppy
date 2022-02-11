package com.marcoassenza.shoppy.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.marcoassenza.shoppy.databinding.ItemCardAdapterBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import java.util.*

@SuppressLint("NotifyDataSetChanged")
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
            return  originalShoppingList
                .filter { item ->
                    item.name.lowercase(Locale.getDefault())
                        .contains(filterText.lowercase(Locale.getDefault()))
                }.filter{ item ->
                    if (categoriesFilters.isEmpty()) true
                    else categoriesFilters.contains(item.category.id)
                }
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

    fun setShoppingList(shoppingList: List<Item>) {
        val oldSize = originalShoppingList.size

        shoppingList.forEach { item ->
            if (!originalShoppingList.contains(item)) originalShoppingList.add(item)
        }

        originalShoppingList.removeIf { item ->
            val removeIf = !shoppingList.contains(item)
            if (removeIf) notifyItemRemoved(originalShoppingList.indexOf(item))
            removeIf
        }

        val newSize = originalShoppingList.size
        notifyItemRangeChanged(oldSize, newSize)
    }

    fun filter(text: String) {
        filterText = text
        notifyDataSetChanged()
    }

    fun filter(category: Category, isChecked: Boolean) {
        if (categoriesFilters.contains(category.id) and !isChecked)
            categoriesFilters.remove(category.id)

        if (!categoriesFilters.contains(category.id) and isChecked)
            categoriesFilters.add(category.id)

        notifyDataSetChanged()
    }

    private fun resetFilters(){
        categoriesFilters.clear()
        filterText = ""
        notifyDataSetChanged()
    }
}

class ShoppingItemViewHolder(val binding: ItemCardAdapterBinding) :
    RecyclerView.ViewHolder(binding.root)