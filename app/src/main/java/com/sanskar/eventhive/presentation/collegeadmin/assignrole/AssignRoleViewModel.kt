package com.sanskar.eventhive.presentation.collegeadmin.assignrole

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.repository.RoleRepository
import com.sanskar.eventhive.presentation.permission.PermissionHelper
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
class AssignRoleViewModel @Inject constructor(
    private val roleRepository: RoleRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val permissionHelper: PermissionHelper,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AssignRoleUiState>(AssignRoleUiState.Loading)
    val uiState: StateFlow<AssignRoleUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var targetUid: String = ""
    private var roleJob: Job? = null

    fun load(uid: String) {
        targetUid = uid
        viewModelScope.launch {
            _uiState.value = AssignRoleUiState.Loading
            val currentUid = auth.currentUser?.uid ?: run {
                _uiState.value = AssignRoleUiState.Error("Not signed in")
                return@launch
            }
            val collegeId = firestore.collection("users").document(currentUid).get().await().getString("collegeId").orEmpty()
            if (collegeId.isBlank()) {
                _uiState.value = AssignRoleUiState.Error("College not assigned")
                return@launch
            }
            val selected = roleRepository.getAssignedRoleIds(uid).toSet()
            roleJob?.cancel()
            roleJob = launch {
                roleRepository.getRolesForCollege(collegeId).collect { roles ->
                    _uiState.value = AssignRoleUiState.Success(
                        collegeId = collegeId,
                        roles = roles,
                        selectedRoleIds = selected,
                    )
                }
            }
        }
    }

    fun toggle(roleId: String) {
        val state = uiState.value as? AssignRoleUiState.Success ?: return
        val mutable = state.selectedRoleIds.toMutableSet()
        if (!mutable.add(roleId)) mutable.remove(roleId)
        _uiState.value = state.copy(selectedRoleIds = mutable)
    }

    fun save() {
        val state = uiState.value as? AssignRoleUiState.Success ?: return
        viewModelScope.launch {
            roleRepository.assignRolesToUser(targetUid, state.collegeId, state.selectedRoleIds.toList())
                .onSuccess {
                    if (targetUid == auth.currentUser?.uid) {
                        permissionHelper.refresh(targetUid)
                    }
                    _events.emit("Roles updated")
                }
                .onFailure { _events.emit(it.message ?: "Failed to assign roles") }
        }
    }

    override fun onCleared() {
        roleJob?.cancel()
        super.onCleared()
    }
}
