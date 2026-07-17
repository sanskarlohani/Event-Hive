package com.sanskar.eventhive.presentation.collegeadmin.members

import androidx.compose.runtime.Immutable
import com.sanskar.eventhive.data.model.User

sealed class CollegeMemberListUiState {
    data object Loading : CollegeMemberListUiState()
    @Immutable
    data class Success(val items: List<User>) : CollegeMemberListUiState()
    data class Error(val message: String) : CollegeMemberListUiState()
}
