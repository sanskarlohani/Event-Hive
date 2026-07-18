package com.sanskar.eventhive.ui.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.model.Notification
import com.sanskar.eventhive.ui.components.ClayCard
import com.sanskar.eventhive.ui.theme.AppDimens
import com.sanskar.eventhive.ui.viewModel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle(initialValue = emptyList())
    val readOverrides = remember { mutableStateMapOf<String, Boolean>() }

    val grouped = remember(notifications, readOverrides.toMap()) {
        val now = Calendar.getInstance()
        val today = mutableListOf<Notification>()
        val thisWeek = mutableListOf<Notification>()
        val earlier = mutableListOf<Notification>()

        notifications.forEach { notification ->
            val cal = Calendar.getInstance().apply {
                time = runCatching { notification.timestamp.toDate() }.getOrElse { java.util.Date(0) }
            }
            when {
                (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
                    (cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) -> today += notification
                (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
                    (cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)) -> thisWeek += notification
                else -> earlier += notification
            }
        }
        mapOf("Today" to today, "This Week" to thisWeek, "Earlier" to earlier)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Text(
                        text = "Mark all read",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            notifications.forEach { readOverrides[it.id] = true }
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyNotificationsState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    horizontal = AppDimens.ScreenHorizontalPadding,
                    vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grouped.forEach { (section, list) ->
                    if (list.isNotEmpty()) {
                        item {
                            Text(
                                text = section,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                            )
                        }
                        items(list, key = { it.id }) { notification ->
                            val isRead = readOverrides[notification.id] ?: notification.read
                            NotificationRow(
                                notification = notification,
                                read = isRead,
                                onClick = {
                                    readOverrides[notification.id] = true
                                    notification.deepLink?.let { deepLink ->
                                        navigateNotificationDeepLink(navController, deepLink)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun navigateNotificationDeepLink(navController: NavController, deepLink: String) {
    val cleaned = deepLink.trim()
    if (cleaned.isBlank()) return

    runCatching {
        if (cleaned.startsWith("eventhive://", ignoreCase = true)) {
            navController.navigate(cleaned.toUri())
        } else {
            navController.navigate(cleaned)
        }
    }.onFailure { error ->
        Log.e("NotificationsScreen", "Failed to navigate deep link: $cleaned", error)
    }
}

@Composable
private fun NotificationRow(
    notification: Notification,
    read: Boolean,
    onClick: () -> Unit
) {
    val icon = when {
        notification.title.contains(other = "ticket", ignoreCase = true) -> Icons.Default.ConfirmationNumber
        notification.title.contains(other = "event", ignoreCase = true) -> Icons.Default.CalendarMonth
        else -> Icons.Default.Campaign
    }
    val iconTint = when {
        notification.title.contains("ticket", true) -> Color(0xFFF59E0B)
        notification.title.contains("event", true) -> Color(0xFF14B8A6)
        else -> MaterialTheme.colorScheme.primary
    }

    val cardBg = if (read) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    }

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        cornerRadius = 20.dp,
        elevation = 6.dp,
        backgroundColor = cardBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (read) FontWeight.Bold else FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (read) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTimestamp(notification),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                if (!read) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                .padding(18.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "All caught up!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "You have no new notifications right now.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTimestamp(notification: Notification): String {
    val date = runCatching { notification.timestamp.toDate() }.getOrElse { java.util.Date(0) }
    val now = Calendar.getInstance()
    val incoming = Calendar.getInstance().apply { time = date }
    return when {
        now.get(Calendar.YEAR) == incoming.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == incoming.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        }
        now.get(Calendar.WEEK_OF_YEAR) == incoming.get(Calendar.WEEK_OF_YEAR) -> {
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
    }
}
