package com.sanskar.eventhive.Chat

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.sanskar.eventhive.Chat.Data.CHATS
import com.sanskar.eventhive.Chat.Data.ChatData
import com.sanskar.eventhive.Chat.Data.ChatUser
import com.sanskar.eventhive.Chat.Data.Event
import com.sanskar.eventhive.Chat.Data.MESSAGE
import com.sanskar.eventhive.Chat.Data.Message
import com.sanskar.eventhive.Chat.Data.USER_NODE
import com.sanskar.eventhive.Chat.Data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val firebaseMessaging: FirebaseMessaging,
    //private val messagingRepository: MessagingRepository
) : ViewModel() {


    var inProcess = mutableStateOf(false)
    var inProcessChat = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    //Messaging
    private fun updateFCMToken(userId: String) {
        firebaseMessaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null) {
                    db.collection(USER_NODE).document(userId).update("fcmToken", token)
                        .addOnSuccessListener { Log.d("FCM", "Token updated successfully") }
                        .addOnFailureListener { e -> Log.e("FCM", "Failed to update token", e) }
                }
            } else {
                Log.e("FCM", "Failed to fetch FCM token", task.exception)
            }
        }
    }

//    fun sendNotification(userIds: List<String>, title: String, body: String) {
//        viewModelScope.launch {
//            try {
//                val response = messagingRepository.sendNotification(userIds, title, body)
//                if (response.isSuccessful) {
//                    Log.d("Notification", "Notification sent successfully!")
//                } else {
//                    Log.e("Notification", "Failed to send notification: ${response.errorBody()?.string()}")
//                }
//            } catch (e: Exception) {
//                Log.e("Notification", "Error sending notification", e)
//            }
//        }
//    }

    //Login
    fun login(email: String, password: String) {

        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill all the field")
            return
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            updateFCMToken(userId) // Update the FCM token
                            getUserData(userId)    // Fetch the user's data
                        }
                        signIn.value = true
                        inProcess.value = false
                        auth.currentUser?.uid?.let {
                            getUserData(it)
                        }
                    } else {
                        handleException(exception = it.exception, customMessage = "Login Failed")
                    }
                }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            CreateOrUpdateProfile(imageurl = it.toString())
        }
    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("image/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
                inProcess.value = false
            }
            .addOnFailureListener { exception ->
                inProcess.value = false
                handleException(exception, "Failed to upload image")
            }


    }

    fun signUp(name: String, number: String, email: String, password: String) {
        inProcess.value = true
        if (name.isEmpty() || number.isEmpty() || email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please fill all the fields")
            return
        }

        db.collection(USER_NODE).whereEqualTo("number", number).get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    CreateOrUpdateProfile(name, number)
                                    updateFCMToken(userId) // Update FCM token for the new user
                                    signIn.value = true
                                    inProcess.value = false
                                    Log.d("TAG", "signUp: User Logged In")
                                }
                            } else {
                                handleException(task.exception, customMessage = "Signup failed")
                                inProcess.value = false
                            }
                        }
                } else {
                    handleException(customMessage = "Number already exists")
                    inProcess.value = false
                }
            }
    }

    fun signOut() {
        try {
            auth.signOut()  // Firebase sign out
            signIn.value = false
            userData.value = null
            depopulateMessage()
            currentChatMessageListener = null
            eventMutableState.value = Event("Logged Out")
        } catch (e: Exception) {
            handleException(exception = e, customMessage = "Sign out failed")
        }
    }

    fun CreateOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageurl: String? = null
    ) {
        val uid = auth.currentUser?.uid ?: return

        // Prepare updated user data
        val updatedUserData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageurl ?: userData.value?.imageUrl
        )

        inProcess.value = true

        db.collection(USER_NODE).document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Update existing user data
                    db.collection(USER_NODE).document(uid)
                        .update(
                            mapOf(
                                "name" to (name ?: userData.value?.name),
                                "number" to (number ?: userData.value?.number),
                                "imageUrl" to (imageurl ?: userData.value?.imageUrl)
                            )
                        )
                        .addOnSuccessListener {
                            inProcess.value = false
                            getUserData(uid) // Refresh the user data
                        }
                        .addOnFailureListener {
                            handleException(it, "Failed to update profile")
                        }
                } else {
                    // Create new user data
                    db.collection(USER_NODE).document(uid).set(updatedUserData)
                        .addOnSuccessListener {
                            inProcess.value = false
                            getUserData(uid) // Refresh the user data
                        }
                        .addOnFailureListener {
                            handleException(it, "Failed to create profile")
                        }
                }
            }
            .addOnFailureListener {
                handleException(it, "Failed to retrieve user data")
            }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("LiveChatApp", "live chat exception: ", exception)
        exception?.printStackTrace()
        val errMsg = exception?.localizedMessage ?: ""
        val message = customMessage.ifEmpty { errMsg }

        eventMutableState.value = Event(message)
        inProcess.value = false
    }

    private fun getUserData(uid: String) {
        inProcess.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "user not retrieve")
            }

            if (value != null) {
                var user = value.toObject<UserData>()
                userData.value = user
                inProcess.value = false
                populateChat()
            }
        }
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() || !number.isDigitsOnly()) {
            handleException(customMessage = "Number can't be empty or must contain digits only")
            return
        }

        // Prevent the user from adding themselves
        if (number == userData.value?.number) {
            handleException(customMessage = "You cannot add yourself as a chat")
            return
        }

        // Check if a chat already exists
        db.collection(CHATS).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.number", number),
                    Filter.equalTo("user2.number", userData.value?.number)
                ),
                Filter.and(
                    Filter.equalTo("user1.number", userData.value?.number),
                    Filter.equalTo("user2.number", number)
                )
            )
        ).get().addOnSuccessListener { chatQuery ->
            if (chatQuery.isEmpty) {
                // Check if the user exists
                db.collection(USER_NODE).whereEqualTo("number", number).get()
                    .addOnSuccessListener { userQuery ->
                        if (userQuery.isEmpty) {
                            handleException(customMessage = "Number not found")
                        } else {
                            // Create the chat
                            val chatPartner = userQuery.toObjects<UserData>()[0]
                            val id = db.collection(CHATS).document().id
                            val chat = ChatData(
                                chatId = id,
                                user1 = ChatUser(
                                    userData.value?.userId,
                                    userData.value?.name,
                                    userData.value?.imageUrl,
                                    userData.value?.number
                                ),
                                user2 = ChatUser(
                                    chatPartner.userId,
                                    chatPartner.name,
                                    chatPartner.imageUrl,
                                    chatPartner.number
                                )
                            )
                            db.collection(CHATS).document(id).set(chat)
                                .addOnSuccessListener {
                                    Log.d("onAddChat", "Chat added successfully with ID: $id")
                                    // Notify the ViewModel to update state
                                }
                                .addOnFailureListener { e ->
                                    handleException(e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        handleException(e)
                    }
            } else {
                // Chat already exists
                handleException(customMessage = "Chat already exists")
            }
        }.addOnFailureListener { e ->
            handleException(e)
        }
    }


    fun populateChat() {
        inProcessChat.value = true

        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }

            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChat.value = false
            }
        }
    }

    fun populateMessage(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener =
            db.collection(CHATS)
                .document(chatId)
                .collection(MESSAGE)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        handleException(error)
                    }

                    if (value != null) {
                        chatMessages.value = value.documents.mapNotNull {
                            it.toObject<Message>()
                        }.sortedBy { it.timeStamp }
                        inProgressChatMessage.value = false
                    }
                }
    }

    fun depopulateMessage() {
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)

        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)
    }
}

