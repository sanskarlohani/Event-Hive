package com.sanskar.eventhive.data.model


import com.google.firebase.Timestamp

data class SubEvent(
    val subEventId: String = System.currentTimeMillis().toString(),

    val title: String = "",
    val description: String = "",
    val about: String? = null,
    val prizePool: String? = null,
    val perks: List<String> = emptyList(),

    val eventId: String = "",
    val clubId: String = "",
    val categoryId: String= "",

    val posterUrl: String? = null,
    val venue: String = "",

    val registrationStartTime: Timestamp? = null,
    val registrationEndTime: Timestamp? = null,

    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long = System.currentTimeMillis(),

    val organizers: List<EventOrganizer> = emptyList(),
    val ticketIds: List<String> = emptyList(),

    val sponsors: List<String> = emptyList(),

    // new fields:
    val mode: EventMode = EventMode.SINGLE,  // is it single, group, or both?
    val minTeamSize: Int = 1,                // smallest allowed signup
    val maxTeamSize: Int = 1,                // largest allowed signup

    val teamsIds: List<String> = emptyList(),

    val isOnline: Boolean = false,
    val registrationRequired: Boolean = false,

    val eventResults: List<String> = emptyList(),
    val additionalInfoDuringRegistration: List<AdditionalInfo> = emptyList(),
)
