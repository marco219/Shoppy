package com.marcoassenza.shoppy.views.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.adapters.CategoryChipAdapter
import com.marcoassenza.shoppy.adapters.ShoppingListAdapter
import com.marcoassenza.shoppy.databinding.FragmentShoppingListBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.viewmodels.ShoppingListViewModel
import com.marcoassenza.shoppy.views.activities.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ShoppingListFragment : Fragment(),
    ShoppingListAdapter.ShoppingListRecyclerViewListener,
    CategoryChipAdapter.CategoryChipRecyclerViewListener,
    AddItemFragment.ValidationButtonListener {

    private var _binding: FragmentShoppingListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val shoppingListViewModel: ShoppingListViewModel by viewModels()
    private val shoppingListAdapter = ShoppingListAdapter(this)
    private val categoryListAdapter = CategoryChipAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChipRecyclerView()
        setupItemRecyclerView()
        setupItemRecyclerViewObserver()
        setupChipRecyclerViewObserver()
        setupSearchView()
    }

    override fun onResume() {
        super.onResume()
        setupFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupChipRecyclerViewObserver() {
        lifecycleScope.launch {
            shoppingListViewModel.itemList
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { list ->
                    withContext(Dispatchers.Main) {
                        categoryListAdapter.setCategoryList(
                            list.map { it.category }
                                .distinct())
                        binding.chipRecyclerview.smoothScrollToPosition(0)
                    }
                }
        }
    }

    private fun setupItemRecyclerViewObserver() {
        lifecycleScope.launch {
            shoppingListViewModel.itemList
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { list ->
                    withContext(Dispatchers.Main) {
                        if (list.isEmpty()) setEmptyStateViewVisible(true)
                        else setEmptyStateViewVisible(false)
                        shoppingListAdapter.setShoppingList(list)
                        binding.itemRecyclerview.smoothScrollToPosition(0)
                    }
                }
        }
    }

    private fun setupItemRecyclerView() {
        binding.itemRecyclerview.apply {
            adapter = shoppingListAdapter
            layoutManager = activity?.resources?.configuration?.orientation.let {
                when (it) {
                    Configuration.ORIENTATION_LANDSCAPE -> StaggeredGridLayoutManager(
                        3,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                    else -> StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                }
            }
        }
    }

    private fun setupChipRecyclerView() {
        binding.chipRecyclerview.apply {
            adapter = categoryListAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
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

    private fun setupFab() {
        activity?.let { activity ->
            val fab = MainActivity.mainFabCustomizer(
                activity,
                R.string.add_item_to_shopping_list,
                R.drawable.ic_baseline_add_shopping_cart_24
            )
            {
                val bottomSheetDialogFragment = AddItemFragment(this)
                bottomSheetDialogFragment.show(parentFragmentManager, AddItemFragment.TAG)
            }

            enableShowHideFab(fab)
        }
    }

    private fun enableShowHideFab(fab: ExtendedFloatingActionButton?) {
        binding.itemRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    fab?.hide()
                else if (dy < 0)
                    fab?.show()
            }
        })
    }

    private fun setEmptyStateViewVisible(isVisible: Boolean) {
        if (isVisible) {
            binding.emptyStateImage.visibility = View.VISIBLE
            binding.emptyStateText.visibility = View.VISIBLE
        } else {
            binding.emptyStateImage.visibility = View.GONE
            binding.emptyStateText.visibility = View.GONE
        }
    }

    override fun onItemCardClick(item: Item) {}

    override fun onItemCardLongClick(item: Item) {}

    override fun onItemCheckButtonClick(item: Item) {
        lifecycleScope.launch {
            shoppingListViewModel.deleteItem(item)
        }
        Snackbar.make(
            binding.root.rootView,
            item.displayName.plus(" ").plus(getString(R.string.item_successfully_deleted)),
            Snackbar.LENGTH_LONG
        )
            .apply {
                setAction(R.string.undo) { shoppingListViewModel.insertNewItem(item) }
            }.show()
    }

    override fun onItemInventoryButtonClick(item: Item) {}

    override fun onValidate(item: Item) {
        Snackbar.make(
            binding.root.rootView,
            item.displayName.plus(" ").plus(getString(R.string.item_successfully_added)),
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.undo) { shoppingListViewModel.undoItem(item) }
        }.show()
    }

    override fun onCategoryChipClick(category: Category, isChecked: Boolean) {
        shoppingListAdapter.filter(category, isChecked)
    }
}