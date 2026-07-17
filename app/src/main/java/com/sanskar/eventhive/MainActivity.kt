package com.sanskar.eventhive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sanskar.eventhive.settings.ThemePreferenceManager
import com.sanskar.eventhive.ui.navigation.AppNavigation
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.theme.EventHiveTheme
import com.sanskar.eventhive.ui.viewModel.NotificationViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val NOTIFICATION_PERMISSION_CODE = 100

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val notificationViewModel: NotificationViewModel by viewModels()
    private var navController: NavHostController? = null

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Log the incoming intent
        intent?.data?.let { uri ->
            Log.d(TAG, "🔗 Received deep link: $uri")
            Log.d(TAG, "Deep link path: ${uri.path}")
            Log.d(TAG, "Deep link host: ${uri.host}")
            Log.d(TAG, "Deep link scheme: ${uri.scheme}")
        }

        // Initialize FCM token in background
        lifecycleScope.launch {
            initializeFcmToken()
        }

        setContent {
            val context = LocalContext.current
            val themePreferenceManager = androidx.compose.runtime.remember { ThemePreferenceManager(context) }
            val themePref by themePreferenceManager.themeFlow.collectAsStateWithLifecycle(initialValue = "System")
            val darkTheme = when (themePref) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }
            val navController = rememberNavController().also {
                this.navController = it
            }

            // Handle deep links
            LaunchedEffect(Unit) {
                intent?.let { handleIntent(it) }
            }

            // Clean up when activity is destroyed
            DisposableEffect(Unit) {
                onDispose {
                    this@MainActivity.navController = null
                }
            }

            EventHiveTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(navController = navController)
                }
            }
        }

        // Request notification permission after UI is set up
        requestNotificationPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Log any new intents received while app is running
        intent?.data?.let { uri ->
            Log.d(TAG, "🔄 Received new deep link while running: $uri")
            Log.d(TAG, "New deep link path: ${uri.path}")
            Log.d(TAG, "New deep link host: ${uri.host}")
            Log.d(TAG, "New deep link scheme: ${uri.scheme}")
        }

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                navController?.let { controller ->
                    handleDeepLink(uri, controller)
                }
            }
        }
    }

    private fun handleDeepLink(uri: Uri, navController: NavHostController) {
        try {
            // Remove the leading slash if present and get the path
            val path = uri.path?.removePrefix("/") ?: return
            Log.d(TAG, "Deep link path: $path")
            
            // Split the path into segments
            val segments = path.split("/")

            // Handle navigation based on the number of segments
            when (segments.size) {
                1 -> {
                    // Single category
                    val categoryId = segments[0]
                    val route = NavigationItem.AllCategory
                    Log.d(TAG, "Navigating to category: $route")
                    navController.navigate(route)
                }
                2 -> {
                    // Club detail
                    val (categoryId, clubId) = segments
                    val route = NavigationItem.ClubDetail.createRoute(categoryId, clubId)
                    Log.d(TAG, "Navigating to club: $route")
                    navController.navigate(route)
                }
                3 -> {
                    // Event detail
                    val (categoryId, clubId, eventId) = segments
                    val route = NavigationItem.EventDetail.createRoute(categoryId, clubId, eventId)
                    Log.d(TAG, "Navigating to event: $route")
                    navController.navigate(route)
                }
                else -> {
                    Log.w(TAG, "Invalid deep link pattern: $segments")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Deep link error", e)
        }
    }

    private fun initializeFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "FCM token failed", task.exception)
                    return@addOnCompleteListener
                }
                Log.d(TAG, "FCM token: ${task.result}")
            }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
        }
    }
}
