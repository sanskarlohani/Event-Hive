package com.sanskar.eventhive.domain.usecase

import com.sanskar.eventhive.data.repository.CollegeRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

sealed class CollegeAssignResult {
    data class AutoAssigned(val collegeId: String) : CollegeAssignResult()
    data object NeedsCode : CollegeAssignResult()
    data object InvalidCode : CollegeAssignResult()
    data object NotRegistered : CollegeAssignResult()
}

class UserCollegeUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val collegeRepository: CollegeRepository,
) {
    suspend fun assignAfterSignup(): CollegeAssignResult {
        val user = auth.currentUser ?: return CollegeAssignResult.NotRegistered
        val existingCollegeId = runCatching { collegeRepository.getUserCollegeId(user.uid) }.getOrDefault("")
        if (existingCollegeId.isNotBlank()) {
            return CollegeAssignResult.AutoAssigned(existingCollegeId)
        }
        val email = user.email ?: return CollegeAssignResult.NotRegistered
        val domain = email.substringAfter("@", "").lowercase()
        if (domain.isBlank()) return CollegeAssignResult.NeedsCode

        val college = collegeRepository.getCollegeByDomain(domain)
        return if (college != null) {
            collegeRepository.assignUserCollege(user.uid, college.id)
                .fold(
                    onSuccess = { CollegeAssignResult.AutoAssigned(college.id) },
                    onFailure = { CollegeAssignResult.NeedsCode },
                )
        } else {
            CollegeAssignResult.NeedsCode
        }
    }

    suspend fun submitCode(code: String): CollegeAssignResult {
        val user = auth.currentUser ?: return CollegeAssignResult.NotRegistered
        val college = collegeRepository.getCollegeByCode(code.trim().uppercase()) ?: return CollegeAssignResult.InvalidCode
        return collegeRepository.assignUserCollege(user.uid, college.id)
            .fold(
                onSuccess = { CollegeAssignResult.AutoAssigned(college.id) },
                onFailure = { CollegeAssignResult.InvalidCode },
            )
    }
}
