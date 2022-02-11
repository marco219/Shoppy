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

class ShoppingListAdapter(private val shoppingListRecyclerViewListener: ShoppingListRecyclerViewListener) :
    RecyclerView.Adapter<ShoppingItemViewHolder>() {

    interface ShoppingListRecyclerViewListener {
        fun onItemClick(shoppingItem: Item)
        fun onItemLongClick(shoppingItem: Item)
        //fun onRecyclerViewEnd()
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

        holder.binding.card.setOnClickListener {
            shoppingListRecyclerViewListener.onItemClick(item)
        }

        holder.binding.card.setOnLongClickListener { /*it as MaterialCardView
            it.isChecked = !it.isChecked
            shoppingListRecyclerViewListener.onItemLongClick(shoppingItem)
            */true
        }
    }

    override fun getItemCount(): Int {
        return filteredShoppingList.size
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

    @SuppressLint("NotifyDataSetChanged")
    fun setShoppingList(shoppingList: List<Item>) {
        val oldSize = originalShoppingList.size

        shoppingList.forEach { item ->
            if (!originalShoppingList.contains(item)) originalShoppingList.add(item)
        }

        val newSize = originalShoppingList.size
        notifyItemRangeChanged(oldSize, newSize)
    }
}

class ShoppingItemViewHolder(val binding: ItemCardAdapterBinding) :
    RecyclerView.ViewHolder(binding.root)