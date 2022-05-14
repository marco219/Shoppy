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
import com.google.android.material.snackbar.Snackbar
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.adapters.CategoryChipAdapter
import com.marcoassenza.shoppy.adapters.StorageAdapter
import com.marcoassenza.shoppy.databinding.FragmentStorageBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.utils.Constant
import com.marcoassenza.shoppy.utils.NetworkStatus
import com.marcoassenza.shoppy.utils.isConnected
import com.marcoassenza.shoppy.viewmodels.ItemsViewModel
import com.marcoassenza.shoppy.views.helpers.*
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

    private val itemsViewModel: ItemsViewModel by activityViewModels()
    private lateinit var storageAdapter: StorageAdapter
    private lateinit var categoryListAdapter: CategoryChipAdapter

    private var networkSnackbar: Snackbar? = null

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
        setupBaseView()
        setupNetworkStatusObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupNetworkStatusObserver() {
        if (!requireContext().isConnected)
            networkSnackbar = showNoNetworkSnackBar()

        itemsViewModel.networkStatus
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                when (it) {
                    NetworkStatus.Unavailable -> networkSnackbar = showNoNetworkSnackBar()
                    NetworkStatus.Available -> {
                        networkSnackbar?.dismiss()
                        showNetworkIsBackSnackBar()
                    }
                    else -> {}
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupChipRecyclerViewObserver() {
        itemsViewModel.storageCategoryList
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
        itemsViewModel.storageList
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
                    if (requireContext().isConnected) {
                        if (item.stockQuantity == 1) {
                            itemsViewModel.resetStockQuantityAndMoveToGroceryList(item)
                            showUndoMoveItemSnackBar(item)
                        } else itemsViewModel.updateStockQuantity(item, -1)
                    }
                }

                override fun onItemPlusButtonClick(item: Item) {
                    if (requireContext().isConnected) {
                        if (item.stockQuantity == Constant.MAX_ITEM_IN_STORAGE) return
                        itemsViewModel.updateStockQuantity(item, 1)
                    }
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

    private fun setupBaseView() {
        val fab = requireActivity().mainFabCustomizer(
            R.string.add_item_to_storage,
            R.drawable.ic_baseline_add_24
        ) { showAddItemBottomSheet() }

        binding.itemRecyclerview.enableShowHideExtendedFab(fab)

        requireActivity().setTopAppBarSubtitle(R.string.title_storage)
    }

    private fun setupAddedItemObserver() {
        itemsViewModel.addedItem
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { item ->
                item?.let {
                    //showUndoAddItemSnackBar(item)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showAddItemBottomSheet() {
        if (requireContext().isConnected) {
            findNavController().navigate(StorageFragmentDirections.navigateToAddItemToStorage())
        }
    }

    private fun showUndoAddItemSnackBar(item: Item) {
        binding.root.showUndoActionSnackbar(
            item.displayName.plus(" ")
                .plus(getString(R.string.item_successfully_added))
        ) { itemsViewModel.undoAddItem(item) }
    }

    private fun showUndoMoveItemSnackBar(item: Item) {
        binding.root.showUndoActionSnackbar(
            item.displayName.plus(" ")
                .plus(getString(R.string.item_successfully_moved_to_grocery_list))
        ) { itemsViewModel.undoMoveItemToGroceryList(item) }
    }

    private fun showNoNetworkSnackBar(): Snackbar? {
        networkSnackbar = binding.root.showIndefiniteSnackbar(getString(R.string.no_internet))
        return networkSnackbar
    }

    private fun showNetworkIsBackSnackBar() {
        binding.root.showLongSnackbar(getString(R.string.back_online))
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
