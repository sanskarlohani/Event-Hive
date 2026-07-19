package com.sanskar.eventhive.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.settings.ThemePreferenceManager
import com.sanskar.eventhive.ui.components.BottomBarScaffold
import com.sanskar.eventhive.ui.components.ClayButton
import com.sanskar.eventhive.ui.components.ClayCard
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.theme.AppDimens
import com.sanskar.eventhive.ui.viewModel.AuthViewModel
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.TicketViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    clubViewModel: ClubViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel(),
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferenceManager = remember { ThemePreferenceManager(context) }
    @Suppress("DEPRECATION")
    val appVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        }.getOrDefault("1.0")
    }
    val selectedTheme by preferenceManager.themeFlow.collectAsState(initial = "System")
    val userResource by userViewModel.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)
    val joinedClubRefs by userViewModel.joinedClubRefs.collectAsStateWithLifecycle()
    val allClubs by clubViewModel.allClubs.collectAsStateWithLifecycle()
    val allTickets by ticketViewModel.allTickets.collectAsStateWithLifecycle()
    val signOutState by authViewModel.signoutState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val user = (userResource as? Resource.Success)?.data
    val displayName = user?.name?.ifBlank { "Member" } ?: "Member"
    val displayEmail = user?.email ?: "member@eventhive.app"
    val currentUserId = user?.userId.orEmpty()
    val profileImageUrl = user?.profileImageUrl.orEmpty()
    val eventCount = user?.events?.size ?: 0
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            userViewModel.observeJoinedClubs(currentUserId)
        }
    }
    val clubCount = remember(allClubs, user, currentUserId) {
        if (currentUserId.isBlank()) 0
        else allClubs.count { club -> club.members.contains(currentUserId) }
    }
    val effectiveClubCount = remember(joinedClubRefs, clubCount, user) {
        when {
            joinedClubRefs.isNotEmpty() -> joinedClubRefs.size
            clubCount > 0 -> clubCount
            else -> user?.clubs?.size ?: 0
        }
    }
    val ticketCount = remember(allTickets, user, currentUserId) {
        if (currentUserId.isBlank()) {
            user?.tickets?.size ?: 0
        } else {
            val fromTicketCollection = allTickets.count {
                it.userId == currentUserId || it.participantIds.contains(currentUserId)
            }
            if (fromTicketCollection > 0) fromTicketCollection else (user?.tickets?.size ?: 0)
        }
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(signOutState) {
        if (signOutState is Resource.Success) {
            navController.navigate(NavigationItem.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    BottomBarScaffold(navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.ScreenHorizontalPadding, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileHeroCard(
                displayName = displayName,
                email = displayEmail,
                profileImageUrl = profileImageUrl,
                onEditProfile = { navController.navigate(NavigationItem.EditProfile.route) },
            )
            StatsRow(
                eventCount = eventCount,
                clubCount = effectiveClubCount,
                ticketCount = ticketCount,
                onEventsClick = { navController.navigate(NavigationItem.UserEvent.route) },
                onClubsClick = { navController.navigate(NavigationItem.UserClub.route) },
                onTicketsClick = { navController.navigate(NavigationItem.UserTicket.route) }
            )
            SectionTitle("Activity")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.AirplaneTicket,
                    title = "My Tickets",
                    onClick = { navController.navigate(NavigationItem.UserTicket.route) }
                )
            }
            SectionTitle("Settings")
            SettingsGroup {
                ThemeSelector(
                    selectedTheme = selectedTheme,
                    onThemeClick = { option ->
                        coroutineScope.launch {
                            preferenceManager.setTheme(option)
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                NotificationToggle(
                    enabled = notificationsEnabled,
                    onToggle = { notificationsEnabled = it }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = {}
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingsRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    onClick = { navController.navigate(NavigationItem.PrivacyPolicy.route) }
                )
            }
            SectionTitle("Support")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help Center",
                    onClick = {}
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingsRow(
                    icon = Icons.Default.BugReport,
                    title = "Report a Bug",
                    onClick = { navController.navigate(NavigationItem.ReportBug.route) }
                )
            }
            SectionTitle("Danger Zone", color = MaterialTheme.colorScheme.error)
            ClayCard(
                cornerRadius = 24.dp,
                elevation = 8.dp,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClayButton(
                        text = "Sign Out",
                        onClick = { showSignOutDialog = true },
                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        textColor = MaterialTheme.colorScheme.error,
                        cornerRadius = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = "Version $appVersion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out from Event Hive?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        authViewModel.signOut()
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("This action is permanent. Continue?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeroCard(
    displayName: String,
    email: String,
    profileImageUrl: String,
    onEditProfile: () -> Unit
) {
    ClayCard(
        cornerRadius = 32.dp,
        elevation = 12.dp,
        backgroundColor = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFF7F63FF)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile image",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            ClayButton(
                text = "Edit Profile",
                onClick = onEditProfile,
                backgroundColor = Color.White.copy(alpha = 0.2f),
                textColor = Color.White,
                cornerRadius = 20.dp,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun StatsRow(
    eventCount: Int,
    clubCount: Int,
    ticketCount: Int,
    onEventsClick: () -> Unit,
    onClubsClick: () -> Unit,
    onTicketsClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatsCard(
            modifier = Modifier.weight(1f),
            title = "Events",
            value = eventCount.toString(),
            onClick = onEventsClick
        )
        StatsCard(
            modifier = Modifier.weight(1f),
            title = "Clubs",
            value = clubCount.toString(),
            onClick = onClubsClick
        )
        StatsCard(
            modifier = Modifier.weight(1f),
            title = "Tickets",
            value = ticketCount.toString(),
            onClick = onTicketsClick
        )
    }
}

@Composable
private fun StatsCard(
    modifier: Modifier,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    ClayCard(
        modifier = modifier,
        onClick = onClick,
        cornerRadius = 20.dp,
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    ClayCard(
        cornerRadius = 24.dp,
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: String,
    onThemeClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Theme", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Light", "Dark", "System").forEach { option ->
                val selected = option == selectedTheme
                FilledTonalButton(
                    onClick = { onThemeClick(option) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(option)
                }
            }
        }
    }
}

@Composable
private fun NotificationToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Notifications", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Receive reminders and updates",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
