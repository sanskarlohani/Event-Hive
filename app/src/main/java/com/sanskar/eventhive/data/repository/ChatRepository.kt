package com.sanskar.eventhive.data.repository

import com.sanskar.eventhive.data.model.RtdbChatMessage
import com.sanskar.eventhive.data.model.ChatRoomMetadata
import com.sanskar.eventhive.data.model.ChatRoomPreview
import com.sanskar.eventhive.presentation.permission.PermissionHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val firestore: FirebaseFirestore,
    private val permissionHelper: PermissionHelper,
) {
    private fun currentUid(): String = auth.currentUser?.uid.orEmpty()

    suspend fun getOrCreateDmRoom(otherUid: String, collegeId: String): String {
        val uid = currentUid()
        require(uid.isNotBlank()) { "User not signed in" }
        val roomId = listOf(uid, otherUid).sorted().joinToString("_")
        grantAccess(roomId, "dm", roomId, collegeId = collegeId)
        return roomId
    }

    suspend fun grantAccess(
        roomId: String,
        type: String,
        relatedId: String,
        categoryId: String? = null,
        clubId: String? = null,
        collegeId: String? = null
    ) {
        val uid = currentUid()
        if (uid.isBlank()) return

        val roomRef = database.reference.child("chats").child(roomId)
        val metadataRef = roomRef.child("metadata")
        val metadataSnap = metadataRef.get().await()

        if (!metadataSnap.exists()) {
            val metadata = mutableMapOf<String, Any>(
                "type" to type,
                "relatedId" to relatedId,
                "readOnly" to false,
                "createdAt" to ServerValue.TIMESTAMP,
            )
            categoryId?.let { metadata["categoryId"] = it }
            clubId?.let { metadata["clubId"] = it }
            collegeId?.let { metadata["collegeId"] = it }
            metadataRef.setValue(metadata).await()
        }

        database.reference.child("chatAccess").child(uid).child(roomId).setValue(true).await()
    }

    fun getMessages(roomId: String): Flow<List<RtdbChatMessage>> = callbackFlow {
        val ref = database.reference.child("chats").child(roomId).child("messages")
        val cache = linkedMapOf<String, RtdbChatMessage>()

        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.toChatMessage()?.let { msg ->
                    if (msg.deletedAt == null) {
                        cache[msg.id] = msg
                    } else {
                        cache.remove(msg.id)
                    }
                    trySend(cache.values.sortedByDescending { it.timestamp }).isSuccess
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.toChatMessage()?.let { msg ->
                    if (msg.deletedAt == null) {
                        cache[msg.id] = msg
                    } else {
                        cache.remove(msg.id)
                    }
                    trySend(cache.values.sortedByDescending { it.timestamp }).isSuccess
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val id = snapshot.key ?: return
                cache.remove(id)
                trySend(cache.values.sortedByDescending { it.timestamp }).isSuccess
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.orderByChild("timestamp").addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun loadMoreMessages(roomId: String, beforeTimestamp: Long): List<RtdbChatMessage> {
        val snap = database.reference
            .child("chats")
            .child(roomId)
            .child("messages")
            .orderByChild("timestamp")
            .endAt((beforeTimestamp - 1).toDouble())
            .limitToLast(20)
            .get()
            .await()

        return snap.children
            .mapNotNull { it.toChatMessage() }
            .filter { it.deletedAt == null }
            .sortedByDescending { it.timestamp }
    }

    suspend fun sendMessage(
        roomId: String,
        text: String,
        displayName: String,
        isAnonymous: Boolean,
        collegeId: String? = null,
    ): Result<Unit> = runCatching {
        val uid = currentUid()
        require(uid.isNotBlank()) { "User not signed in" }
        val clean = text.trim()
        require(clean.isNotBlank()) { "Empty message" }

        if (isAnonymous) {
            val alias = getAnonAlias(requireNotNull(collegeId) { "collegeId required for anonymous chat" })
            sendAnonMessage(roomId = roomId, text = clean, alias = alias)
        } else {
            val messageRef = database.reference
                .child("chats")
                .child(roomId)
                .child("messages")
                .push()

            val payload = mapOf(
                "senderId" to uid,
                "displayName" to displayName,
                "text" to clean,
                "timestamp" to ServerValue.TIMESTAMP,
                "deletedAt" to null,
            )
            messageRef.setValue(payload).await()
        }
    }

    suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit> = runCatching {
        val uid = currentUid()
        require(uid.isNotBlank()) { "User not signed in" }

        val msgRef = database.reference
            .child("chats")
            .child(roomId)
            .child("messages")
            .child(messageId)
        val senderId = msgRef.child("senderId").get().await().getValue(String::class.java)
        val canModerate = permissionHelper.hasPermission("canModerateChat")
        if (!canModerate && senderId != uid) {
            throw IllegalAccessException("Not allowed to delete this message")
        }
        msgRef.child("deletedAt").setValue(ServerValue.TIMESTAMP).await()
    }

    private val nameCache = mutableMapOf<String, String>()

    suspend fun removeAccess(roomId: String) {
        val uid = currentUid()
        if (uid.isBlank()) return
        database.reference.child("chatAccess").child(uid).child(roomId).removeValue().await()
    }

    fun getRoomsForUser(): Flow<List<ChatRoomPreview>> = callbackFlow {
        val uid = currentUid()
        if (uid.isBlank()) {
            trySend(emptyList()).isSuccess
            close()
            return@callbackFlow
        }

        val accessRef = database.reference.child("chatAccess").child(uid)
        
        val roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val roomIds = snapshot.children.mapNotNull { it.key }.distinct()
                if (roomIds.isEmpty()) {
                    trySend(emptyList()).isSuccess
                    return
                }

                this@callbackFlow.launch {
                    try {
                        val previews = roomIds.map { roomId ->
                            async {
                                try {
                                    val roomRef = database.reference.child("chats").child(roomId)
                                    val metadataSnap = roomRef.child("metadata").get().await()
                                    
                                    if (!metadataSnap.exists()) return@async null

                                    val lastMsgSnap = roomRef.child("messages")
                                        .orderByChild("timestamp")
                                        .limitToLast(1)
                                        .get()
                                        .await()

                                    val last = lastMsgSnap.children.firstOrNull()
                                    val type = metadataSnap.child("type").getValue(String::class.java).orEmpty()
                                    val relatedId = metadataSnap.child("relatedId").getValue(String::class.java).orEmpty()
                                    val catId = metadataSnap.child("categoryId").getValue(String::class.java).orEmpty()
                                    val clId = metadataSnap.child("clubId").getValue(String::class.java).orEmpty()
                                    val readOnly = metadataSnap.child("readOnly").getValue(Boolean::class.java) ?: false
                                    val timestamp = last?.child("timestamp")?.getValue(Long::class.java) ?: 0L
                                    val message = last?.child("text")?.getValue(String::class.java).orEmpty()

                                    val name = nameCache[relatedId] ?: try {
                                        when (type) {
                                            "club" -> {
                                                val snap = firestore.collection("Categories").document(catId)
                                                    .collection("Clubs").document(relatedId).get().await()
                                                snap.getString("name")?.also { nameCache[relatedId] = it } ?: "Club Chat"
                                            }
                                            "event" -> {
                                                val snap = firestore.collection("Categories").document(catId)
                                                    .collection("Clubs").document(clId)
                                                    .collection("Events").document(relatedId).get().await()
                                                snap.getString("title")?.also { nameCache[relatedId] = it } ?: "Event Chat"
                                            }
                                            else -> when (type) {
                                                "dm" -> "Direct Message"
                                                "anonymous" -> "Anonymous"
                                                else -> roomId
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ChatRepository", "Firestore name fetch failed for $relatedId", e)
                                        when (type) {
                                            "club" -> "Club Chat"
                                            "event" -> "Event Chat"
                                            "dm" -> "Direct Message"
                                            "anonymous" -> "Anonymous"
                                            else -> roomId
                                        }
                                    }
                                    
                                    ChatRoomPreview(
                                        id = roomId,
                                        roomId = roomId,
                                        type = type,
                                        name = name,
                                        relatedId = relatedId,
                                        readOnly = readOnly,
                                        lastMessage = message,
                                        timestamp = timestamp,
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("ChatRepository", "Error fetching preview for $roomId", e)
                                    null
                                }
                            }
                        }.awaitAll().filterNotNull()
                        
                        trySend(previews.sortedByDescending { it.timestamp }).isSuccess
                    } catch (e: Exception) {
                        android.util.Log.e("ChatRepository", "Parallel fetch failed", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        accessRef.addValueEventListener(roomListener)
        awaitClose { accessRef.removeEventListener(roomListener) }
    }

    suspend fun getAnonAlias(collegeId: String): String {
        val uid = currentUid()
        require(uid.isNotBlank()) { "User not signed in" }
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val today = formatter.format(java.util.Date())
        val docRef = firestore
            .collection("anonAliases")
            .document(collegeId)
            .collection(today)
            .document(uid)
        val existing = docRef.get().await().getString("alias")
        if (!existing.isNullOrBlank()) return existing

        val adjectives = listOf("Teal", "Swift", "Quiet", "Bright", "Nova", "Mellow")
        val animals = listOf("Otter", "Falcon", "Panda", "Fox", "Dolphin", "Lynx")
        val seed = kotlin.math.abs((uid + today).hashCode())
        val alias = "${adjectives[seed % adjectives.size]}${animals[seed % animals.size]}#${seed % 99 + 1}"
        docRef.set(mapOf("alias" to alias, "createdAt" to System.currentTimeMillis())).await()
        return alias
    }

    fun getRoomMetadata(roomId: String): Flow<ChatRoomMetadata> = callbackFlow {
        val ref = database.reference.child("chats").child(roomId).child("metadata")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                this@callbackFlow.launch {
                    val type = snapshot.child("type").getValue(String::class.java).orEmpty()
                    val relatedId = snapshot.child("relatedId").getValue(String::class.java).orEmpty()
                    val catId = snapshot.child("categoryId").getValue(String::class.java).orEmpty()
                    val clId = snapshot.child("clubId").getValue(String::class.java).orEmpty()

                    val name = nameCache[relatedId] ?: when (type) {
                        "club" -> {
                            val snap = firestore.collection("Categories").document(catId)
                                .collection("Clubs").document(relatedId).get().await()
                            snap.getString("name")?.also { nameCache[relatedId] = it } ?: "Club Chat"
                        }
                        "event" -> {
                            val snap = firestore.collection("Categories").document(catId)
                                .collection("Clubs").document(clId)
                                .collection("Events").document(relatedId).get().await()
                            snap.getString("title")?.also { nameCache[relatedId] = it } ?: "Event Chat"
                        }
                        "dm" -> "Direct Message"
                        "anonymous" -> "Anonymous"
                        else -> roomId
                    }

                    val metadata = ChatRoomMetadata(
                        id = roomId,
                        name = name,
                        type = type,
                        collegeId = snapshot.child("collegeId").getValue(String::class.java).orEmpty(),
                        relatedId = relatedId,
                        readOnly = snapshot.child("readOnly").getValue(Boolean::class.java) ?: false,
                        createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L,
                    )
                    trySend(metadata).isSuccess
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun sendAnonMessage(roomId: String, text: String, alias: String) {
        val ref = database.reference.child("chats").child(roomId).child("messages").push()
        ref.setValue(
            mapOf(
                "senderId" to "anon",
                "displayName" to alias,
                "text" to text,
                "timestamp" to ServerValue.TIMESTAMP,
                "deletedAt" to null,
            )
        ).await()
    }

    private fun DataSnapshot.toChatMessage(): RtdbChatMessage? {
        val id = key ?: return null
        return RtdbChatMessage(
            id = id,
            senderId = child("senderId").getValue(String::class.java).orEmpty(),
            displayName = child("displayName").getValue(String::class.java).orEmpty(),
            text = child("text").getValue(String::class.java).orEmpty(),
            timestamp = child("timestamp").getValue(Long::class.java) ?: 0L,
            deletedAt = child("deletedAt").getValue(Long::class.java),
        )
    }
}
