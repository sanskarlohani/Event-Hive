package com.sanskar.eventhive.presentation.superadmin.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.repository.CollegeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlatformAnalyticsViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PlatformAnalyticsUiState>(PlatformAnalyticsUiState.Loading)
    val uiState: StateFlow<PlatformAnalyticsUiState> = _uiState.asStateFlow()

    private val _range = MutableStateFlow(AnalyticsRange.DAYS_7)
    val range: StateFlow<AnalyticsRange> = _range.asStateFlow()

    init {
        load(AnalyticsRange.DAYS_7)
    }

    fun load(range: AnalyticsRange) {
        viewModelScope.launch {
            _range.value = range
            _uiState.value = PlatformAnalyticsUiState.Loading
            val cutoffMillis = when (range) {
                AnalyticsRange.DAYS_7 -> System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                AnalyticsRange.DAYS_30 -> System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                AnalyticsRange.ALL_TIME -> null
            }
            runCatching {
                val stats = collegeRepository.getPlatformStats(cutoffMillis)
                val rows = collegeRepository.getPerCollegeAnalytics(cutoffMillis)
                PlatformAnalyticsUiState.Success(stats = stats, rows = rows)
            }.onSuccess {
                _uiState.value = it
            }.onFailure {
                _uiState.value = PlatformAnalyticsUiState.Error(it.message ?: "Failed to load analytics")
            }
        }
    }
}
