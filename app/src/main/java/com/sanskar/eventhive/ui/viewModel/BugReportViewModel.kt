package com.sanskar.eventhive.ui.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.repository.Inteface.BugReportRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BugReportViewModel @Inject constructor(
    private val repo: BugReportRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _submitState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val submitState: StateFlow<Resource<Unit>> = _submitState.asStateFlow()

    fun submit(
        title: String,
        description: String,
        appVersion: String?,
        deviceInfo: String?,
        screenshotUri: Uri?
    ) {
        viewModelScope.launch {
            _submitState.value = Resource.Loading
            val userId = auth.currentUser?.email ?: auth.currentUser?.uid ?: ""
            val res = repo.submitBug(userId, title, description, appVersion, deviceInfo, screenshotUri)
            _submitState.value = res
        }
    }

    fun reset() {
        _submitState.value = Resource.Idle
    }
}
