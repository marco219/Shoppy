package com.marcoassenza.shoppy.views.fragments.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.adapters.DropDownMenuAdapter
import com.marcoassenza.shoppy.databinding.FragmentAddItemToStorageBinding
import com.marcoassenza.shoppy.models.Category
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.viewmodels.ItemsViewModel
import com.marcoassenza.shoppy.views.helpers.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AddItemToStorageFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAddItemToStorageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val itemsViewModel: ItemsViewModel by activityViewModels()

    private lateinit var selectedCategory: Category

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemToStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMinusPlusButton()
        setupDropdownMenuObserver()
        setupValidationButton()
    }

    private fun setupDropdownMenuObserver() {
        itemsViewModel.defaultCategoryList
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { list ->
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
                            setOnFocusChangeListener { v, hasFocus -> if (hasFocus) v.hideKeyboard() }
                        }
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupValidationButton() {
        binding.validateButton.setOnClickListener {
            val itemName = binding.itemNameInput.text.toString().lowercase()
            val itemQuantity = binding.stockQuantityText.text.toString().toInt()

            when {
                itemName.isBlank() -> {
                    binding.itemNameInputLayout.error =
                        resources.getString(R.string.no_item_name_error)
                }
                itemQuantity < 1 -> {
                }
                !this::selectedCategory.isInitialized -> {
                    binding.itemCategoryInputLayout.error =
                        resources.getString(R.string.no_category_error)
                }
                else -> {
                    val item = Item(
                        name = itemName,
                        category = selectedCategory,
                        isInGroceryList = false,
                        stockQuantity = itemQuantity
                    )
                    itemsViewModel.insertNewItem(item)
                    dismiss()
                }
            }
        }
    }

    private fun setupMinusPlusButton() {
        binding.plusButton.setOnClickListener {
            var quantityToAdd: Int = binding.stockQuantityText.text.toString().toInt()
            quantityToAdd++
            binding.stockQuantityText.text = quantityToAdd.toString()
        }
        binding.minusButton.setOnClickListener {
            var quantityToAdd: Int = binding.stockQuantityText.text.toString().toInt()
            if (quantityToAdd > 0) {
                quantityToAdd--
                binding.stockQuantityText.text = quantityToAdd.toString()
            }
        }
    }
}
