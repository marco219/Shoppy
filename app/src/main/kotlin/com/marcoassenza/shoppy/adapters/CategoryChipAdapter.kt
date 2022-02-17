package com.marcoassenza.shoppy.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.marcoassenza.shoppy.databinding.CategoryChipAdapterBinding
import com.marcoassenza.shoppy.models.Category

class CategoryChipAdapter(private val categoryChipListener: CategoryChipListener) :
    RecyclerView.Adapter<CategoryChipViewHolder>() {

    interface CategoryChipListener {
        fun onCategoryChipClick(category: Category, isChecked: Boolean)
    }

    private var categoryList: MutableList<Category> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryChipViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CategoryChipAdapterBinding.inflate(inflater, parent, false)
        return CategoryChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryChipViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.chip.text = category.categoryDisplayName
        category.color.let {
            val isColorLight = MaterialColors.isColorLight(it)
            if (isColorLight) holder.binding.chip.setTextColor(Color.BLACK)
            else holder.binding.chip.setTextColor(Color.WHITE)
            holder.binding.chip.chipBackgroundColor = ColorStateList.valueOf(it)
            holder.binding.chip.chipStrokeColor = ColorStateList.valueOf(it)
        }

        holder.binding.chip.setOnCheckedChangeListener { _, isChecked ->
            categoryChipListener.onCategoryChipClick(category, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun setCategoryList(newList: List<Category>) {
        newList.forEach { category ->
            if (!categoryList.contains(category)) {
                categoryList.add(category)
                notifyItemInserted(categoryList.indexOf(category))
            }
        }

        categoryList.removeIf { category ->
            val removeIf = !newList.contains(category)
            if (removeIf) notifyItemRemoved(categoryList.indexOf(category))
            removeIf
        }
    }

}

class CategoryChipViewHolder(val binding: CategoryChipAdapterBinding) :
    RecyclerView.ViewHolder(binding.root)