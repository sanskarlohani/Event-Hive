package com.sanskar.eventhive.presentation.collegeadmin.assignrole

import androidx.compose.runtime.Immutable
import com.sanskar.eventhive.data.model.Role

sealed class AssignRoleUiState {
    data object Loading : AssignRoleUiState()
    @Immutable
    data class Success(
        val collegeId: String,
        val roles: List<Role>,
        val selectedRoleIds: Set<String>,
    ) : AssignRoleUiState()
    data class Error(val message: String) : AssignRoleUiState()
}
