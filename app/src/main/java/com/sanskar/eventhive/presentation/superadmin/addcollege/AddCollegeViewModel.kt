package com.sanskar.eventhive.presentation.superadmin.addcollege

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.model.College
import com.sanskar.eventhive.data.repository.CollegeRepository
import com.sanskar.eventhive.presentation.admin.generateCollegeCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCollegeViewModel @Inject constructor(
    private val collegeRepository: CollegeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddCollegeUiState>(AddCollegeUiState.Idle)
    val uiState: StateFlow<AddCollegeUiState> = _uiState.asStateFlow()

    private val _codeAvailable = MutableStateFlow<Boolean?>(null)
    val codeAvailable: StateFlow<Boolean?> = _codeAvailable.asStateFlow()

    fun autoGenerateCode(): String = generateCollegeCode()

    fun validateCode(code: String) {
        val normalized = code.trim().uppercase()
        if (normalized.length != 6) {
            _codeAvailable.value = false
            return
        }
        viewModelScope.launch {
            _codeAvailable.value = runCatching { collegeRepository.isCodeAvailable(normalized) }.getOrDefault(false)
        }
    }

    fun submit(
        name: String,
        listedCourses: List<String>,
        domain: String?,
        code: String,
        guideName: String,
        guideEmail: String,
    ) {
        viewModelScope.launch {
            _uiState.value = AddCollegeUiState.Loading
            runCatching {
                require(listedCourses.any { it.isNotBlank() }) { "Add at least one listed course" }
                val college = College(
                    name = name.trim(),
                    listedCourses = listedCourses.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
                    emailDomain = domain?.trim()?.lowercase()?.ifBlank { null },
                    collegeCode = code.trim().uppercase(),
                    studentGuideName = guideName.trim(),
                    studentGuideEmail = guideEmail.trim(),
                )
                val createdCollegeId = collegeRepository.createCollege(college).getOrThrow()
                AddCollegeUiState.Success(createdCollegeId)
            }.onSuccess {
                _uiState.value = it
            }.onFailure {
                _uiState.value = AddCollegeUiState.Error(it.message ?: "Unable to add college")
            }
        }
    }
}
