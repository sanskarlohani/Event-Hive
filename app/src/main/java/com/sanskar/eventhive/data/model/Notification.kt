package com.sanskar.eventhive.data.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean = false,
    val deepLink: String? = null,
    val senderId: String? = null
)

enum class NotificationType {
    GENERAL, CLUB, EVENT, TICKET, CHAT
}