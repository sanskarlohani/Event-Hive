package com.sanskar.eventhive.data.model



data class SendMessageDto(
    val to: String? = null,
    val notification: NotificationBody,
    val data: Map<String, String> = emptyMap()
)

data class NotificationBody(
    val title: String,
    val body: String,
    val image: String? = null
)