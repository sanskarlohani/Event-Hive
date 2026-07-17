package com.sanskar.eventhive.data.model

import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp

@Immutable
data class College(
    val id: String = "",
    val name: String = "",
    val listedCourses: List<String> = emptyList(),
    val emailDomain: String? = null,
    val collegeCode: String = "",
    val isActive: Boolean = true,
    val studentGuideUid: String = "",
    val studentGuideName: String = "",
    val studentGuideEmail: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
)
