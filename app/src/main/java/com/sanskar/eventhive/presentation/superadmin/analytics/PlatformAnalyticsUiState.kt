package com.sanskar.eventhive.presentation.superadmin.analytics

import androidx.compose.runtime.Immutable
import com.sanskar.eventhive.data.model.CollegeAnalyticsRow
import com.sanskar.eventhive.data.model.PlatformStats

sealed class PlatformAnalyticsUiState {
    data object Loading : PlatformAnalyticsUiState()
    @Immutable
    data class Success(
        val stats: PlatformStats,
        val rows: List<CollegeAnalyticsRow>,
    ) : PlatformAnalyticsUiState()
    data class Error(val message: String) : PlatformAnalyticsUiState()
}

enum class AnalyticsRange {
    DAYS_7,
    DAYS_30,
    ALL_TIME,
}
