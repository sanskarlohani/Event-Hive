package com.sanskar.eventhive.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ChatRoomMetadata(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val collegeId: String = "",
    val relatedId: String = "",
    val readOnly: Boolean = false,
    val createdAt: Long = 0L,
)
