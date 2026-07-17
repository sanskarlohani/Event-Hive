package com.sanskar.eventhive.data.repository

import com.sanskar.eventhive.data.model.AppRole
import com.sanskar.eventhive.data.model.ClubRole
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.model.UserClub
import com.sanskar.eventhive.data.model.UserEvent
import com.google.firebase.firestore.DocumentSnapshot

private fun Any?.asString(): String = when (this) {
    null -> ""
    is String -> this
    else -> toString()
}

private fun Any?.asNullableString(): String? = when (this) {
    null -> null
    is String -> this.ifBlank { null }
    else -> toString().ifBlank { null }
}

private fun Any?.asLongOrNull(): Long? = when (this) {
    null -> null
    is Long -> this
    is Int -> this.toLong()
    is Double -> this.toLong()
    is Float -> this.toLong()
    is String -> this.toLongOrNull()
    else -> null
}

private fun Any?.asBoolean(default: Boolean = false): Boolean = when (this) {
    is Boolean -> this
    is String -> this.equals("true", ignoreCase = true)
    else -> default
}

private fun parseClubs(raw: Any?): List<UserClub> {
    val list = raw as? List<*> ?: return emptyList()
    return list.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        val roleRaw = map["role"].asString()
        val role = runCatching { ClubRole.valueOf(roleRaw) }.getOrDefault(ClubRole.MEMBER)
        UserClub(
            clubId = map["clubId"].asString(),
            categoryId = map["categoryId"].asString(),
            role = role,
        )
    }
}

private fun parseEvents(raw: Any?): List<UserEvent> {
    val list = raw as? List<*> ?: return emptyList()
    return list.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        UserEvent(
            categoryId = map["categoryId"].asString(),
            clubId = map["clubId"].asString(),
            eventId = map["eventId"].asString(),
        )
    }
}

private fun parseTickets(raw: Any?): List<String> {
    val list = raw as? List<*> ?: return emptyList()
    return list.mapNotNull { it as? String }
}

private fun parseCustomRoleIds(raw: Any?): List<String> {
    val list = raw as? List<*> ?: return emptyList()
    return list.mapNotNull { it as? String }
}

private fun parseEffectivePermissions(raw: Any?): Map<String, Boolean> {
    val defaults = mapOf(
        "canCreateEvent" to false,
        "canEditEvent" to false,
        "canDeleteEvent" to false,
        "canCreateClub" to false,
        "canManageClubMembers" to false,
        "canViewReports" to false,
        "canModerateChat" to false,
        "canSendAnnouncements" to false,
    )
    val map = raw as? Map<*, *> ?: return defaults
    return defaults.mapValues { (key, fallback) -> map[key].asBoolean(fallback) }
}

fun DocumentSnapshot.toSafeUser(): User? {
    if (!exists()) return null
    val data = data ?: return null

    val appRole = runCatching { AppRole.valueOf(data["appRole"].asString()) }.getOrDefault(AppRole.MEMBER)

    return User(
        userId = data["userId"].asString().ifBlank { id },
        name = data["name"].asString(),
        email = data["email"].asString(),
        sic = data["sic"].asString(),
        registrationNo = data["registrationNo"].asString(),
        course = data["course"].asString(),
        yearOfJoining = data["yearOfJoining"].asString(),
        yearOfPassing = data["yearOfPassing"].asString(),
        collegeName = data["collegeName"].asString(),
        phone = data["phone"].asNullableString(),
        appRole = appRole,
        profileImageUrl = data["profileImageUrl"].asNullableString(),
        clubs = parseClubs(data["clubs"]),
        events = parseEvents(data["events"]),
        tickets = parseTickets(data["tickets"]),
        createdAt = data["createdAt"].asLongOrNull() ?: System.currentTimeMillis(),
        lastSeenAt = data["lastSeenAt"].asLongOrNull(),
        isOnline = data["isOnline"].asBoolean(false),
        notificationToken = data["notificationToken"].asNullableString(),
        collegeId = data["collegeId"].asString(),
        systemRole = data["systemRole"].asString().ifBlank { "student" },
        customRoleIds = parseCustomRoleIds(data["customRoleIds"]),
        effectivePermissions = parseEffectivePermissions(data["effectivePermissions"]),
    )
}
