package com.marcoassenza.shoppy.views.fragments

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.color.MaterialColors
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.adapters.ShoppingListAdapter
import com.marcoassenza.shoppy.databinding.FragmentShoppingListBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.viewmodels.ShoppingListViewModel
import com.marcoassenza.shoppy.views.activities.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ShoppingListFragment : Fragment(), ShoppingListAdapter.ShoppingListRecyclerViewListener {

    private var _binding: FragmentShoppingListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val shoppingListViewModel: ShoppingListViewModel by viewModels()
    private val shoppingListAdapter = ShoppingListAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerview.apply {
            adapter = shoppingListAdapter
            layoutManager = activity?.resources?.configuration?.orientation.let {
                when (it) {
                    Configuration.ORIENTATION_LANDSCAPE -> StaggeredGridLayoutManager(
                        3,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                    else -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                }
            }
        }
        setupRecyclerViewObserver()
        setupSearchView()
        setupChipsFilterObserver(binding.chipGroup)
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity ->
            val fab = MainActivity.mainFabCustomizer(
                activity,
                R.string.add_item_to_shopping_list,
                R.drawable.ic_baseline_add_shopping_cart_24
            )
            {
                val bottomSheetDialog = AddItemFragment()
                bottomSheetDialog.show(parentFragmentManager, AddItemFragment.TAG)
            }

            binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0)
                        fab?.hide()
                    else if (dy < 0)
                        fab?.show()
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupChipsFilterObserver(chipGroup: ChipGroup) {
        viewLifecycleOwner.lifecycleScope.launch{
            shoppingListViewModel.categoryList.collect { list ->
                list?.forEach { category ->
                        withContext(Dispatchers.Main){
                            chipGroup.addCategoryChip(category)
                                .setOnCheckedChangeListener {_, isChecked ->
                                    shoppingListAdapter.filter(category = category, isChecked = isChecked)
                                }
                        }
                }
            }
        }
    }

    private fun setupRecyclerViewObserver() {
        viewLifecycleOwner.lifecycleScope.launch{
            shoppingListViewModel.itemList.collect { list ->
                withContext(Dispatchers.Main) {
                    shoppingListAdapter.setShoppingList(list)
                }
            }
        }
    }

    private fun setupSearchView() {
        shoppingListAdapter.filter(binding.searchBar.query.toString().lowercase())
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchText: String?): Boolean {
                searchText?.let {
                    shoppingListAdapter.filter(searchText.lowercase())
                }
                return false
            }

            override fun onQueryTextChange(searchText: String?): Boolean {
                searchText?.let {
                    shoppingListAdapter.filter(searchText.lowercase())
                }
                return true
            }
        })
    }

    override fun onItemCardClick(item: Item) {}
    override fun onItemCardLongClick(item: Item) {}

    override fun onItemCheckButtonClick(item: Item) {
        viewLifecycleOwner.lifecycleScope.launch {
            shoppingListViewModel.deleteItem(item)
        }
    }

    override fun onItemInventoryButtonClick(item: Item) {}

    private fun ChipGroup.addCategoryChip(category: Category): Chip {
        Chip(requireContext()).apply {
            id = View.generateViewId()
            text = category.displayName
            isClickable = true
            isCheckable = true
            val isColorLight = MaterialColors.isColorLight(category.color)
            if (isColorLight) setTextColor(Color.BLACK)
            else setTextColor(Color.WHITE)
            chipBackgroundColor = ColorStateList.valueOf(category.color)
            chipStrokeColor = ColorStateList.valueOf(category.color)
            setChipSpacingHorizontalResource(R.dimen.big_margin)
            isCheckedIconVisible = true
            isFocusable = true
            addView(this)
            return this
        }
    }
}