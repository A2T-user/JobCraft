package ru.practicum.android.diploma.search.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.ext.viewBinding
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.search.presentation.SearchViewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()
    private val binding by viewBinding(FragmentSearchBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFilters.setOnClickListener {
            openFilters()
        }

        binding.btnVacancy.setOnClickListener {
            openVacancy()
        }

        viewModel.searchTest()

    }

    private fun openFilters() {
        val action = SearchFragmentDirections.actionSearchFragmentToFiltersFragment()
        findNavController().navigate(action)
    }

    private fun openVacancy() {
        val action = SearchFragmentDirections.actionSearchFragmentToVacancyFragment(123)
        findNavController().navigate(action)
    }
}
