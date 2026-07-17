package com.sanskar.eventhive.presentation.superadmin.addcollege

sealed class AddCollegeUiState {
    data object Loading : AddCollegeUiState()
    data class Success(val collegeId: String) : AddCollegeUiState()
    data class Error(val message: String) : AddCollegeUiState()
    data object Idle : AddCollegeUiState()
}
