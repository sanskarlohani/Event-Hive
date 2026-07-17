package com.sanskar.eventhive.presentation.collegeadmin.dashboard

import androidx.compose.runtime.Immutable
import com.sanskar.eventhive.data.model.Event

sealed class CollegeAdminDashboardUiState {
    data object Loading : CollegeAdminDashboardUiState()
    @Immutable
    data class Success(
        val membersCount: Long,
        val upcomingEventsCount: Long,
        val activeClubsCount: Long,
        val recentEvents: List<Event>,
    ) : CollegeAdminDashboardUiState()
    data class Error(val message: String) : CollegeAdminDashboardUiState()
}
