package com.sanskar.eventhive.presentation.superadmin.collegedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.model.College
import com.sanskar.eventhive.data.model.CollegeStats
import com.sanskar.eventhive.data.repository.CollegeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollegeDetailViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CollegeDetailUiState>(CollegeDetailUiState.Loading)
    val uiState: StateFlow<CollegeDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var observeJob: Job? = null
    private var currentCollege: College? = null

    fun start(collegeId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            collegeRepository.getCollegeById(collegeId).collect { college ->
                currentCollege = college
                val stats = runCatching { collegeRepository.getCollegeStats(collegeId) }
                    .getOrDefault(CollegeStats())
                _uiState.value = CollegeDetailUiState.Success(college = college, stats = stats)
            }
        }
    }

    fun updateCode(code: String) {
        val college = currentCollege ?: return
        viewModelScope.launch {
            collegeRepository.updateCollegeCode(college.id, code)
                .onSuccess { _events.emit("College code updated") }
                .onFailure { _events.emit(it.message ?: "Failed to update code") }
        }
    }

    fun toggleActive() {
        val college = currentCollege ?: return
        viewModelScope.launch {
            collegeRepository.setCollegeActive(college.id, !college.isActive)
                .onSuccess { _events.emit(if (college.isActive) "College deactivated" else "College activated") }
                .onFailure { _events.emit(it.message ?: "Failed to update status") }
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}
