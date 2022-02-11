package com.marcoassenza.shoppy.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.models.Category
import javax.annotation.Nonnull

class DropDownMenuAdapter(
    @Nonnull val context: Context,
    @LayoutRes val resource: Int,
    @Nonnull val categoryList: List<Category>) : BaseAdapter(), Filterable {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return categoryList.size
    }

    override fun getItem(position: Int): Any {
        return categoryList[position].displayName
    }

    override fun getItemId(position: Int): Long {
        return categoryList[position].id.toLong()
    }

    fun getCategory(position: Int): Category {
        return categoryList[position]
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(resource, parent, false)
        rowView.findViewById<TextView>(R.id.text_view).text = categoryList[position].displayName
        return rowView
    }

    override fun getFilter(): Filter {
        return FakeFilter()
    }
}

class FakeFilter:Filter(){
    override fun performFiltering(constraint: CharSequence?): FilterResults {
       return FilterResults()
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
    }

}