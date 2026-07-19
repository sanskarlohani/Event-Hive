package com.sanskar.eventhive.ui.screen.Club

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.ClubRole
import com.sanskar.eventhive.data.model.Event
import com.sanskar.eventhive.ui.components.ClayButton
import com.sanskar.eventhive.ui.components.ClayCard
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.permissions.canCreateEvent
import com.sanskar.eventhive.ui.permissions.canManageClubMembers
import com.sanskar.eventhive.ui.permissions.normalizeSystemRole
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.EventViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(
    navController: NavController,
    categoryId: String,
    clubId: String,
    userId: String,
    clubViewModel: ClubViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    Log.d("ClubDetailScreen", "userId: $userId")

    LaunchedEffect(categoryId, clubId) {
        clubViewModel.getClub(categoryId, clubId)
        clubViewModel.getClubMember(categoryId, clubId, userId)
        eventViewModel.getEventsByClubId(categoryId, clubId)
    }

    val club by clubViewModel.club.collectAsStateWithLifecycle()
    val clubMember by clubViewModel.clubMember.collectAsStateWithLifecycle()
    val eventsByClubId by eventViewModel.eventsByClubId.collectAsState()
    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle()
    val currentUserEmail = (currentUserRes as? Resource.Success)?.data?.email.orEmpty()
    val currentUser = (currentUserRes as? Resource.Success)?.data

    val isAdmin = clubMember?.role == ClubRole.ADMIN
    val normalizedRole = normalizeSystemRole(currentUser?.systemRole)
    val canOpenClubSettings = isAdmin ||
        normalizedRole == "studentGuide" ||
        normalizedRole == "superAdmin" ||
        canManageClubMembers(currentUser)
    val canCreateEventAccess = isAdmin || canCreateEvent(currentUser)
    val isPublic = club?.isPublic ?: false
    val joined = (clubMember != null) ||
        (userId.isNotBlank() && (club?.members?.contains(userId) == true)) ||
        (currentUserEmail.isNotBlank() && (club?.members?.contains(currentUserEmail) == true))

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showExportDialog by remember { mutableStateOf(false) }
    var lastSavedFilePath by remember { mutableStateOf<String?>(null) }
    var lastSavedMimeType by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past")

    fun openSavedFile() {
        val filePath = lastSavedFilePath ?: return
        val mimeType = lastSavedMimeType ?: return
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open file with"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportMembers(asExcel: Boolean) {
        coroutineScope.launch {
            try {
                val users = club?.let { clubViewModel.getClubMembersDetails(it) } ?: emptyList()
                val csv = usersToCsv(users)
                val extension = if (asExcel) "xls" else "csv"
                val mime = if (asExcel) "application/vnd.ms-excel" else "text/csv"
                val label = if (asExcel) "Excel" else "CSV"
                val fileName = "club_${clubId}_members_${System.currentTimeMillis()}.$extension"
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(csv.toByteArray()) }
                lastSavedFilePath = file.absolutePath
                lastSavedMimeType = mime
                val result = snackbarHostState.showSnackbar(
                    message = "$label file saved to Downloads.",
                    actionLabel = "View",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    openSavedFile()
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to save: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(club?.name ?: "Club") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (joined) {
                        IconButton(onClick = { navController.navigate("chat/club_$clubId") }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Club chat")
                        }
                    }
                    if (canOpenClubSettings) {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    NavigationItem.ClubSetting.createRoute(categoryId, clubId)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Club settings"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ClayCard(
                cornerRadius = 28.dp,
                elevation = 10.dp,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(3.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val url = club?.logoUrl
                            Log.d("ClubDetailScreen", "Rendering logo: $url")
                            AsyncImage(
                                model = url,
                                contentDescription = "Club logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = androidx.compose.ui.res.painterResource(com.sanskar.eventhive.R.drawable.ic_launcher_foreground),
                                placeholder = androidx.compose.ui.res.painterResource(com.sanskar.eventhive.R.drawable.ic_launcher_foreground),
                                onLoading = { Log.d("ClubDetailScreen", "Logo loading...") },
                                onSuccess = { Log.d("ClubDetailScreen", "Logo loaded successfully") },
                                onError = { Log.e("ClubDetailScreen", "Logo failed to load: ${it.result.throwable.message}") }
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = club?.name ?: "Loading...",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isPublic) "Public club" else "Private club",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = club?.description.orEmpty().ifBlank { "No description available." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClubStatPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Group,
                            label = "Members",
                            value = (club?.members?.size ?: 0).toString()
                        )
                        ClubStatPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.CalendarToday,
                            label = "Events",
                            value = (club?.events?.size ?: 0).toString()
                        )
                        ClubStatPill(
                            modifier = Modifier.weight(1f),
                            icon = if (isPublic) Icons.Filled.Public else Icons.Filled.Lock,
                            label = "Access",
                            value = if (isPublic) "Open" else "Closed"
                        )
                    }

                    val joinLeaveEnabled = !isAdmin && (joined || isPublic)
                    ClayButton(
                        text = if (joined) "Leave Club" else "Join Club",
                        onClick = {
                            if (joined) {
                                clubViewModel.leaveClub(categoryId, clubId, userId)
                            } else {
                                clubViewModel.joinClub(categoryId, clubId, userId)
                            }
                        },
                        enabled = joinLeaveEnabled,
                        backgroundColor = if (joined) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                        textColor = if (joined) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    )
                    
                    if (!joinLeaveEnabled) {
                        Text(
                            text = if (isAdmin) {
                                "You are an admin of this club."
                            } else {
                                "This is a private club. Joining is restricted."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (canCreateEventAccess) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ClayButton(
                                text = "Create Event",
                                onClick = {
                                    navController.navigate(
                                        NavigationItem.CreateEvent.createRoute(categoryId, clubId)
                                    )
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                textColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            ClayButton(
                                text = "Members Data",
                                onClick = { showExportDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                                textColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            Text(
                text = "Events",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val events = eventsByClubId
            val now = Instant.now()
            val filtered = if (selectedTab == 0) {
                events.filter { it.startTime?.toDate()?.toInstant()?.isAfter(now) == true }
            } else {
                events.filter { it.endTime?.toDate()?.toInstant()?.isBefore(now) == true }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (filtered.isEmpty()) {
                    Text(
                        text = "No ${tabs[selectedTab].lowercase()} events",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    filtered.forEach { event ->
                        EventCard(event = event) {
                            navController.navigate(
                                NavigationItem.EventDetail.createRoute(
                                    categoryId = categoryId,
                                    clubId = clubId,
                                    eventId = event.eventId
                                )
                            )
                        }
                    }
                }
            }
        }

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Club Members") },
                text = { Text("Choose a format to export member details.") },
                confirmButton = {
                    TextButton(onClick = {
                        showExportDialog = false
                        exportMembers(asExcel = false)
                    }) {
                        Text("CSV")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showExportDialog = false
                        exportMembers(asExcel = true)
                    }) {
                        Text("Excel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ClubStatPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventCard(
    event: Event,
    onDetailsClick: () -> Unit = {}
) {
    val zoned = event.startTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())
    val dateText = zoned?.let { DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()).format(it) }
        ?: "--"
    val timeText = zoned?.let { DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()).format(it) }
        .orEmpty()

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .width(76.dp)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = timeText, 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = event.venue, 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    ClayButton(
                        text = "Details",
                        onClick = onDetailsClick,
                        modifier = Modifier.width(100.dp).height(36.dp),
                        cornerRadius = 10.dp,
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

fun usersToCsv(users: List<com.sanskar.eventhive.data.model.User>): String {
    val header = "UserId,Name,Course,YearOfJoining,YearOfPassing,Email,Phone,Role\n"
    val rows = users.joinToString("\n") { user ->
        "${user.userId},${user.name},${user.course},${user.yearOfJoining},${user.yearOfPassing},${user.email},${user.phone ?: ""},${user.appRole}"
    }
    return header + rows
}
