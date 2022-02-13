package com.marcoassenza.shoppy.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.databinding.FragmentInventoryBinding
import com.marcoassenza.shoppy.viewmodels.InventoryViewModel
import com.marcoassenza.shoppy.views.activities.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inventoryViewModel = ViewModelProvider(this)[InventoryViewModel::class.java]

        _binding = FragmentInventoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView: TextView = binding.textInventory
        inventoryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity ->
            MainActivity.mainFabCustomizer(
                activity,
                R.string.add_item_to_inventory,
                R.drawable.ic_baseline_add_24
            )
            {
                Toast.makeText(activity.applicationContext, "Inventory List", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}