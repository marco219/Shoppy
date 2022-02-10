package com.marcoassenza.shoppy.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marcoassenza.shoppy.databinding.ItemCardAdapterBinding
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
    private var categoriesFilters: MutableList<String> = mutableListOf()

    private var originalShoppingList: MutableList<Item> = mutableListOf()
    private val filteredShoppingList: List<Item>
        get() {
            return  originalShoppingList
                .filter { item ->
                    item.name.lowercase(Locale.getDefault())
                        .contains(filterText.lowercase(Locale.getDefault()))
                }.filter{ item ->
                    if (categoriesFilters.isEmpty()) true
                    else categoriesFilters.contains(item.category.lowercase())
                }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCardAdapterBinding.inflate(inflater, parent, false)
        return ShoppingItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShoppingItemViewHolder, position: Int) {
        val shoppingItem = filteredShoppingList[position]
        holder.binding.cardTitle.text = shoppingItem.name
        //TODO: change color based on filter
        //holder.binding.card.cardForegroundColor = MaterialColors.getColor(holder.itemView, R.color.md_theme_light_primary)

        /*val circularProgressDrawable = CircularProgressDrawable(holder.itemView.context)

        Glide.with(holder.itemView.context)
            .load(GlideUrl(user.picture.medium, LazyHeaders.Builder()
                .addHeader("User-Agent", "android")
                .build()))
            .error(R.drawable.ic_baseline_report_problem_24)
            .placeholder(circularProgressDrawable.apply {
                strokeWidth = 5f
                centerRadius = 30f
                start()
            })
            .into(holder.binding.picture)

        if (position == shoppingList.size - 3) shoppingListRecyclerViewListener.onRecyclerViewEnd()
        */

        holder.binding.card.setOnClickListener {
            shoppingListRecyclerViewListener.onItemClick(shoppingItem)
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

    fun filter(category: String, isChecked: Boolean) {
        if (categoriesFilters.contains(category.lowercase()) and !isChecked)
            categoriesFilters.remove(category.lowercase())

        if (!categoriesFilters.contains(category.lowercase()) and isChecked)
            categoriesFilters.add(category.lowercase())

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