package com.marcoassenza.shoppy.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.adapters.DropDownMenuAdapter
import com.marcoassenza.shoppy.databinding.FragmentAddItemBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.viewmodels.ShoppingListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AddItemFragment(private val validationButtonListener: ValidationButtonListener) :
    BottomSheetDialogFragment() {

    private var _binding: FragmentAddItemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val shoppingListViewModel: ShoppingListViewModel by viewModels()

    private lateinit var selectedCategory: Category

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdownMenuObserver()
    }

    private fun setupDropdownMenuObserver() {
        lifecycleScope.launch {
            shoppingListViewModel.defaultCategoryList
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { list ->
                    list?.let {
                        withContext(Dispatchers.Main) {
                            val adapter = DropDownMenuAdapter(
                                requireContext(),
                                R.layout.dropdown_item, list
                            )
                            binding.itemCategoryInput.apply {
                                setAdapter(adapter)
                                setOnItemClickListener { _, _, position, _ ->
                                    selectedCategory = list[position]
                                }
                            }
                        }
                    }
                }
        }

        binding.validateButton.setOnClickListener {
            val itemName = binding.itemNameInput.text.toString()
            if (itemName.isNotEmpty() and this::selectedCategory.isInitialized) {
                val item = Item(name = itemName, category = selectedCategory)
                shoppingListViewModel.insertNewItem(item)
                validationButtonListener.onValidate(item)
                dismiss()
            }
        }
    }

    interface ValidationButtonListener {
        fun onValidate(item: Item)
    }

    companion object {
        const val TAG = "AddItemBottomSheetFragment"
    }

}
