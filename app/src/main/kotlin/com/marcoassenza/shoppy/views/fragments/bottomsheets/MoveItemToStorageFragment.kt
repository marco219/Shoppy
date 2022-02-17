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
import com.marcoassenza.shoppy.databinding.FragmentMoveItemToStorageBinding
import com.marcoassenza.shoppy.models.Item
import com.marcoassenza.shoppy.viewmodels.ItemsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MoveItemToStorageFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMoveItemToStorageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val itemsViewModel: ItemsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoveItemToStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserverAndView()
    }

    private fun setupObserverAndView() {
        itemsViewModel.toBeTreatedItem
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { item ->
                item?.let {
                    setupQuantityText(item)
                    setupValidationButton(item)
                    setupMinusPlusButton()
                }
            }.launchIn(lifecycleScope)
    }

    private fun setupQuantityText(item: Item) {
        val quantity = item.stockQuantity
        lateinit var text: String
        if (quantity > 0) {
            text = "".plus(resources.getString(R.string.there_are_already))
                .plus(" $quantity ")
                .plus(resources.getString(R.string.in_stock))

            binding.availableQuantityInStockText.text = text
        } else {
            text = resources.getString(R.string.no_item_in_stock)
        }
        binding.availableQuantityInStockText.text = text
    }

    private fun setupValidationButton(item: Item) {
        binding.validateButton.setOnClickListener {
            val quantityToAdd = binding.stockQuantityText.text.toString().toInt()
            if (quantityToAdd > 0) {
                itemsViewModel.updateStockQuantityAndMoveToStorage(item, quantityToAdd)
                dismiss()
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
