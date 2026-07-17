package com.sanskar.eventhive.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.model.College
import com.sanskar.eventhive.data.repository.CollegeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpFormViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
) : ViewModel() {
    private val _colleges = MutableStateFlow<List<College>>(emptyList())
    val colleges: StateFlow<List<College>> = _colleges.asStateFlow()

    init {
        viewModelScope.launch {
            collegeRepository.getAllColleges().collect { items ->
                _colleges.value = items.filter { it.isActive }.sortedBy { it.name.lowercase() }
            }
        }
    }
}
