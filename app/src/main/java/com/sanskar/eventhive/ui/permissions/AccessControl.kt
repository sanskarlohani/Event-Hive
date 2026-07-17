package com.sanskar.eventhive.ui.permissions

import com.sanskar.eventhive.data.model.User

fun normalizeSystemRole(rawRole: String?): String {
    return when (rawRole?.trim()?.lowercase()) {
        "superadmin", "super_admin", "super user", "superuser" -> "superAdmin"
        "studentguide", "student_guide", "student guide" -> "studentGuide"
        else -> "student"
    }
}

fun hasPermission(user: User?, key: String): Boolean {
    return user?.effectivePermissions?.get(key) == true
}

fun hasAnyEffectivePermission(user: User?): Boolean {
    return user?.effectivePermissions?.values?.any { it } == true
}

fun canAccessCollegeAdmin(user: User?): Boolean {
    val role = normalizeSystemRole(user?.systemRole)
    return role == "studentGuide" || role == "superAdmin" || hasAnyEffectivePermission(user)
}

fun canManageCategories(user: User?): Boolean {
    val role = normalizeSystemRole(user?.systemRole)
    return role == "studentGuide" || role == "superAdmin" || hasPermission(user, "canCreateClub")
}

fun canCreateClub(user: User?): Boolean {
    val role = normalizeSystemRole(user?.systemRole)
    return role == "studentGuide" || role == "superAdmin" || hasPermission(user, "canCreateClub")
}

fun canManageClubMembers(user: User?): Boolean {
    val role = normalizeSystemRole(user?.systemRole)
    return role == "studentGuide" || role == "superAdmin" || hasPermission(user, "canManageClubMembers")
}

fun canCreateEvent(user: User?): Boolean {
    val role = normalizeSystemRole(user?.systemRole)
    return role == "studentGuide" || role == "superAdmin" ||
        hasPermission(user, "canCreateEvent") || hasPermission(user, "canEditEvent")
}

fun canEditEvent(user: User?): Boolean {
    val role = normalizeSystemRole(user?.systemRole)
    return role == "studentGuide" || role == "superAdmin" ||
        hasPermission(user, "canEditEvent") || hasPermission(user, "canCreateEvent")
}
