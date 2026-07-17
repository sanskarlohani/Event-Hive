package com.sanskar.eventhive.presentation.collegeadmin.roles

import androidx.compose.runtime.Immutable
import com.sanskar.eventhive.data.model.Role

sealed class RoleManagerUiState {
    data object Loading : RoleManagerUiState()
    @Immutable
    data class Success(
        val collegeId: String,
        val isStudentGuide: Boolean,
        val roles: List<Role>,
    ) : RoleManagerUiState()
    data class Error(val message: String) : RoleManagerUiState()
}
