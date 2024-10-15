package ru.practicum.android.diploma.filters.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.ext.viewBinding
import ru.practicum.android.diploma.common.presentation.FilterParametersViewModel
import ru.practicum.android.diploma.databinding.FragmentFiltersBinding
import ru.practicum.android.diploma.filters.domain.model.FilterParameters
import ru.practicum.android.diploma.filters.presentation.FiltersViewModel
import ru.practicum.android.diploma.filters.presentation.states.FiltersState
import kotlin.math.ceil

class FiltersFragment : Fragment(R.layout.fragment_filters) {
    private val binding by viewBinding(FragmentFiltersBinding::bind)
    private val viewModel: FiltersViewModel by viewModel()

    private var isClickAllowed = true

    init {
        viewModel.getFilters()
    }

    // добавляем общую для всего графа навигации ViewModel
    private val filterParametersViewModel: FilterParametersViewModel by navGraphViewModels(R.id.root_navigation_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        viewModel.getStateLiveData().observe(viewLifecycleOwner) {
            renderState(it)
        }
    }

    private fun renderState(state: FiltersState) {
        with(binding) {
            etSelectPlace.setText(getString(R.string.full_place, state.country, state.region))
            etSelectIndustry.setText(state.industry)
            etSalary.setText(state.minSalary)
            cbSalary.isChecked = state.isOnlyWithSalary
            btnCancel.isVisible = !state.isEmpty
        }

    }

    private fun initListeners() {
        binding.cbSalary.setOnCheckedChangeListener { _, isChecked ->
            filterParametersViewModel.setOnlyWithSalary(isChecked)
        }
        binding.btnCancel.setOnClickListener {
            setEmptyFilters()
            filterParametersViewModel.setFilterParametersLiveData(
                FilterParameters(
                    null,
                    null,
                    null,
                    null
                )
            )
        }
        binding.btnApply.setOnClickListener {
            applyFilters()
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.etSelectPlace.setOnClickListener {
            openPlaceSelection()
        }
        binding.etSelectIndustry.setOnClickListener {
            openIndustrySelection()
        }
        binding.etSalary.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // Empty
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Empty
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (searchDebounce()) {
                    if (s.toString().isNotEmpty()) {
                        filterParametersViewModel.setExpectedSalary(ceil(s.toString().toDouble()).toInt())
                    } else {
                        filterParametersViewModel.setExpectedSalary(null)
                    }
                }
            }
        })
    }

    private fun setEmptyFilters() {
        renderState(FiltersState("", "", "", false, "", true))
    }

    private fun openPlaceSelection() {
        val action = FiltersFragmentDirections.actionFiltersFragmentToSelectPlaceFragment()
        findNavController().navigate(action)
    }

    private fun openIndustrySelection() {
        val action = FiltersFragmentDirections.actionFiltersFragmentToSelectIndustryFragment()
        findNavController().navigate(action)
    }

    private fun applyFilters() {
        filterParametersViewModel.applyFilters()
        // Можно делать и вот так:
        // filterParametersViewMode.setFilterParameters(parameters)
        // это обновит лайвдату для всего графа навигации, где используется FilterParametersViewModel
        findNavController().popBackStack()
    }

    private fun searchDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewLifecycleOwner.lifecycleScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY_MILLIS)
                isClickAllowed = true
            }
        }
        return current
    }

    companion object {
        const val SEARCH_DEBOUNCE_DELAY_MILLIS = 2000L
    }

}
