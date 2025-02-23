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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.ext.viewBinding
import ru.practicum.android.diploma.common.presentation.FilterParametersViewModel
import ru.practicum.android.diploma.common.presentation.model.FilterParameters
import ru.practicum.android.diploma.databinding.FragmentFiltersBinding
import ru.practicum.android.diploma.filters.domain.model.Area

class FiltersFragment : Fragment(R.layout.fragment_filters) {
    private val binding by viewBinding(FragmentFiltersBinding::bind)
    private val filterParametersViewModel: FilterParametersViewModel by navGraphViewModels(R.id.root_navigation_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etSalary.setText(
            filterParametersViewModel.getFilterParametersLiveData().value?.expectedSalary?.toString().orEmpty()
        )
        filterParametersViewModel.getFilterParametersLiveData().observe(viewLifecycleOwner) {
            setParameters(it)
        }
        filterParametersViewModel.getFiltersChangedLiveData().observe(viewLifecycleOwner) {
            binding.btnApply.isVisible = it
        }
        initListeners()
    }

    private fun setParameters(filters: FilterParameters?) {
        with(binding) {
            bindPlace(filters?.region, filters?.country)
            etSelectIndustry.setText(filters?.industry?.name.orEmpty())
            cbSalary.isChecked = filters?.onlyWithSalary ?: false
            btnCancel.isVisible = filters?.industry != null ||
                filters?.country != null ||
                filters?.expectedSalary != null ||
                filters?.onlyWithSalary ?: false
        }
    }

    private fun bindPlace(region: Area?, country: Area?) {
        if (region != null) {
            binding.etSelectPlace.setText(getString(R.string.full_place, country?.name ?: "", region.name))
        } else if (country != null) {
            binding.etSelectPlace.setText(country.name)
        } else {
            binding.etSelectPlace.setText("")
        }
    }

    private fun initListeners() {
        with(binding) {
            cbSalary.setOnCheckedChangeListener { _, isChecked ->
                filterParametersViewModel.setOnlyWithSalary(isChecked)
                etSalary.clearFocus()
            }
            btnCancel.setOnClickListener {
                filterParametersViewModel.clearFilters()
                etSalary.setText("")
                etSalary.clearFocus()
            }
            btnApply.setOnClickListener {
                applyFilters()
            }
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            etSelectPlace.setOnClickListener { openPlaceSelection() }
            etSelectIndustry.setOnClickListener { openIndustrySelection() }
            initSalaryTextListener()
            etSelectPlace.addTextChangedListener(
                CustomTextWatcher(tlSelectPlace) {
                    filterParametersViewModel.setRegion(null)
                    filterParametersViewModel.setCountry(null)
                }
            )
            etSelectIndustry.addTextChangedListener(
                CustomTextWatcher(tlSelectIndustry) {
                    filterParametersViewModel.setIndustry(null)
                }
            )
        }
    }

    private fun FragmentFiltersBinding.initSalaryTextListener() {
        etSalary.addTextChangedListener(object : TextWatcher {
            private var salaryInputJob: Job? = null
            override fun afterTextChanged(s: Editable) {
                salaryInputJob?.cancel()
                salaryInputJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(SET_SALARY_DELAY_MILLIS)
                    val salary = s.toString().toIntOrNull()
                    filterParametersViewModel.setExpectedSalary(salary)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Empty
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Empty
            }
        })
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
        findNavController().popBackStack()
    }

    companion object {
        const val SET_SALARY_DELAY_MILLIS = 300L
    }

    class CustomTextWatcher(private val textInputLayout: TextInputLayout, private val onClear: (() -> Unit)? = null) :
        TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s.isNullOrEmpty()) {
                textInputLayout.setEndIconDrawable(R.drawable.btn_forward)
            } else {
                textInputLayout.setEndIconDrawable(R.drawable.ic_clear_padding_buttom)
                textInputLayout.setEndIconOnClickListener {
                    textInputLayout.editText?.text?.clear()
                    textInputLayout.editText?.clearFocus()
                    onClear?.invoke()
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Empty
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Empty
        }
    }

}
