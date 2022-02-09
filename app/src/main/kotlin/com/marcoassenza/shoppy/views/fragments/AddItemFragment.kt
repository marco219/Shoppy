package com.marcoassenza.shoppy.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.databinding.DropdownItemBinding
import com.marcoassenza.shoppy.databinding.FragmentAddItemBinding
import com.marcoassenza.shoppy.databinding.FragmentShoppingListBinding
import com.marcoassenza.shoppy.viewmodels.ShoppingListViewModel

class AddItemFragment: BottomSheetDialogFragment() {

    private var _binding: FragmentAddItemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var shoppingListViewModel: ShoppingListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)

        shoppingListViewModel = ViewModelProvider(this)[ShoppingListViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdownMenuObserver()
    }


    private fun setupDropdownMenuObserver(){
        shoppingListViewModel.listFilters.observe(viewLifecycleOwner) { list ->
            val arrayAdapter = ArrayAdapter(requireContext(),
                R.layout.dropdown_item, list)
            binding.itemCategoryInput.setAdapter(arrayAdapter)
        }
    }


    companion object {
        const val TAG = "AddItemBottomSheetFragment"
    }

}
