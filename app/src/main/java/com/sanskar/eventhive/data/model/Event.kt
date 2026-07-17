package com.sanskar.eventhive.data.model

import com.google.firebase.Timestamp

enum class EventRole { ADMIN, OC, MOD, PARTICIPANT,COORDINATOR,SPEAKER }

data class EventOrganizer(
    val userId: String = "",
    val role: EventRole = EventRole.COORDINATOR,
    val clubName: String = "",
    val assignedAt: Long = System.currentTimeMillis(),
    val eventName: String = "",
)

// 1) New enum to describe your event’s participation mode
enum class EventMode {
    SINGLE,
    GROUP,
    BOTH,
}

// 2) Updated Event data class
data class Event(
    val eventId: String = System.currentTimeMillis().toString(),

    val title: String = "",
    val description: String = "",
    val about: String? = null,
    val prizePool: String? = null,
    val perks: List<String> = emptyList(),

    val clubId: String = "",
    val categoryId: String= "",

    val posterUrl: String? = null,
    val bannerUrl: String? = null,
    val venue: String = "",

    val registrationStartTime: Timestamp? = null,
    val registrationEndTime: Timestamp? = null,

    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long = System.currentTimeMillis(),

    val organizers: List<EventOrganizer> = emptyList(),

    val sponsors: List<String> = emptyList(),

    // new fields:
    val mode: EventMode = EventMode.SINGLE,  // is it single, group, or both?
    val minTeamSize: Int = 1,                // smallest allowed signup
    val maxTeamSize: Int = 1,                // largest allowed signup

    val ticketIds: List<String> = emptyList(),
    val teamsIds: List<String> = emptyList(),
    val participantsIds: List<String> = emptyList(),

    var isOnline: Boolean = false,
    var registrationRequired: Boolean = false,

    val eventResults: List<String> = emptyList(),
    var haveSubEvent: Boolean = false,
    val subEventIds: List<String> = emptyList(),
    val additionalInfo: List<AdditionalInfo> = emptyList(),
    val additionalInfoAskFromUser: List<AdditionalInfoAskFromUser> = emptyList(),
)

data class AdditionalInfo(
    val key: String = "",
    val value: String = "",
)
data class AdditionalInfoAskFromUser(
    val key: String = "",
    val value: String = "",
    var required: Boolean = false,
)

data class EventResult(
    val eventId: String,
    val subEventId: String? = null,
    val categoryId: String,
    val winnerTeamId: String,
    val runnerUpTeamIds: List<Map<String, Int>> = emptyList(), //team id with rank
    val teamScores: Map<String, Int> = emptyMap(),  // team id with score
    val recordedAt: Long = System.currentTimeMillis()
)


data class Sponsor(
    val sponsorId: String = System.currentTimeMillis().toString(),
    val name: String = "",
    val description: String? = null,
    val bannerUrl: String? = null,
    val logoUrl: String? = null,
    val websiteUrl: String? = null,
    val eventId: String = "",
    val clubId: String = "",
    val categoryId: String = "",
)

