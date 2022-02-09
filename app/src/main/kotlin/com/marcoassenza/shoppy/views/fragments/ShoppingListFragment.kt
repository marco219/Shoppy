package com.marcoassenza.shoppy.views.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.marcoassenza.shoppy.views.activities.MainActivity
import com.marcoassenza.shoppy.viewmodels.ShoppingListViewModel
import timber.log.Timber

class ShoppingListFragment : Fragment(), ShoppingListAdapter.ShoppingListRecyclerViewListener {

    private var _binding: FragmentShoppingListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var shoppingListViewModel: ShoppingListViewModel
    private val shoppingListAdapter = ShoppingListAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        shoppingListViewModel = ViewModelProvider(this)[ShoppingListViewModel::class.java]

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
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity ->
            MainActivity.mainFabCustomizer(
                activity,
                R.string.add_item_to_shopping_list,
                R.drawable.ic_baseline_add_shopping_cart_24
            )
            {
                context?.let {
                    val bottomSheetDialog = AddItemFragment()
                    bottomSheetDialog.show(parentFragmentManager, AddItemFragment.TAG)
                }
            }
        }

        val fab = activity?.findViewById<ExtendedFloatingActionButton>(R.id.fab)
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    fab?.hide()
                else if (dy < 0)
                    fab?.show()
            }
        })

        setupSearchView()

        context?.let {
            setupChipsFilterObserver(it, binding.chipGroup)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupChipsFilterObserver(context: Context, chipGroup: ChipGroup) {
        shoppingListViewModel.listFilters.observe(viewLifecycleOwner) { list ->
            list.forEach { filter ->
                chipGroup.addChip(context, filter).setOnCheckedChangeListener { chip, isChecked ->
                    Timber.d((chip as Chip).text.toString() + " is " + isChecked)
                }
            }
        }
    }

    private fun setupRecyclerViewObserver() {
        shoppingListViewModel.itemList.observe(viewLifecycleOwner) { list ->
            shoppingListAdapter.setShoppingList(list)
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

    override fun onItemClick(shoppingItem: String) {
        Snackbar.make(binding.root, shoppingItem, BaseTransientBottomBar.LENGTH_SHORT)
            .show()
    }

    override fun onItemLongClick(shoppingItem: String) {
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