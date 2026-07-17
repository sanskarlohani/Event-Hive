package com.sanskar.eventhive.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class RtdbChatMessage(
    val id: String = "",
    val senderId: String = "",
    val displayName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val deletedAt: Long? = null,
)
