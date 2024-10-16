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

class FiltersFragment : Fragment(R.layout.fragment_filters) {
    private val binding by viewBinding(FragmentFiltersBinding::bind)

    private var isClickAllowed = false
    private var openFromSearch = true

    // добавляем общую для всего графа навигации ViewModel
    private val filterParametersViewModel: FilterParametersViewModel by navGraphViewModels(R.id.root_navigation_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

        if (openFromSearch){
            filterParametersViewModel.getSavedParameters()
            openFromSearch = false
        }

        filterParametersViewModel.getFilterParametersLiveData().observe(viewLifecycleOwner) {
            renderState(it)
        }

    }

    private fun renderState(state: FilterParameters) {
        with(binding) {
            if (state.country != null){
                etSelectPlace.setText(getString(R.string.full_place, state.country, state.region))
            }
            if (state.expectedSalary != null){
                etSalary.setText(state.expectedSalary.toString())
            }
            if (state.industry != null) {
                etSelectIndustry.setText(state.industry.name)
            }
            cbSalary.isChecked = state.onlyWithSalary
            btnCancel.isVisible = state.industry != null ||
                state.country != null ||
                state.expectedSalary != null ||
                state.onlyWithSalary
        }

    }

    private fun initListeners() {
        binding.cbSalary.setOnCheckedChangeListener { _, isChecked ->
            filterParametersViewModel.setOnlyWithSalary(isChecked)
            binding.btnCancel.isVisible = true
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
            openFromSearch = true
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
                if (saveDebounce()) {
                    if (s.toString().isNotEmpty()) {
                        filterParametersViewModel.setExpectedSalary(s.toString().toInt())
                    } else {
                        filterParametersViewModel.setExpectedSalary(null)
                    }
                }
            }
        })
    }

    private fun setEmptyFilters() {
        renderState(FilterParameters(null, null, null, null))
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
        openFromSearch = true
        with(filterParametersViewModel){
            if (binding.etSalary.text.toString().isNotEmpty()){
                setExpectedSalary(binding.etSalary.text.toString().toInt())
            }
            applyFilters()
        }
        // Можно делать и вот так:
        // filterParametersViewMode.setFilterParameters(parameters)
        // это обновит лайвдату для всего графа навигации, где используется FilterParametersViewModel
        findNavController().popBackStack()
    }

    private fun saveDebounce(): Boolean {
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
