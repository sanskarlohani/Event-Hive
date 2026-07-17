package com.sanskar.eventhive.presentation.collegeadmin.reports

import androidx.compose.runtime.Immutable

@Immutable
data class CollegeAdminReportData(
    val totalStudents: Long = 0,
    val eventsThisMonth: Long = 0,
    val eventsTotal: Long = 0,
    val bookingsThisMonth: Long = 0,
    val bookingsTotal: Long = 0,
    val topClubs: List<Pair<String, Long>> = emptyList(),
)

sealed class CollegeAdminReportsUiState {
    data object Loading : CollegeAdminReportsUiState()
    data class Success(val data: CollegeAdminReportData) : CollegeAdminReportsUiState()
    data class Error(val message: String) : CollegeAdminReportsUiState()
}
