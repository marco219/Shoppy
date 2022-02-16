package com.marcoassenza.shoppy.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.adapters.CategoryChipAdapter
import com.marcoassenza.shoppy.adapters.StorageAdapter
import com.marcoassenza.shoppy.databinding.FragmentStorageBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.utils.Constant
import com.marcoassenza.shoppy.viewmodels.GroceryListViewModel
import com.marcoassenza.shoppy.views.activities.MainActivity
import com.marcoassenza.shoppy.views.helpers.enableShowHideExtendedFab
import com.marcoassenza.shoppy.views.helpers.setDynamicStaggeredGridLayout
import com.marcoassenza.shoppy.views.helpers.setLinearLayout
import com.marcoassenza.shoppy.views.helpers.showUndoActionSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class StorageFragment : Fragment() {

    private var _binding: FragmentStorageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val groceryListViewModel: GroceryListViewModel by activityViewModels()
    private lateinit var storageAdapter: StorageAdapter
    private lateinit var categoryListAdapter: CategoryChipAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStorageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChipRecyclerView()
        setupItemRecyclerView()
        setupItemRecyclerViewObserver()
        setupChipRecyclerViewObserver()
        setupSearchView()
        setupAddedItemObserver()
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
        groceryListViewModel.storageCategoryList
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                withContext(Dispatchers.Main) {
                    categoryListAdapter.setCategoryList(it)
                    binding.chipRecyclerview.smoothScrollToPosition(0)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupItemRecyclerViewObserver() {
        groceryListViewModel.storageList
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                withContext(Dispatchers.Main) {
                    if (it.isEmpty()) setEmptyStateViewVisible(true)
                    else setEmptyStateViewVisible(false)
                    val isFirstItemChanged = storageAdapter.setStorageList(it)
                    if (isFirstItemChanged) binding.itemRecyclerview.smoothScrollToPosition(0)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupItemRecyclerView() {
        storageAdapter =
            StorageAdapter(object : StorageAdapter.StorageItemListener {
                override fun onItemCardClick(item: Item) {}

                override fun onItemMinusButtonClick(item: Item) {
                    if (item.stockQuantity == 1) {
                        groceryListViewModel.resetStockQuantityAndMoveToGroceryList(item)
                        showUndoMoveItemSnackBar(item)
                    } else groceryListViewModel.updateStockQuantity(item, -1)
                }

                override fun onItemPlusButtonClick(item: Item) {
                    if (item.stockQuantity == Constant.MAX_ITEM_IN_STORAGE) return
                    groceryListViewModel.updateStockQuantity(item, 1)
                }
            })

        binding.itemRecyclerview.setDynamicStaggeredGridLayout(storageAdapter, activity)
    }

    private fun setupChipRecyclerView() {
        categoryListAdapter =
            CategoryChipAdapter(object : CategoryChipAdapter.CategoryChipListener {
                override fun onCategoryChipClick(category: Category, isChecked: Boolean) {
                    storageAdapter.filter(category, isChecked)
                }
            })

        binding.chipRecyclerview.setLinearLayout(categoryListAdapter, activity)
    }

    private fun setupSearchView() {
        storageAdapter.filter(binding.searchBar.query.toString().lowercase())
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchText: String?): Boolean {
                searchText?.let {
                    storageAdapter.filter(searchText.lowercase())
                }
                return false
            }

            override fun onQueryTextChange(searchText: String?): Boolean {
                searchText?.let {
                    storageAdapter.filter(searchText.lowercase())
                }
                return true
            }
        })
    }

    private fun setupFab() {
        activity?.let { activity ->
            val fab = MainActivity.mainFabCustomizer(
                activity,
                R.string.add_item_to_storage,
                R.drawable.ic_baseline_add_24
            ) { showAddItemBottomSheet() }

            binding.itemRecyclerview.enableShowHideExtendedFab(fab)
        }
    }

    private fun setupAddedItemObserver() {
        groceryListViewModel.addedItem
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { item ->
                item?.let {
                    showUndoAddItemSnackBar(item)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showAddItemBottomSheet() {
        findNavController().navigate(StorageFragmentDirections.navigateToAddItemToStorage())
    }

    private fun showUndoAddItemSnackBar(item: Item) {
        binding.root.rootView.showUndoActionSnackbar(
            item.displayName.plus(" ")
                .plus(getString(R.string.item_successfully_added))
        ) { groceryListViewModel.undoAddItem(item) }
    }

    private fun showUndoMoveItemSnackBar(item: Item) {
        binding.root.rootView.showUndoActionSnackbar(
            item.displayName.plus(" ")
                .plus(getString(R.string.item_successfully_moved_to_grocery_list))
        ) { groceryListViewModel.undoMoveItemToGroceryList(item) }
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
}