package com.sanskar.eventhive.Notification.FirebaseMessaging

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sanskar.eventhive.MainActivity
import com.sanskar.eventhive.R
import com.sanskar.eventhive.data.TokenManager
import com.sanskar.eventhive.data.model.Notification
import com.sanskar.eventhive.data.repository.Inteface.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // Save token locally
        TokenManager.saveToken(applicationContext, token)

        // Save token to Firestore
        saveTokenToFirestore(token)

        // Subscribe to topics
        subscribeToTopics(token)
    }

    private fun subscribeToTopics(token: String) {
        val topics = listOf("chat", "announcements", "events")
        topics.forEach { topic ->
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "Subscribed to topic: $topic")
                    } else {
                        Log.e("FCM", "Failed to subscribe to topic: $topic", task.exception)
                    }
                }
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val user = auth.currentUser
        if (user == null) {
            Log.d("FCM", "No authenticated user")
            return
        }

        val tokenData = hashMapOf(
            "fcmToken" to token,
            "lastUpdated" to FieldValue.serverTimestamp(),
            "deviceType" to "android"
        )

        firestore.collection("users").document(user.uid)
            .set(tokenData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FCM", "Token saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to save token", e)
            }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message received: ${remoteMessage.data}")

        val notification = Notification(
            title = remoteMessage.notification?.title ?: "New Notification",
            body = remoteMessage.notification?.body ?: "",
            imageUrl = remoteMessage.notification?.imageUrl?.toString(),
            deepLink = remoteMessage.data["deepLink"],
            senderId = remoteMessage.data["senderId"]
        )

        // Save notification
        notificationRepository.saveNotification(notification)

        // Create pending intent for notification click
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = Intent.ACTION_VIEW
            // Format the deep link URI properly
            notification.deepLink?.let { deepLink ->
                data = Uri.parse("eventhive://app$deepLink")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // Use unique request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build and show notification
        val builder = NotificationCompat.Builder(this, "default_channel")
            .setSmallIcon(R.drawable.google_icon)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notification.imageUrl?.let { imageUrl ->
            builder.setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(getBitmapFromUrl(imageUrl)))
        }

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun getBitmapFromUrl(imageUrl: String): android.graphics.Bitmap? {
        return try {
            val url = java.net.URL(imageUrl)
            android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: Exception) {
            Log.e("FCM", "Error loading notification image", e)
            null
        }
    }
}
