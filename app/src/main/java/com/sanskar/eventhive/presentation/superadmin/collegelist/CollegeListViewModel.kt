package com.sanskar.eventhive.presentation.superadmin.collegelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.model.College
import com.sanskar.eventhive.data.repository.CollegeRepository
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollegeListViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CollegeListUiState>(CollegeListUiState.Loading)
    val uiState: StateFlow<CollegeListUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var lastDoc: DocumentSnapshot? = null
    private var exhausted = false
    private val items = mutableListOf<College>()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            exhausted = false
            lastDoc = null
            items.clear()
            runCatching { collegeRepository.getCollegesPage(pageSize = 20, lastDocument = null) }
                .onSuccess { (page, cursor) ->
                    items.addAll(page)
                    lastDoc = cursor
                    exhausted = page.size < 20
                    _uiState.value = CollegeListUiState.Success(items.toList())
                }
                .onFailure { _uiState.value = CollegeListUiState.Error(it.message ?: "Failed to load colleges") }
            _isRefreshing.value = false
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || exhausted) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            runCatching { collegeRepository.getCollegesPage(pageSize = 20, lastDocument = lastDoc) }
                .onSuccess { (page, cursor) ->
                    items.addAll(page)
                    lastDoc = cursor
                    exhausted = page.size < 20
                    _uiState.value = CollegeListUiState.Success(items.toList())
                }
                .onFailure { _uiState.value = CollegeListUiState.Error(it.message ?: "Failed to load more colleges") }
            _isLoadingMore.value = false
        }
    }
}
