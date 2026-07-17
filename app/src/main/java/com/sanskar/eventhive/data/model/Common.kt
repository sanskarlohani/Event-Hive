package com.sanskar.eventhive.data.model


data class EventUser(
    val userId: String = "",
    val eventId: String = "",
    val categoryId: String = "",
    val clubId: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val role: String = "member",
)

data class EventTicket(
    val userId: String = "",
    val ticketId: String = "",
    val categoryId: String = "",
    val clubId: String = "",
    val eventId: String = "",
    val generatedAt: Long = System.currentTimeMillis(),
)
