package com.sanskar.eventhive.data.repository.Implementation

import android.util.Log
import com.sanskar.eventhive.Notification.FirebaseMessaging.FcmApi
import com.sanskar.eventhive.data.model.Notification
import com.sanskar.eventhive.data.model.SendMessageDto
import com.sanskar.eventhive.data.repository.Inteface.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: FcmApi,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : NotificationRepository {
    override suspend fun sendMessage(dto: SendMessageDto) = api.sendMessage(dto)
    override suspend fun broadcast(dto: SendMessageDto) = api.broadcast(dto)
    private fun currentUid(): String? = firebaseAuth.currentUser?.uid

    override fun saveNotification(notification: Notification) {
        val uid = currentUid() ?: return

        Log.d("NotificationRepository", "Current user uid: $uid")


        firestore.collection("users")
            .document(uid)
            .collection("Notifications")
            .add(notification)
            .addOnFailureListener { e ->
                Log.e("Firestore", "Save notification failed", e)
            }
    }

    override fun getNotifications(): Flow<List<Notification>> = callbackFlow {
        val uid = currentUid() ?: return@callbackFlow

        val listener = firestore.collection("users")
            .document(uid)
            .collection("Notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening for notifications", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        Log.d("Firestore", "Notification doc: ${doc.data}")
                    }
                } else {
                    Log.d("Firestore", "No notifications")
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        Notification(
                            id = doc.getString("id").orEmpty().ifBlank { doc.id },
                            title = doc.getString("title").orEmpty(),
                            body = doc.getString("body").orEmpty(),
                            imageUrl = doc.getString("imageUrl"),
                            timestamp = (doc.get("timestamp") as? Timestamp) ?: Timestamp.now(),
                            read = doc.getBoolean("read") ?: false,
                            deepLink = doc.getString("deepLink"),
                            senderId = doc.getString("senderId")
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(notifications)
            }


        awaitClose { listener.remove() }
    }
}
