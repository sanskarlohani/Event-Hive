package com.sanskar.eventhive.presentation.collegeadmin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.repository.CollegeRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollegeAdminDashboardViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CollegeAdminDashboardUiState>(CollegeAdminDashboardUiState.Loading)
    val uiState: StateFlow<CollegeAdminDashboardUiState> = _uiState.asStateFlow()

    private var recentEventsJob: Job? = null
    private var collegeId: String = ""

    fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = CollegeAdminDashboardUiState.Error("Not signed in")
            return
        }
        viewModelScope.launch {
            collegeId = collegeRepository.getUserCollegeId(uid)
            if (collegeId.isBlank()) {
                _uiState.value = CollegeAdminDashboardUiState.Error("College not assigned")
                return@launch
            }
            val stats = runCatching { collegeRepository.getCollegeStats(collegeId) }.getOrElse {
                _uiState.value = CollegeAdminDashboardUiState.Error(it.message ?: "Failed to load stats")
                return@launch
            }
            recentEventsJob?.cancel()
            recentEventsJob = launch {
                collegeRepository.getRecentEventsForCollege(collegeId).collect { events ->
                    _uiState.value = CollegeAdminDashboardUiState.Success(
                        membersCount = stats.totalStudents,
                        upcomingEventsCount = stats.totalEvents,
                        activeClubsCount = stats.totalClubs,
                        recentEvents = events,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        recentEventsJob?.cancel()
        super.onCleared()
    }
}
