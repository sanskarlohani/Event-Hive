package com.sanskar.eventhive.presentation.collegeadmin.codemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.repository.CollegeRepository
import com.sanskar.eventhive.presentation.admin.generateCollegeCode
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollegeCodeManagerViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CollegeCodeManagerUiState>(CollegeCodeManagerUiState.Loading)
    val uiState: StateFlow<CollegeCodeManagerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = CollegeCodeManagerUiState.Error("Not signed in")
            return
        }
        viewModelScope.launch {
            val collegeId = collegeRepository.getUserCollegeId(uid)
            if (collegeId.isBlank()) {
                _uiState.value = CollegeCodeManagerUiState.Error("College not assigned")
                return@launch
            }
            collegeRepository.getCollegeById(collegeId).collect { college ->
                _uiState.value = CollegeCodeManagerUiState.Success(collegeId = college.id, code = college.collegeCode)
            }
        }
    }

    fun autoGenerate(): String = generateCollegeCode()

    fun updateCode(code: String) {
        val state = uiState.value as? CollegeCodeManagerUiState.Success ?: return
        viewModelScope.launch {
            val normalized = code.trim().uppercase()
            val available = collegeRepository.isCodeAvailable(normalized)
            if (!available && normalized != state.code) {
                _events.emit("Code already taken")
                return@launch
            }
            collegeRepository.updateCollegeCode(state.collegeId, normalized)
                .onSuccess {
                    _events.emit("College code updated. Share this with students.")
                }
                .onFailure { _events.emit(it.message ?: "Failed to update code") }
        }
    }
}
