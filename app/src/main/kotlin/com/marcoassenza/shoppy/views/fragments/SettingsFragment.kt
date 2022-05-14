package com.marcoassenza.shoppy.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.databinding.FragmentSettingsBinding
import com.marcoassenza.shoppy.utils.NetworkStatus
import com.marcoassenza.shoppy.utils.isConnected
import com.marcoassenza.shoppy.viewmodels.ItemsViewModel
import com.marcoassenza.shoppy.views.helpers.setTopAppBarSubtitle
import com.marcoassenza.shoppy.views.helpers.showIndefiniteSnackbar
import com.marcoassenza.shoppy.views.helpers.showLongSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val itemsViewModel: ItemsViewModel by activityViewModels()
    private var networkSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUsernameEdittext()
        setupUsernameEdittextObserver()
        setupChangeUsernameButton()
        setupRemoteDatabaseStatusObserver()
        setGroceryListOverviewObserver()
        setStorageOverviewObserver()
    }

    override fun onResume() {
        super.onResume()
        hideFab()
        setupNetworkStatusObserver()
        requireActivity().setTopAppBarSubtitle(R.string.title_settings)
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
                    NetworkStatus.Unavailable -> {
                        networkSnackbar = showNoNetworkSnackBar()
                        binding.userNameInput.isEnabled = false
                    }
                    NetworkStatus.Available -> {
                        networkSnackbar?.dismiss()
                        showNetworkIsBackSnackBar()
                        binding.userNameInput.isEnabled = true
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
                binding.statusCardText.text = it.name
            }
            .launchIn(lifecycleScope)
    }

    private fun setStorageOverviewObserver() {
        itemsViewModel.storageList
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                val text = when {
                    it.isEmpty() -> getString(R.string.no_item_in_storage)
                    it.size == 1 -> "${getString(R.string.there_is)} ${it.size} ${getString(R.string.item_in_storage)}"
                    else -> "${getString(R.string.there_are)} ${it.size} ${getString(R.string.items_in_storage)}"
                }

                binding.storageCardText.text = text
            }
            .launchIn(lifecycleScope)
    }

    private fun setGroceryListOverviewObserver() {
        itemsViewModel.groceryList
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                val text = when {
                    it.isEmpty() -> getString(R.string.no_item_in_storage)
                    it.size == 1 -> "${getString(R.string.there_is)} ${it.size} ${getString(R.string.item_in_grocery_list)}"
                    else -> "${getString(R.string.there_are)} ${it.size} ${getString(R.string.items_in_grocery_list)}"
                }

                binding.groceryListCardText.text = text
            }
            .launchIn(lifecycleScope)
    }

    private fun setupUsernameEdittext() {
        binding.userNameInput.addTextChangedListener(
            onTextChanged = { text, _, _, _ ->
                Timber.d(text.toString())
            }
        )
    }

    private fun setupChangeUsernameButton() {
        binding.validateButton.setOnClickListener {
            if (requireContext().isConnected) {
                val userName = binding.userNameInput.text.toString()
                if (userName.isNotEmpty()) {
                    itemsViewModel.submitNewUsername(userName)
                }
            }
        }
    }

    private fun setupUsernameEdittextObserver() {
        itemsViewModel.username
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                binding.userNameInput.setText(it)
            }
            .launchIn(lifecycleScope)
    }

    private fun showNoNetworkSnackBar(): Snackbar? {
        networkSnackbar = binding.root.showIndefiniteSnackbar(getString(R.string.no_internet))
        return networkSnackbar
    }

    private fun showNetworkIsBackSnackBar() {
        binding.root.showLongSnackbar(getString(R.string.back_online))
    }

    private fun hideFab() {
        activity?.findViewById<ExtendedFloatingActionButton>(R.id.fab)?.hide()
    }


}
