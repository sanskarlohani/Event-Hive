package com.sanskar.eventhive.Chat
//
//import android.util.Log
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//import dagger.hilt.android.HiltAndroidApp
//import javax.inject.Inject
//
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//    companion object {
//        private const val TAG = "FCMService"
//    }
//
//    @Inject
//    //lateinit var messagingRepository: MessagingRepository
//
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        Log.d(TAG, "New FCM token received: $token")
//
//        messagingRepository.updateTokenInFirestore(token)
//            .addOnSuccessListener {
//                Log.d(TAG, "✅ Token successfully updated in Firestore")
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "❌ Failed to update token in Firestore", e)
//            }
//    }
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//        Log.d(TAG, "📩 Message received from: ${remoteMessage.from}")
//
//        // Log message data
//        Log.d(TAG, "Message data payload: ${remoteMessage.data}")
//
//        remoteMessage.notification?.let {
//            Log.d(TAG, "Message Notification Title: ${it.title}")
//            Log.d(TAG, "Message Notification Body: ${it.body}")
//        }
//
//        try {
//            remoteMessage.data.let { data ->
//                Log.d(TAG, "Processing notification with data: $data")
//                val deepLink = data["deepLink"]
//                Log.d(TAG, "Deep link from data: $deepLink")
//
//                messagingRepository.showNotification(
//                    title = data["title"] ?: "New Message",
//                    body = data["body"] ?: "You have a new message",
//                    deepLink = deepLink
//                )
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Error processing message", e)
//        }
//    }
//}
