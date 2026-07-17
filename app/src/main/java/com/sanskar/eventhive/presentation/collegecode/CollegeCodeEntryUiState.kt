package com.sanskar.eventhive.presentation.collegecode

sealed class CollegeCodeEntryUiState {
    data object Idle : CollegeCodeEntryUiState()
    data object Loading : CollegeCodeEntryUiState()
    data class Success(val collegeId: String) : CollegeCodeEntryUiState()
    data object InvalidCode : CollegeCodeEntryUiState()
    data class Error(val message: String) : CollegeCodeEntryUiState()
}
