package com.sanskar.eventhive.presentation.superadmin.collegedetail

import androidx.compose.runtime.Immutable
import com.sanskar.eventhive.data.model.College
import com.sanskar.eventhive.data.model.CollegeStats

sealed class CollegeDetailUiState {
    data object Loading : CollegeDetailUiState()
    @Immutable
    data class Success(
        val college: College,
        val stats: CollegeStats,
    ) : CollegeDetailUiState()
    data class Error(val message: String) : CollegeDetailUiState()
}
