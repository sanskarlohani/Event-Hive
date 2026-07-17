package com.sanskar.eventhive.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ChatRoomPreview(
    val id: String = "",
    val roomId: String = "",
    val type: String = "",
    val name: String = "",
    val relatedId: String = "",
    val readOnly: Boolean = false,
    val lastMessage: String = "",
    val timestamp: Long = 0L,
)
