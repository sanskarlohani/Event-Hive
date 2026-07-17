package com.sanskar.eventhive.presentation.admin

import kotlin.random.Random

val PermissionKeys = listOf(
    "canCreateEvent",
    "canEditEvent",
    "canDeleteEvent",
    "canCreateClub",
    "canManageClubMembers",
    "canViewReports",
    "canModerateChat",
    "canSendAnnouncements",
)

fun generateCollegeCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return buildString {
        repeat(6) {
            append(chars[Random.nextInt(chars.length)])
        }
    }
}
