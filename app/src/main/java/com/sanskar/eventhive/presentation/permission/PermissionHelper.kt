package com.sanskar.eventhive.presentation.permission

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionHelper @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val _permissions = MutableStateFlow(defaultPermissions())
    val permissions: StateFlow<Map<String, Boolean>> = _permissions.asStateFlow()

    fun hasPermission(key: String): Boolean = _permissions.value[key] == true

    suspend fun refresh(uid: String) {
        if (uid.isBlank()) {
            _permissions.value = defaultPermissions()
            return
        }
        val snap = firestore.collection("users").document(uid).get().await()
        val raw = snap.get("effectivePermissions") as? Map<*, *>
        if (raw == null) {
            _permissions.value = defaultPermissions()
            return
        }
        val mapped = defaultPermissions().toMutableMap()
        raw.forEach { (k, v) ->
            val key = k as? String ?: return@forEach
            mapped[key] = (v as? Boolean) == true
        }
        _permissions.value = mapped
    }

    companion object {
        fun defaultPermissions() = mapOf(
            "canCreateEvent" to false,
            "canEditEvent" to false,
            "canDeleteEvent" to false,
            "canCreateClub" to false,
            "canManageClubMembers" to false,
            "canViewReports" to false,
            "canModerateChat" to false,
            "canSendAnnouncements" to false,
        )
    }
}
