package com.sanskar.eventhive.ui.screen.Club.Event

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.R
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.EventMode
import com.sanskar.eventhive.data.model.Team
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.ui.components.ClayButton
import com.sanskar.eventhive.ui.components.ClayCard
import com.sanskar.eventhive.ui.viewModel.EventViewModel
import com.sanskar.eventhive.ui.viewModel.TicketViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@Composable
fun EventRegistrationScreen(
    navController: NavController,
    categoryId: String,
    clubId: String,
    eventId: String,
    userId: String,
    eventViewModel: EventViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    fun userLabel(user: com.sanskar.eventhive.data.model.User): String {
        val descriptor = listOfNotNull(
            user.course.takeIf { it.isNotBlank() },
            user.yearOfJoining.takeIf { it.isNotBlank() },
            user.yearOfPassing.takeIf { it.isNotBlank() },
        ).joinToString(" | ")
        return if (descriptor.isBlank()) user.name else "${user.name} ($descriptor)"
    }
    // 1) Load event
    val event by eventViewModel.event.collectAsStateWithLifecycle()


    LaunchedEffect(eventId) {
        eventViewModel.getEvent(categoryId, clubId, eventId)
    }

    val eventName = remember(event) {
        event?.title.orEmpty()
    }
    val actionStatus by ticketViewModel.actionStatus.collectAsStateWithLifecycle(null)
    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)
    val currentUser = (currentUserRes as? Resource.Success)?.data

    // 2) Local UI state
    val individualLabel = stringResource(R.string.registration_individual)
    val groupLabel = stringResource(R.string.registration_group)
    var participation by remember {
        mutableStateOf(
            when (event?.mode) {
                EventMode.SINGLE -> individualLabel
                EventMode.GROUP -> groupLabel
                EventMode.BOTH -> groupLabel
                else -> individualLabel
            }
        )
    }
    val isGroupRegistration = remember(event?.mode, participation) {
        when (event?.mode) {
            EventMode.GROUP -> true
            EventMode.SINGLE -> false
            EventMode.BOTH -> participation == groupLabel
            else -> false
        }
    }
    var teamName by remember { mutableStateOf("") }
    val memberIds = remember { mutableStateListOf<String>() }
    LaunchedEffect(event?.minTeamSize) {
        memberIds.clear()
    }

    // Additional‐info answers
    val extraAnswers = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(event) {
        event?.additionalInfoAskFromUser?.forEach {
            if (!extraAnswers.containsKey(it.key)) {
                extraAnswers[it.key] = ""
            }
        }
    }

    // Dialog controls
    var showMemberDialog by remember { mutableStateOf(value = false) }
    var searchQuery by remember { mutableStateOf("") }
    val candidates by ticketViewModel.membersNotRegistered.collectAsStateWithLifecycle(emptyList())
    val selectedIds = remember { mutableStateListOf<String>() }
    val selectedMemberCount = memberIds.count { it.isNotBlank() && (it != userId) }
    val totalParticipantCount = 1 + selectedMemberCount
    val minAllowed = (event?.minTeamSize ?: 1).coerceAtLeast(1)
    val maxAllowed = (event?.maxTeamSize ?: Int.MAX_VALUE).coerceAtLeast(minAllowed)
    val groupSizeValid = !isGroupRegistration || (totalParticipantCount in minAllowed..maxAllowed)
    val requiredAnswersMissing = event?.additionalInfoAskFromUser
        ?.any { it.required && (extraAnswers[it.key].isNullOrBlank()) } == true
    val canSubmit = !requiredAnswersMissing &&
        (!isGroupRegistration || teamName.isNotBlank()) &&
        groupSizeValid

    // Fetch candidates when dialog opens
    LaunchedEffect(showMemberDialog) {
        if (showMemberDialog && (event != null)) {
            ticketViewModel.getMembersNotRegistered(
                categoryId,
                clubId,
                eventId,
                event!!.participantsIds,
                userId,
            )
            selectedIds.clear()
        }
    }

    // Main content
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Header
        Column(Modifier.padding(24.dp)) {
            Text(
                text = event?.title ?: "",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(8.dp))
            event?.startTime?.let {
                Text(
                    text = "${it.toDateString()} • ${it.toTimeString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Participation section
        SectionCard(title = stringResource(R.string.registration_participation)) {
            if (event?.mode == EventMode.BOTH) {
                SegmentedControl(
                    options = listOf(individualLabel, groupLabel),
                    selected = participation,
                    onSelect = { participation = it },
                )
            } else {
                Text(
                    text = if (event?.mode == EventMode.SINGLE) individualLabel else groupLabel,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Team details
        if (isGroupRegistration) {
            SectionCard(title = stringResource(R.string.registration_team_details)) {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text(stringResource(R.string.registration_team_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { showMemberDialog = true }) {
                    val maxTeamSize = (event?.maxTeamSize ?: 1).coerceAtLeast(1)
                    val maxOthers = (maxTeamSize - 1).coerceAtLeast(0)
                    Text(stringResource(R.string.registration_select_members, memberIds.filter { it.isNotBlank() }.size, maxOthers))
                }
                Spacer(Modifier.height(8.dp))
                val memberNames = remember(memberIds, candidates, currentUser) {
                    memberIds.filter { it.isNotBlank() }.map { id ->
                        if (id == userId) currentUser?.name ?: id
                        else candidates.find { it.userId == id }?.name ?: id
                    }
                }
                memberNames.forEach { name ->
                    Text("• $name", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        // Additional-info fields
        if (!event?.additionalInfoAskFromUser.isNullOrEmpty()) {
            SectionCard(title = stringResource(R.string.registration_additional_info)) {
                event!!.additionalInfoAskFromUser.forEach { info ->
                    OutlinedTextField(
                        value = extraAnswers[info.key]!!,
                        onValueChange = { extraAnswers[info.key] = it },
                        label = { Text(info.key) },
                        isError = info.required && extraAnswers[info.key]?.isBlank() == true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Submit button
        ClayButton(
            text = stringResource(R.string.registration_register),
            onClick = {
                val id = UUID.randomUUID().toString()
                val selectedMembers = if (isGroupRegistration) {
                    memberIds.filter { it.isNotBlank() && it != userId }
                } else {
                    emptyList()
                }
                val participantIds = (listOf(userId) + selectedMembers).distinct()
                val memberNameMap = buildMap {
                    currentUser?.name?.takeIf { it.isNotBlank() }?.let { put(userId, it) }
                    candidates.forEach { candidate ->
                        if (candidate.name.isNotBlank()) {
                            put(candidate.userId, candidate.name)
                        }
                    }
                }
                val finalTeamName = if (isGroupRegistration) {
                    teamName.trim().ifBlank { "Team-${id.take(6)}" }
                } else {
                    "Solo-${userId.take(6)}"
                }
                val team = Team(
                    teamId = id,
                    teamName = finalTeamName,
                    teamMemberIds = participantIds,
                    teamMemberNames = participantIds.associateWith { pid ->
                        memberNameMap[pid] ?: pid
                    },
                    teamLeaderId = userId,
                    eventId = eventId,
                    clubId = clubId,
                    categoryId = categoryId,
                    eventName = eventName,
                )
                val ticket = Ticket(
                    ticketId = id,
                    categoryId = categoryId,
                    clubId = clubId,
                    eventId = eventId,
                    userId = userId,
                    teamId = id,
                    additionalInfoAskByEventOrganizer = event?.additionalInfoAskFromUser
                        ?: emptyList(),
                    additionalInfoAnswers = extraAnswers
                        .filterValues { it.isNotBlank() },
                    participantIds = participantIds,
                    )
                ticketViewModel.issueTicket(ticket, team)
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .semantics { contentDescription = "Submit registration for ${event?.title}" }
        )
    }

    // Member-selection dialog
    if (showMemberDialog) {
        AlertDialog(
            onDismissRequest = { showMemberDialog = false },
            title = { Text(stringResource(R.string.registration_select_members_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.registration_search_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    val filtered = candidates.filter {
                        it.userId.contains(searchQuery, ignoreCase = true) ||
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.course.contains(searchQuery, ignoreCase = true) ||
                            it.yearOfJoining.contains(searchQuery, ignoreCase = true) ||
                            it.yearOfPassing.contains(searchQuery, ignoreCase = true)
                    }
                    Column(modifier = Modifier.height(200.dp)) {
                        filtered.forEach { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (user.userId in selectedIds) selectedIds.remove(user.userId)
                                else if (selectedIds.size < ((event?.maxTeamSize
                                                ?: Int.MAX_VALUE) - 1).coerceAtLeast(0)
                                        )
                                    selectedIds.add(user.userId)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = user.userId in selectedIds,
                                    onCheckedChange = {
                                        if (it && selectedIds.size < ((event?.maxTeamSize
                                                ?: Int.MAX_VALUE) - 1).coerceAtLeast(0)
                                        )
                                            selectedIds.add(user.userId)
                                        else
                                            selectedIds.remove(user.userId)
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(userLabel(user), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val minSize = ((event?.minTeamSize ?: 1) - 1).coerceAtLeast(0)
                val maxSize = ((event?.maxTeamSize ?: Int.MAX_VALUE) - 1).coerceAtLeast(0)
                Button(
                    onClick = {
                        memberIds.clear()
                        memberIds.addAll(selectedIds)
                        showMemberDialog = false
                    },
                    enabled = selectedIds.size in minSize..maxSize
                ) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showMemberDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Handle Loading / Error / Success
    when (actionStatus) {
        is Resource.Loading -> FullScreenLoading()
        is Resource.Error -> FullScreenError((actionStatus as Resource.Error).exception)
        is Resource.Success -> LaunchedEffect(Unit) { navController.popBackStack() }
        else -> {}
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ClayCard(
        cornerRadius = 20.dp,
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(Modifier.padding(4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        options.forEach { opt ->
            Box(
                Modifier
                    .weight(1f)
                    .clickable { onSelect(opt) }
                    .background(if (opt == selected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    opt,
                    color = if (opt == selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = "Loading registration content" }
        )
    }
}

@Composable
private fun FullScreenError(e: Throwable?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = e?.localizedMessage ?: stringResource(R.string.common_error),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { contentDescription = "Error: ${e?.localizedMessage}" }
        )
    }
}

private fun Timestamp.toDateString(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(this.toDate())

private fun Timestamp.toTimeString(): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(this.toDate())
