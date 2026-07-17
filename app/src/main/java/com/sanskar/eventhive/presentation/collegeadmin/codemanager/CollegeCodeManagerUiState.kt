package com.sanskar.eventhive.presentation.collegeadmin.codemanager

sealed class CollegeCodeManagerUiState {
    data object Loading : CollegeCodeManagerUiState()
    data class Success(val collegeId: String, val code: String) : CollegeCodeManagerUiState()
    data class Error(val message: String) : CollegeCodeManagerUiState()
}
