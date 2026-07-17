package com.sanskar.eventhive.presentation.superadmin.collegelist

import androidx.compose.runtime.Immutable
import com.sanskar.eventhive.data.model.College

sealed class CollegeListUiState {
    data object Loading : CollegeListUiState()
    @Immutable
    data class Success(val items: List<College>) : CollegeListUiState()
    data class Error(val message: String) : CollegeListUiState()
}
