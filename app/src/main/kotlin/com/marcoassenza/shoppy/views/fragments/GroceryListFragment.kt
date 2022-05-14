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
import com.marcoassenza.shoppy.adapters.GroceryListAdapter
import com.marcoassenza.shoppy.data.local.remote.RemoteDatabaseStatus.*
import com.marcoassenza.shoppy.databinding.FragmentGroceryListBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
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
class GroceryListFragment : Fragment() {

    private var _binding: FragmentGroceryListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //delegate by activityViewModels() MUST be used in order for the viewModel to be shared across fragment
    private val itemsViewModel: ItemsViewModel by activityViewModels()
    private lateinit var groceryListAdapter: GroceryListAdapter
    private lateinit var categoryListAdapter: CategoryChipAdapter

    private var networkSnackbar: Snackbar? = null
    private var remoteDbStatusSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroceryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChipRecyclerView()
        setupItemRecyclerView()
        setupItemRecyclerViewObserver()
        setupChipRecyclerViewObserver()
        setupSearchView()
        setupMovedItemObserver()
        setupAddedItemObserver()
    }

    override fun onResume() {
        super.onResume()
        setupBaseView()
        setupNetworkStatusObserver()
        setupRemoteDatabaseStatusObserver()
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
                    NetworkStatus.Unavailable -> showNoNetworkSnackBar()
                    NetworkStatus.Available -> {
                        networkSnackbar?.dismiss()
                        showNetworkIsBackSnackBar()
                    }
                    else -> {}
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupRemoteDatabaseStatusObserver() {
        itemsViewModel.remoteDataStatus
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                when(it){
                    Unavailable, Failure, Unknown -> showRemoteDbUnavailableSnackBar()
                    Available, ChangingUser, UserNameIsNull -> remoteDbStatusSnackbar?.dismiss()
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupChipRecyclerViewObserver() {
        itemsViewModel.categoryList
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
        itemsViewModel.groceryList
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                withContext(Dispatchers.Main) {
                    if (it.isEmpty()) setEmptyStateViewVisible(true)
                    else setEmptyStateViewVisible(false)
                    val isFirstItemChanged = groceryListAdapter.setGroceryList(it)
                    if (isFirstItemChanged) binding.itemRecyclerview.smoothScrollToPosition(0)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupItemRecyclerView() {
        groceryListAdapter =
            GroceryListAdapter(object : GroceryListAdapter.GroceryItemListener {
                override fun onItemCardClick(item: Item) {}

                override fun onItemCheckButtonClick(item: Item) {
                    if (requireContext().isConnected) {
                        itemsViewModel.deleteItem(item)
                        showUndoDeleteItemSnackBar(item)
                    }
                }

                override fun onItemStorageButtonClick(item: Item) {
                    showMoveToStorageBottomSheet(item)
                }
            })

        binding.itemRecyclerview.setDynamicStaggeredGridLayout(groceryListAdapter, activity)
    }

    private fun setupChipRecyclerView() {
        categoryListAdapter =
            CategoryChipAdapter(object : CategoryChipAdapter.CategoryChipListener {
                override fun onCategoryChipClick(category: Category, isChecked: Boolean) {
                    groceryListAdapter.filter(category, isChecked)
                }
            })

        binding.chipRecyclerview.setLinearLayout(categoryListAdapter, activity)
    }

    private fun setupSearchView() {
        groceryListAdapter.filter(binding.searchBar.query.toString().lowercase())
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchText: String?): Boolean {
                searchText?.let {
                    groceryListAdapter.filter(searchText.lowercase())
                }
                return false
            }

            override fun onQueryTextChange(searchText: String?): Boolean {
                searchText?.let {
                    groceryListAdapter.filter(searchText.lowercase())
                }
                return true
            }
        })
    }

    private fun setupBaseView() {
        val fab = requireActivity().mainFabCustomizer(
            R.string.add_item_to_grocery_list,
            R.drawable.ic_baseline_add_shopping_cart_24
        ) {
            showAddItemBottomSheet()
        }
        binding.itemRecyclerview.enableShowHideExtendedFab(fab)

        requireActivity().setTopAppBarSubtitle(R.string.title_grocery_list)
    }

    private fun showMoveToStorageBottomSheet(item: Item) {
        if (requireContext().isConnected) {
            itemsViewModel.setToBeTreatedItem(item)
            findNavController().navigate(GroceryListFragmentDirections.navigateToMoveItemToStorage())
        }
    }

    private fun showAddItemBottomSheet() {
        if (requireContext().isConnected) findNavController().navigate(GroceryListFragmentDirections.navigateToAddItemToGroceryList())
    }

    private fun setupMovedItemObserver() {
        itemsViewModel.movedItem
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { item ->
                item?.let {
                    showUndoMoveItemSnackBar(item)
                }
            }
            .launchIn(lifecycleScope)
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

    private fun showUndoMoveItemSnackBar(item: Item) {
        binding.root.showUndoActionSnackbar(
            item.displayName.plus(" ")
                .plus(getString(R.string.item_successfully_moved_to_storage))
        ) { itemsViewModel.undoMoveItemToStorage(item) }
    }

    private fun showUndoAddItemSnackBar(item: Item) {
        binding.root.showUndoActionSnackbar(
            item.displayName.plus(" ")
                .plus(getString(R.string.item_successfully_added))
        ) { itemsViewModel.undoAddItem(item) }
    }

    private fun showUndoDeleteItemSnackBar(item: Item) {
        binding.root.showUndoActionSnackbar(
            item.displayName.plus(" ")
                .plus(getString(R.string.item_successfully_deleted))
        ) { itemsViewModel.undoDeleteItem(item) }
    }

    private fun showNoNetworkSnackBar(): Snackbar? {
        networkSnackbar = binding.root.showIndefiniteSnackbar(getString(R.string.no_internet))
        return networkSnackbar
    }

    private fun showRemoteDbUnavailableSnackBar(): Snackbar? {
        remoteDbStatusSnackbar = binding.root.showIndefiniteSnackbar(getString(R.string.remote_db_is_unavailable))
        return remoteDbStatusSnackbar
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
