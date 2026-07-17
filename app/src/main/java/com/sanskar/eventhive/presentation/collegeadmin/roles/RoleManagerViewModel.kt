package com.sanskar.eventhive.presentation.collegeadmin.roles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.model.Role
import com.sanskar.eventhive.data.repository.RoleRepository
import com.sanskar.eventhive.presentation.admin.PermissionKeys
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RoleManagerViewModel @Inject constructor(
    private val roleRepository: RoleRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {
    private val _uiState = MutableStateFlow<RoleManagerUiState>(RoleManagerUiState.Loading)
    val uiState: StateFlow<RoleManagerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var rolesJob: Job? = null

    fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = RoleManagerUiState.Error("Not signed in")
            return
        }
        viewModelScope.launch {
            val user = firestore.collection("users").document(uid).get().await()
            val collegeId = user.getString("collegeId").orEmpty()
            val systemRole = user.getString("systemRole").orEmpty()
            val isGuide = systemRole == "studentGuide"
            if (collegeId.isBlank()) {
                _uiState.value = RoleManagerUiState.Error("College not assigned")
                return@launch
            }
            rolesJob?.cancel()
            rolesJob = launch {
                roleRepository.getRolesForCollege(collegeId).collect { roles ->
                    _uiState.value = RoleManagerUiState.Success(
                        collegeId = collegeId,
                        isStudentGuide = isGuide,
                        roles = roles,
                    )
                }
            }
        }
    }

    fun createRole(name: String, permissions: Map<String, Boolean>) {
        val state = uiState.value as? RoleManagerUiState.Success ?: return
        if (!state.isStudentGuide) return
        viewModelScope.launch {
            roleRepository.createRole(
                collegeId = state.collegeId,
                role = Role(name = name.trim(), permissions = PermissionKeys.associateWith { permissions[it] == true }),
            ).onSuccess { _events.emit("Role created") }
                .onFailure { _events.emit(it.message ?: "Failed to create role") }
        }
    }

    fun updateRole(role: Role) {
        val state = uiState.value as? RoleManagerUiState.Success ?: return
        if (!state.isStudentGuide) return
        viewModelScope.launch {
            roleRepository.updateRole(state.collegeId, role)
                .onSuccess { _events.emit("Role updated") }
                .onFailure { _events.emit(it.message ?: "Failed to update role") }
        }
    }

    fun deleteRole(roleId: String) {
        val state = uiState.value as? RoleManagerUiState.Success ?: return
        if (!state.isStudentGuide) return
        viewModelScope.launch {
            roleRepository.deleteRole(state.collegeId, roleId)
                .onSuccess { _events.emit("Role deleted") }
                .onFailure { _events.emit(it.message ?: "Failed to delete role") }
        }
    }

    override fun onCleared() {
        rolesJob?.cancel()
        super.onCleared()
    }
}
