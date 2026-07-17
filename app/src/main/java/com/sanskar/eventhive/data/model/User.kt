package com.sanskar.eventhive.data.model

import androidx.compose.runtime.Immutable

enum class AppRole { ADMIN, MEMBER }

@Immutable
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val sic: String = "",
    val registrationNo: String = "",
    val course: String = "",
    val yearOfJoining: String = "",
    val yearOfPassing: String = "",
    val collegeName: String = "",
    val phone: String? = null,
    val appRole: AppRole = AppRole.MEMBER,
    val profileImageUrl: String? = null,
    val clubs: List<UserClub> = emptyList(),
    val events: List<UserEvent> = emptyList(),
    val tickets: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long? = null,
    val isOnline: Boolean = false,
    val notificationToken: String? = null,
    val collegeId: String = "",
    val systemRole: String = "student",
    val customRoleIds: List<String> = emptyList(),
    val effectivePermissions: Map<String, Boolean> = mapOf(
        "canCreateEvent" to false,
        "canEditEvent" to false,
        "canDeleteEvent" to false,
        "canCreateClub" to false,
        "canManageClubMembers" to false,
        "canViewReports" to false,
        "canModerateChat" to false,
        "canSendAnnouncements" to false,
    ),
)

@Immutable
data class UserClub(
    val clubId: String = "",
    val categoryId: String = "",
    val role: ClubRole = ClubRole.MEMBER,
)

@Immutable
data class UserEvent(
    val categoryId: String = "",
    val clubId: String = "",
    val eventId: String = "",
)

@Immutable
data class UserTicket(
    val ticketId: String = "",
    val categoryId: String = "",
    val clubId: String = "",
    val eventId: String = "",
)









