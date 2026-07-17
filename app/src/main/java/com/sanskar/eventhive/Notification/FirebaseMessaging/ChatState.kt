package com.sanskar.eventhive.Notification.FirebaseMessaging

data class ChatState(
    val isEnteringToken: Boolean = true,
    val remoteToken: String = "",
    val messageText: String = ""
)