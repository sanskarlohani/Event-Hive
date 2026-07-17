package com.sanskar.eventhive.presentation.collegeadmin.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.repository.CollegeRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollegeAdminReportsViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CollegeAdminReportsUiState>(CollegeAdminReportsUiState.Loading)
    val uiState: StateFlow<CollegeAdminReportsUiState> = _uiState.asStateFlow()

    fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = CollegeAdminReportsUiState.Error("Not signed in")
            return
        }
        viewModelScope.launch {
            _uiState.value = CollegeAdminReportsUiState.Loading
            runCatching {
                val collegeId = collegeRepository.getUserCollegeId(uid)
                val stats = collegeRepository.getCollegeStats(collegeId)
                val (eventsMonth, eventsTotal) = collegeRepository.getMonthlyAndTotalEventCount(collegeId)
                val (bookingsMonth, bookingsTotal) = collegeRepository.getMonthlyAndTotalBookingCount(collegeId)
                val topClubs = collegeRepository.getTopClubsForCollege(collegeId, topN = 5)
                CollegeAdminReportData(
                    totalStudents = stats.totalStudents,
                    eventsThisMonth = eventsMonth,
                    eventsTotal = eventsTotal,
                    bookingsThisMonth = bookingsMonth,
                    bookingsTotal = bookingsTotal,
                    topClubs = topClubs,
                )
            }.onSuccess {
                _uiState.value = CollegeAdminReportsUiState.Success(it)
            }.onFailure {
                _uiState.value = CollegeAdminReportsUiState.Error(it.message ?: "Failed to load reports")
            }
        }
    }
}
