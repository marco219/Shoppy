package com.marcoassenza.shoppy.views.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.adapters.ShoppingListAdapter
import com.marcoassenza.shoppy.databinding.FragmentShoppingListBinding
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.viewmodels.ShoppingListViewModel
import com.marcoassenza.shoppy.views.activities.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

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
                            chipGroup.addChip(requireContext(), category).setOnCheckedChangeListener {chip, isChecked ->
                                setCategoryFilter(chip.text.toString(), isChecked)
                            }
                        }
                }
            }
        }
    }

    private fun setupRecyclerViewObserver() {
        viewLifecycleOwner.lifecycleScope.launch{
            shoppingListViewModel.itemList.collect { list ->
                shoppingListAdapter.setShoppingList(list)
            }
        }
    }

    private fun setupSearchView() {
        shoppingListAdapter.filter(binding.searchBar.query.toString().lowercase())
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchText: String?): Boolean {
                searchText?.let {
                    setTextFilter(searchText.lowercase())
                }
                return false
            }

            override fun onQueryTextChange(searchText: String?): Boolean {
                searchText?.let {
                    setTextFilter(searchText.lowercase())
                }
                return true
            }
        })
    }

    private fun setCategoryFilter(chipText: String, isChecked: Boolean){
        shoppingListAdapter.filter(category = chipText, isChecked = isChecked)
    }

    private fun setTextFilter(filterText:String){
        shoppingListAdapter.filter(filterText)
    }

    override fun onItemClick(shoppingItem: Item) {
        Snackbar.make(binding.root, shoppingItem.name, BaseTransientBottomBar.LENGTH_SHORT)
            .show()
    }

    override fun onItemLongClick(shoppingItem: Item) {
        //TODO("Not yet implemented")
    }

    private fun ChipGroup.addChip(context: Context, label: String): Chip {
        Chip(context).apply {
            id = View.generateViewId()
            text = label
            isClickable = true
            isCheckable = true
            setChipSpacingHorizontalResource(R.dimen.big_margin)
            isCheckedIconVisible = true
            isFocusable = true
            addView(this)
            return this
        }
    }
}