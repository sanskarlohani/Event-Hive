package com.sanskar.eventhive.presentation.collegeadmin.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.repository.CollegeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CollegeMemberListViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CollegeMemberListUiState>(CollegeMemberListUiState.Loading)
    val uiState: StateFlow<CollegeMemberListUiState> = _uiState.asStateFlow()

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val loaded = mutableListOf<User>()
    private var lastDoc: DocumentSnapshot? = null
    private var hasMore = true
    private var collegeId: String = ""

    fun setSearch(value: String) {
        _search.value = value
        emitFiltered()
    }

    fun loadInitial(explicitCollegeId: String?) {
        viewModelScope.launch {
            _uiState.value = CollegeMemberListUiState.Loading
            val resolvedCollegeId = explicitCollegeId?.takeIf { it.isNotBlank() } ?: resolveCurrentCollegeId()
            if (resolvedCollegeId.isBlank()) {
                _uiState.value = CollegeMemberListUiState.Error("College not found")
                return@launch
            }
            collegeId = resolvedCollegeId
            loaded.clear()
            lastDoc = null
            hasMore = true
            runCatching { collegeRepository.getCollegeMembersPage(collegeId = collegeId, pageSize = 20, lastDocument = null) }
                .onSuccess { (members, cursor) ->
                    loaded.addAll(members)
                    lastDoc = cursor
                    hasMore = members.size == 20
                    emitFiltered()
                }
                .onFailure {
                    _uiState.value = CollegeMemberListUiState.Error(it.message ?: "Failed to load members")
                }
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || !hasMore || collegeId.isBlank()) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            runCatching { collegeRepository.getCollegeMembersPage(collegeId = collegeId, pageSize = 20, lastDocument = lastDoc) }
                .onSuccess { (members, cursor) ->
                    loaded.addAll(members)
                    lastDoc = cursor
                    hasMore = members.size == 20
                    emitFiltered()
                }
                .onFailure {
                    _uiState.value = CollegeMemberListUiState.Error(it.message ?: "Failed to load more members")
                }
            _isLoadingMore.value = false
        }
    }

    private suspend fun resolveCurrentCollegeId(): String {
        val uid = auth.currentUser?.uid ?: return ""
        val snap = firestore.collection("users").document(uid).get().await()
        return snap.getString("collegeId").orEmpty()
    }

    private fun emitFiltered() {
        val query = search.value.trim().lowercase()
        val filtered = if (query.isBlank()) loaded.toList() else loaded.filter {
            it.name.lowercase().contains(query)
        }
        _uiState.value = CollegeMemberListUiState.Success(filtered)
    }
}
