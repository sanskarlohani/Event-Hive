package com.sanskar.eventhive.presentation.collegecode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.domain.usecase.CollegeAssignResult
import com.sanskar.eventhive.domain.usecase.UserCollegeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollegeCodeEntryViewModel @Inject constructor(
    private val userCollegeUseCase: UserCollegeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CollegeCodeEntryUiState>(CollegeCodeEntryUiState.Idle)
    val uiState: StateFlow<CollegeCodeEntryUiState> = _uiState.asStateFlow()

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    fun setCode(value: String) {
        _code.value = value.uppercase().filter { it.isLetterOrDigit() }.take(6)
    }

    fun checkAutoAssign() {
        viewModelScope.launch {
            _uiState.value = CollegeCodeEntryUiState.Loading
            _uiState.value = when (val result = userCollegeUseCase.assignAfterSignup()) {
                is CollegeAssignResult.AutoAssigned -> CollegeCodeEntryUiState.Success(result.collegeId)
                is CollegeAssignResult.NeedsCode -> CollegeCodeEntryUiState.Idle
                is CollegeAssignResult.InvalidCode -> CollegeCodeEntryUiState.InvalidCode
                is CollegeAssignResult.NotRegistered -> CollegeCodeEntryUiState.Error("User not registered")
            }
        }
    }

    fun confirmCode() {
        val currentCode = code.value
        if (currentCode.length != 6) {
            _uiState.value = CollegeCodeEntryUiState.InvalidCode
            return
        }
        viewModelScope.launch {
            _uiState.value = CollegeCodeEntryUiState.Loading
            _uiState.value = when (val result = userCollegeUseCase.submitCode(currentCode)) {
                is CollegeAssignResult.AutoAssigned -> CollegeCodeEntryUiState.Success(result.collegeId)
                is CollegeAssignResult.InvalidCode -> CollegeCodeEntryUiState.InvalidCode
                is CollegeAssignResult.NeedsCode -> CollegeCodeEntryUiState.InvalidCode
                is CollegeAssignResult.NotRegistered -> CollegeCodeEntryUiState.Error("User not registered")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is CollegeCodeEntryUiState.Error || _uiState.value is CollegeCodeEntryUiState.InvalidCode) {
            _uiState.value = CollegeCodeEntryUiState.Idle
        }
    }
}
