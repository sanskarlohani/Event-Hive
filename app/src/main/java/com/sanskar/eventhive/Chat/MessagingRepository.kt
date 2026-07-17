package com.sanskar.eventhive.Chat
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.tasks.Tasks
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import javax.inject.Inject
//import javax.inject.Singleton
//import retrofit2.Response
//
//
//@Singleton
//class MessagingRepository @Inject constructor(
//    private val context: Context,
//    private val db: FirebaseFirestore, // Inject Firestore
//    private val notificationService: NotificationService // Inject the service
//) {
//    companion object {
//        private const val TAG = "MessagingRepo"
//    }
//
//    suspend fun sendNotification(userIds: List<String>, title: String, body: String): Response<Unit> {
//        Log.d(TAG, "📤 Sending notification to users: $userIds")
//        Log.d(TAG, "Notification content - Title: $title, Body: $body")
//
//        val notificationData = NotificationData(userIds, title, body)
//        val response = notificationService.sendNotification(notificationData)
//
//        if (response.isSuccessful) {
//            Log.d(TAG, "✅ Notification sent successfully")
//        } else {
//            Log.e(TAG, "❌ Failed to send notification: ${response.errorBody()?.string()}")
//        }
//        return response
//    }
//
//    fun updateTokenInFirestore(token: String): Task<Void> {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        Log.d(TAG, "Updating token for user: $userId")
//
//        return if (userId != null) {
//            db.collection("users").document(userId)
//                .update("fcmToken", token)
//                .addOnSuccessListener {
//                    Log.d(TAG, "✅ Token updated successfully in Firestore for user: $userId")
//                }
//                .addOnFailureListener { e ->
//                    Log.e(TAG, "❌ Failed to update token in Firestore", e)
//                }
//        } else {
//            Log.e(TAG, "❌ Cannot update token: User not logged in")
//            Tasks.forException(Exception("User not logged in"))
//        }
//    }
//
//    fun showNotification(title: String?, body: String?, deepLink: String? = null) {
//        Log.d(TAG, "📱 Showing notification - Title: $title, Body: $body, DeepLink: $deepLink")
//
//        try {
//            val channelId = "default_channel"
//            val channelName = "Default Channel"
//
//            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//            // Create or update notification channel
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Log.d(TAG, "Creating notification channel: $channelId")
//                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
//                notificationManager.createNotificationChannel(channel)
//            }
//
//            // Create pending intent for deep link
//            val intent = if (deepLink != null) {
//                Log.d(TAG, "Creating deep link intent for: $deepLink")
//                Intent(Intent.ACTION_VIEW, Uri.parse("eventhive://app/$deepLink")).apply {
//                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                }
//            } else {
//                Log.d(TAG, "Creating default intent")
//                Intent(context, MainActivity::class.java).apply {
//                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                }
//            }
//
//            val pendingIntent = PendingIntent.getActivity(
//                context,
//                0,
//                intent,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//
//            val notification = NotificationCompat.Builder(context, channelId)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setSmallIcon(R.drawable.google_icon)
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .build()
//
//            val notificationId = System.currentTimeMillis().toInt()
//            Log.d(TAG, "Showing notification with ID: $notificationId")
//            notificationManager.notify(notificationId, notification)
//            Log.d(TAG, "✅ Notification shown successfully")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Error showing notification", e)
//        }
//    }
//}
