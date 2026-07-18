package com.sanskar.eventhive.data.model

import java.util.UUID

enum class RegistrationStatus { PENDING, CONFIRMED, CANCELLED ,CLAIMED}

/**
 * Combines the event‐registration intent with the actual ticket credential.
 */
data class Ticket(

    val ticketId: String = UUID.randomUUID().toString(),
    val categoryId: String = "",
    val clubId: String= "",
    val eventId: String = "",
    val userId: String = "",
    val teamId: String  = "",
    val issuedAt: Long = System.currentTimeMillis(),
    val qrCodeUrl: String? = null,
    val participantIds: List<String> = emptyList(),
    val status: RegistrationStatus = RegistrationStatus.CONFIRMED,

    val redeemedAt: com.google.firebase.Timestamp? = null,
    val valid: Boolean = true,
    val additionalInfoAskByEventOrganizer: List<AdditionalInfoAskFromUser> = emptyList(),
    val additionalInfoAnswers: Map<String, String> = emptyMap(),
)


data class Team(
    val teamId: String = UUID.randomUUID().toString(),
    val eventId: String = "",
    val eventName:String = "",
    val subEventId: String? = null,
    val subEventName: String? = null,
    val clubId: String = "",
    val categoryId: String = "",
    val teamName: String = "",
    val teamMemberIds: List<String> = emptyList(),
    val teamMemberNames: Map<String, String> = emptyMap(),
    val teamLeaderId: String = "",
    val teamScore: Int = 0,
)
