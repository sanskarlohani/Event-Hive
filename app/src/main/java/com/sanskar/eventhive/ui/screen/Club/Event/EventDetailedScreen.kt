package com.sanskar.eventhive.ui.screen.Club.Event

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.AdditionalInfo
import com.sanskar.eventhive.data.model.Event
import com.sanskar.eventhive.data.model.EventOrganizer
import com.sanskar.eventhive.data.model.RegistrationStatus
import com.sanskar.eventhive.data.model.Team
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.permissions.canEditEvent
import com.sanskar.eventhive.ui.viewModel.EventViewModel
import com.sanskar.eventhive.ui.viewModel.TicketViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    categoryId: String,
    clubId: String,
    eventId: String,
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel(),
) {
    val userRes by userViewModel.observeUser.collectAsStateWithLifecycle()
    val allUsersRes by userViewModel.allUsers.collectAsStateWithLifecycle()
    val usersById = ((allUsersRes as? Resource.Success)?.data ?: emptyList()).associateBy { it.userId }
    val currentUser = (userRes as? Resource.Success)?.data
    val userId = currentUser?.userId.orEmpty()
    val isEventManager = canEditEvent(currentUser)
    val teams by ticketViewModel.teamsForEvent.collectAsStateWithLifecycle()
    val tickets by ticketViewModel.ticketsForEvent.collectAsStateWithLifecycle()
    val event by eventViewModel.event.collectAsStateWithLifecycle()

    val registered = remember(event?.participantsIds, userId) {
        event?.participantsIds?.contains(userId) == true
    }

    LaunchedEffect(categoryId, clubId, eventId) {
        eventViewModel.getEvent(categoryId, clubId, eventId)
        ticketViewModel.getTicketsForEvent(categoryId, clubId, eventId)
        ticketViewModel.getTeamsForEvent(categoryId, clubId, eventId)
    }

    val isOrganizer = event?.organizers?.any { it.userId == userId } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (registered) {
                        IconButton(onClick = { navController.navigate("chat/event_$eventId") }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Event chat")
                        }
                    }
                    if (isEventManager || isOrganizer) {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    NavigationItem.EventSetting.createRoute(categoryId, clubId, eventId)
                                )
                            }
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = "Event settings")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val currentEvent = event ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeaderSection(
                posterUrl = currentEvent.posterUrl.orEmpty(),
                bannerUrl = currentEvent.bannerUrl.orEmpty(),
                title = currentEvent.title,
                startTime = currentEvent.startTime,
                isOnline = currentEvent.isOnline,
                venue = currentEvent.venue
            )
            AboutSection(
                about = currentEvent.about.orEmpty(),
                description = currentEvent.description
            )
            RegistrationSection(
                categoryId = categoryId,
                clubId = clubId,
                event = currentEvent,
                userId = userId,
                navController = navController,
                ticketViewModel = ticketViewModel,
                onRegister = {
                    navController.navigate(
                        NavigationItem.EventRegistration.createRoute(categoryId, clubId, eventId)
                    )
                }
            )
            PerksSection(currentEvent.perks)
            AdditionalInfoSection(currentEvent.additionalInfo)
            OrganizersSection(currentEvent.organizers, usersById)
            TeamsSection(teams) {
                navController.navigate(
                    NavigationItem.AllTeamsForEvent.createRoute(categoryId, clubId, eventId)
                )
            }
            TicketsSection(tickets)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
fun HeaderSection(
    posterUrl: String,
    bannerUrl: String,
    title: String,
    startTime: Timestamp?,
    isOnline: Boolean,
    venue: String,
) {
    val visual = if (bannerUrl.isNotBlank()) bannerUrl else posterUrl
    val startDate = startTime?.toDate()
    val dateText = remember(startDate) {
        startDate?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }.format(it)
        }.orEmpty()
    }
    val timeText = remember(startDate) {
        startDate?.let {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }.format(it)
        }.orEmpty()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (visual.isNotBlank()) {
                AsyncImage(
                    model = visual,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(icon = Icons.Filled.DateRange, text = dateText.ifBlank { "Date TBD" })
                    InfoChip(icon = Icons.Filled.Schedule, text = timeText.ifBlank { "Time TBD" })
                }
                Spacer(Modifier.height(8.dp))
                InfoChip(
                    icon = if (isOnline) Icons.Filled.LocationOn else Icons.Filled.Place,
                    text = if (isOnline) "Online" else venue.ifBlank { "Venue TBD" }
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun AboutSection(
    about: String,
    description: String,
) {
    SectionCard(title = "About Event") {
        Text(
            text = about.ifBlank { "No summary provided." },
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = description.ifBlank { "No detailed description available." },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationSection(
    categoryId: String,
    clubId: String,
    event: Event,
    userId: String,
    navController: NavController,
    ticketViewModel: TicketViewModel,
    onRegister: () -> Unit = {}
) {
    val endDate = remember(event) { event.registrationEndTime?.toDate() }
    if (endDate == null) return

    val formattedDate = remember(endDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(endDate)
    }
    val formattedTime = remember(endDate) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(endDate)
    }
    val registered = remember(event.participantsIds, userId) {
        event.participantsIds.contains(userId)
    }

    LaunchedEffect(registered, event.eventId, userId) {
        if (registered) ticketViewModel.getTicketForUserInEvent(event.eventId, userId)
    }

    val ticket by ticketViewModel.ticket.collectAsStateWithLifecycle()
    val actionStatus by ticketViewModel.actionStatus.collectAsStateWithLifecycle(initialValue = Resource.Idle)
    val isActionLoading = actionStatus is Resource.Loading

    LaunchedEffect(actionStatus) {
        when (actionStatus) {
            is Resource.Success -> {
                ticketViewModel.getTicketForUserInEvent(event.eventId, userId)
                ticketViewModel.getTicketsForEvent(categoryId, clubId, event.eventId)
                ticketViewModel.resetActionStatus()
            }
            is Resource.Error -> ticketViewModel.resetActionStatus()
            else -> Unit
        }
    }

    val teamId = remember(ticket) { ticket?.teamId.orEmpty() }
    val teamMemberIds = remember(ticket) { ticket?.participantIds ?: emptyList() }

    var remainingMillis by remember(endDate) { mutableStateOf(endDate.time - System.currentTimeMillis()) }
    LaunchedEffect(endDate) {
        while (remainingMillis > 0) {
            remainingMillis = endDate.time - System.currentTimeMillis()
            delay(1000L)
        }
    }

    val days = (remainingMillis / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    val hours = (remainingMillis / (1000 * 60 * 60) % 24).coerceAtLeast(0)
    val minutes = (remainingMillis / (1000 * 60) % 60).coerceAtLeast(0)
    val seconds = (remainingMillis / 1000 % 60).coerceAtLeast(0)

    SectionCard(title = "Registration") {
        Text(
            text = "Ends on $formattedDate at $formattedTime",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = String.format(
                    Locale.getDefault(),
                    "%02dd %02dh %02dm %02ds",
                    days,
                    hours,
                    minutes,
                    seconds
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center
            )
        }

        if (registered) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val tid = ticket?.ticketId ?: return@Button
                        navController.navigate(NavigationItem.UserTicketDetail.createRoute(userId, tid))
                    },
                    modifier = Modifier.weight(1f),
                    enabled = ticket != null && !isActionLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Show Ticket")
                }
                Button(
                    onClick = {
                        val currentTicket = ticket ?: return@Button
                        val participantsToRelease = if (currentTicket.participantIds.isNotEmpty()) {
                            currentTicket.participantIds
                        } else {
                            (teamMemberIds + userId).distinct()
                        }
                        ticketViewModel.cancelTicket(
                            categoryId = categoryId,
                            clubId = clubId,
                            eventId = event.eventId,
                            ticketId = currentTicket.ticketId,
                            teamId = teamId,
                            userId = userId,
                            participantIds = participantsToRelease
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = ticket != null && !isActionLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cancel Ticket")
                }
            }
            if (isActionLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Updating registration...", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            Button(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Register Now")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerksSection(perks: List<String>) {
    SectionCard(title = "Perks") {
        if (perks.isEmpty()) {
            Text("No perks listed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                perks.forEach { perk ->
                    AssistChip(
                        onClick = {},
                        label = { Text(perk) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdditionalInfoSection(additionalInfo: List<AdditionalInfo>) {
    SectionCard(title = "Additional Info") {
        if (additionalInfo.isEmpty()) {
            Text("No additional info provided.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                additionalInfo.forEach { info ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = info.key.ifBlank { "Info" },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = info.value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrganizersSection(
    eventOrganizers: List<EventOrganizer>,
    usersById: Map<String, User>,
    modifier: Modifier = Modifier,
) {
    SectionCard(title = "Organizers", modifier = modifier) {
        if (eventOrganizers.isEmpty()) {
            Text("No organizers assigned.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(eventOrganizers) { organizer ->
                    OrganizerCard(organizer = organizer, user = usersById[organizer.userId])
                }
            }
        }
    }
}

@Composable
private fun OrganizerCard(organizer: EventOrganizer, user: User?) {
    val displayName = user?.name?.takeIf { it.isNotBlank() } ?: organizer.userId
    val roleText = organizer.role.name.lowercase().replace('_', ' ').capitalizeWords()
    val descriptor = listOfNotNull(
        user?.course?.takeIf { it.isNotBlank() },
        user?.yearOfJoining?.takeIf { it.isNotBlank() }
    ).joinToString(" | ")

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(2).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.width(130.dp)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (descriptor.isNotBlank()) {
                    Text(
                        text = descriptor,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = roleText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TeamsSection(
    teams: List<Team>,
    onClick: () -> Unit
) {
    val uniqueTeams = remember(teams) { teams.distinctBy { it.teamId } }
    SectionCard(title = "Teams (${uniqueTeams.size})") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registered teams",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onClick, shape = RoundedCornerShape(8.dp)) {
                Text("See all")
            }
        }
        if (uniqueTeams.isEmpty()) {
            Text("No teams registered yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uniqueTeams.take(3).forEach { team ->
                    TeamCard(team = team)
                }
            }
        }
    }
}

@Composable
fun TeamCard(
    team: Team,
    onClick: () -> Unit = {}
) {
    val leaderName = team.teamMemberNames[team.teamLeaderId]
        ?: team.teamLeaderId.takeIf { it.isNotBlank() }
        ?: "N/A"
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.teamName.ifBlank { "Unnamed Team" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Leader: $leaderName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${team.teamMemberIds.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketsSection(
    tickets: List<Ticket>,
    modifier: Modifier = Modifier,
) {
    SectionCard(title = "Tickets (${tickets.size})", modifier = modifier) {
        if (tickets.isEmpty()) {
            Text("No tickets issued yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tickets.take(6).forEach { ticket ->
                    TicketCard(ticket = ticket)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TicketCard(ticket: Ticket) {
    val issuedDate = remember(ticket.issuedAt) {
        Instant.ofEpochMilli(ticket.issuedAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
    val statusLabel = ticket.status.name.lowercase().replace('_', ' ').capitalizeWords()
    val statusColor = when (ticket.status) {
        RegistrationStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
        RegistrationStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
        RegistrationStatus.CLAIMED -> MaterialTheme.colorScheme.tertiaryContainer
        RegistrationStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticket.userId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Ticket: ${ticket.ticketId.take(10)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Issued: $issuedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun String.capitalizeWords(): String {
    return split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}
