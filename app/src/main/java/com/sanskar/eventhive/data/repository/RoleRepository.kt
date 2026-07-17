package com.sanskar.eventhive.data.repository

import com.sanskar.eventhive.data.model.Role
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val users = firestore.collection("users")
    private val colleges = firestore.collection("colleges")

    private fun rolesCol(collegeId: String) =
        colleges.document(collegeId).collection("roles")

    suspend fun createRole(collegeId: String, role: Role): Result<Unit> = runCatching {
        val id = if (role.id.isBlank()) rolesCol(collegeId).document().id else role.id
        rolesCol(collegeId).document(id).set(role.copy(id = id)).await()
    }

    suspend fun updateRole(collegeId: String, role: Role): Result<Unit> = runCatching {
        require(role.id.isNotBlank()) { "Role id required" }
        rolesCol(collegeId).document(role.id).set(role).await()
    }

    suspend fun deleteRole(collegeId: String, roleId: String): Result<Unit> = runCatching {
        rolesCol(collegeId).document(roleId).delete().await()
    }

    fun getRolesForCollege(collegeId: String): Flow<List<Role>> = callbackFlow {
        val registration = rolesCol(collegeId)
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val value = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(Role::class.java)?.copy(id = doc.id)
                }
                trySend(value).isSuccess
            }
        awaitClose { registration.remove() }
    }

    suspend fun assignRolesToUser(
        uid: String,
        collegeId: String,
        roleIds: List<String>,
    ): Result<Unit> = runCatching {
        val normalizedRoleIds = roleIds.distinct()
        val rolesSnapshot = mutableListOf<Role>()
        normalizedRoleIds.chunked(10).forEach { chunk ->
            if (chunk.isNotEmpty()) {
                val docs = rolesCol(collegeId)
                    .whereIn("id", chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(Role::class.java) }
                rolesSnapshot.addAll(docs)
            }
        }

        val effective = mutableMapOf(
            "canCreateEvent" to false,
            "canEditEvent" to false,
            "canDeleteEvent" to false,
            "canCreateClub" to false,
            "canManageClubMembers" to false,
            "canViewReports" to false,
            "canModerateChat" to false,
            "canSendAnnouncements" to false,
        )

        for (role in rolesSnapshot) {
            role.permissions.forEach { (key, value) ->
                if (value) effective[key] = true
            }
        }

        users.document(uid).update(
            mapOf(
                "customRoleIds" to normalizedRoleIds,
                "effectivePermissions" to effective,
                "updatedAt" to Timestamp.now(),
            )
        ).await()
    }

    suspend fun getAssignedRoleIds(uid: String): List<String> {
        val snap = users.document(uid).get().await()
        return snap.get("customRoleIds") as? List<String> ?: emptyList()
    }
}
