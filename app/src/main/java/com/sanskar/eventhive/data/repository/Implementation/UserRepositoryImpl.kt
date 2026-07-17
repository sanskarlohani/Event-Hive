package com.sanskar.eventhive.data.repository.Implementation

import androidx.core.net.toUri
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.UserClub
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import com.sanskar.eventhive.data.repository.toSafeUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
) : UserRepository {

    private val usersCol = firebaseFirestore.collection("users")

    // One-time suspend operations
    override suspend fun saveUser(user: User): Resource<Unit> = try {
        val uid = user.userId.ifBlank { firebaseAuth.currentUser?.uid ?: user.email }
        val payload = uploadProfileImageIfNeeded(user.copy(userId = uid), uid)
        usersCol.document(uid).set(payload).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getUser(userId: String): Resource<User?> = try {
        val user = usersCol.document(userId).get().await().toSafeUser()
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun updateUser(user: User): Resource<Unit> = try {
        val uid = user.userId.ifBlank { firebaseAuth.currentUser?.uid ?: user.email }
        val payload = uploadProfileImageIfNeeded(user.copy(userId = uid), uid)
        usersCol.document(uid).set(payload).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun deleteUser(userId: String): Resource<Unit> = try {
        usersCol.document(userId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getAllUsers(): Resource<List<User>> = try {
        val users = usersCol.get().await().documents.mapNotNull { it.toSafeUser() }
        Resource.Success(users)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getCurrentUser(): User? = try {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        usersCol.document(uid).get().await().toSafeUser()
    } catch (_: Exception) {
        null
    }

    override suspend fun joinClubForUser(userId: String, categoryId: String, clubId: String): Resource<Unit> {
        return try {

            val userClub = UserClub(
                clubId = clubId,
                categoryId = categoryId,
            )
            usersCol.document(userId)
                .collection("UserClubs")
                .document(clubId)
                .set(userClub)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
    override suspend fun leaveClubForUser(userId: String, categoryId: String, clubId: String): Resource<Unit> {
        return try {

            usersCol.document(userId)
                .collection("UserClubs")
                .document(clubId)
                .delete()
                .await()

            Resource.Success(Unit)
            } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun issueTicketForUser(userId: String, categoryId: String, clubId: String, eventId: String,ticketId:String): Resource<Unit> {
        return try {
            val userRef = usersCol.document(userId)

            userRef.update("tickets", FieldValue.arrayUnion(ticketId)).await()
            Resource.Success(Unit)

        }
        catch (e: Exception) {
            Resource.Error(e)
        }
    }



    // Real-time streams
    override fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val registration: ListenerRegistration = usersCol.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            snap?.let { trySend(it.documents.mapNotNull { doc -> doc.toSafeUser() }).isSuccess }
        }
        awaitClose { registration.remove() }
    }

    override fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val registration: ListenerRegistration = usersCol.document(userId)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val user = snap?.toSafeUser()
                trySend(user).isSuccess
            }
        awaitClose { registration.remove() }
    }

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val currentUid = firebaseAuth.currentUser?.uid
        if (currentUid == null) {
            trySend(null).isSuccess
            close()
        } else {
            val registration: ListenerRegistration = usersCol.document(currentUid)
                .addSnapshotListener { snap, err ->
                    if (err != null) { close(err); return@addSnapshotListener }
                    val user = snap?.toSafeUser()
                    trySend(user).isSuccess
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun cancelTicketForUser(userId: String, ticketId: String): Resource<Unit> {
        return try {
            firebaseFirestore
                .collection("users")
                .document(userId)
                .update("tickets", FieldValue.arrayRemove(ticketId))
                .await()
            Resource.Success(Unit)
        } catch (e:Exception) {
            Resource.Error(e)
        }

    }

    override fun observeJoinedClubsForUser(userId: String): Flow<List<UserClub>> = callbackFlow {
        val registration: ListenerRegistration = usersCol.document(userId)
            .collection("UserClubs")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(UserClub::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }

    private suspend fun uploadProfileImageIfNeeded(user: User, uid: String): User {
        val image = user.profileImageUrl.orEmpty()
        if (image.isBlank()) return user.copy(profileImageUrl = null)
        if (!image.startsWith("content://") && !image.startsWith("file://")) return user

        val ref = firebaseStorage.reference.child("profile_images/$uid/${System.currentTimeMillis()}")
        ref.putFile(image.toUri()).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        return user.copy(profileImageUrl = downloadUrl)
    }
}
