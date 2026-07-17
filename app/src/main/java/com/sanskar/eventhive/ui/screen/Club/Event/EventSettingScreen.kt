package com.sanskar.eventhive.ui.screen.Club.Event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Event
import com.sanskar.eventhive.data.model.EventMode
import com.sanskar.eventhive.ui.permissions.canEditEvent
import com.sanskar.eventhive.ui.viewModel.EventViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSettingScreen(
    categoryId: String,
    clubId: String,
    eventId: String,
    navController: NavController,
    eventViewModel: EventViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    // Trigger load
    LaunchedEffect(eventId) {
        eventViewModel.getEvent(categoryId, clubId, eventId)
    }
    // Collect nullable state
    val eventState by eventViewModel.event.collectAsState(initial = null)
    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)
    val currentUser = (currentUserRes as? Resource.Success)?.data
    val userPermissionLoading = currentUserRes is Resource.Loading || currentUserRes is Resource.Idle
    val canEditEventAccess = canEditEvent(currentUser) || eventState?.organizers?.any { it.userId == currentUser?.userId } == true

    if (userPermissionLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show loading until data arrives
    if (eventState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (!canEditEventAccess) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Event") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("You do not have permission to edit this event.")
            }
        }
        return
    }

    // Non-null safe
    val event = eventState!!

    // Local editable states
    var title by remember { mutableStateOf(TextFieldValue(event.title)) }
    var description by remember { mutableStateOf(TextFieldValue(event.description)) }
    var about by remember { mutableStateOf(TextFieldValue(event.about.orEmpty())) }
    var prizePool by remember { mutableStateOf(TextFieldValue(event.prizePool.orEmpty())) }
    var perks by remember { mutableStateOf(TextFieldValue(event.perks.joinToString(", "))) }
    var venue by remember { mutableStateOf(TextFieldValue(event.venue)) }
    var isOnline by remember { mutableStateOf(event.isOnline) }
    var registrationRequired by remember { mutableStateOf(event.registrationRequired) }

    // Mode selector
    var expanded by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(event.mode) }

    // Team sizes
    var minTeamSize by remember { mutableStateOf(event.minTeamSize.toString()) }
    var maxTeamSize by remember { mutableStateOf(event.maxTeamSize.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (event.eventId.isNotEmpty()) "Edit Event" else "New Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // Build updated Event
                        val updated = event.copy(
                            title = title.text,
                            description = description.text,
                            about = about.text.ifBlank { null },
                            prizePool = prizePool.text.ifBlank { null },
                            perks = perks.text.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            venue = venue.text,
                            isOnline = isOnline,
                            registrationRequired = registrationRequired,
                            mode = selectedMode,
                            minTeamSize = minTeamSize.toIntOrNull() ?: 1,
                            maxTeamSize = maxTeamSize.toIntOrNull() ?: 1
                        )
                        eventViewModel.updateEvent(updated)
                        navController.popBackStack()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Short Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            OutlinedTextField(
                value = about,
                onValueChange = { about = it },
                label = { Text("About (details)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )

            OutlinedTextField(
                value = prizePool,
                onValueChange = { prizePool = it },
                label = { Text("Prize Pool") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = perks,
                onValueChange = { perks = it },
                label = { Text("Perks (comma-separated)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it },
                label = { Text("Venue") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Online Event")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = isOnline,
                    onCheckedChange = { isOnline = it }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Registration Required")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = registrationRequired,
                    onCheckedChange = { registrationRequired = it }
                )
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedMode.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Event Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    EventMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name) },
                            onClick = {
                                selectedMode = mode
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (selectedMode != EventMode.SINGLE) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minTeamSize,
                        onValueChange = { minTeamSize = it.filter(Char::isDigit) },
                        label = { Text("Min Team Size") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxTeamSize,
                        onValueChange = { maxTeamSize = it.filter(Char::isDigit) },
                        label = { Text("Max Team Size") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
