package com.sanskar.eventhive.ui.screen.Club

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.ClubRole
import com.sanskar.eventhive.data.model.ClubUser
import com.sanskar.eventhive.ui.permissions.canManageClubMembers
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageClubRolesScreen(
    categoryId: String,
    clubId: String,
    currentUserId: String,
    navController: NavController,
    viewModel: ClubViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    // collect current user ID (make sure your VM exposes this)
    var searchQuery by remember { mutableStateOf("") }
    val allMembers by viewModel.clubAllMembers.collectAsStateWithLifecycle()
    val clubMember by viewModel.clubMember.collectAsStateWithLifecycle()
    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)
    val currentUser = (currentUserRes as? Resource.Success<*>)?.data as? com.sanskar.eventhive.data.model.User
    val userPermissionLoading = currentUserRes is Resource.Loading || currentUserRes is Resource.Idle
    val canManageRolesAccess = clubMember?.role == ClubRole.ADMIN || canManageClubMembers(currentUser)

    LaunchedEffect(Unit) {
        viewModel.getAllClubMembers(categoryId, clubId)
        viewModel.getClubMember(categoryId, clubId, currentUserId)
    }

    if (userPermissionLoading) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manage Roles") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    if (!canManageRolesAccess) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manage Roles") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("You do not have permission to manage roles.")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Roles") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search members") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* hide keyboard if you like */ })
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val filtered = allMembers.orEmpty()
                    .filter { it.userId.contains(searchQuery, ignoreCase = true) }

                items(filtered, key = { it.userId }) { member ->
                    ManageRoleItem(
                        member = member,
                        currentUserId = currentUserId,
                        onRoleChange = { newRole ->
                            viewModel.clubMemberChangeRole(
                                categoryId = categoryId,
                                clubId = clubId,
                                userId = member.userId,
                                role = newRole.name
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageRoleItem(
    member: ClubUser,
    currentUserId: String,
    onRoleChange: (ClubRole) -> Unit
) {
    // If this user is an ADMIN or it's the current user, we disable role changes
    val isImmutable = member.role == ClubRole.ADMIN || member.userId == currentUserId

    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(member.role) }

    // Turn enum constant into a human‐friendly title
    fun ClubRole.displayName(): String =
        name.lowercase()
            .split('_')
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = member.userId,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            if (isImmutable) {
                // Plain text for admins or self
                Text(
                    text = selectedRole.displayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .width(140.dp)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
            } else {
                // Dropdown for everyone else
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        readOnly = true,
                        value = selectedRole.displayName(),
                        onValueChange = { },
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .width(140.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ClubRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.displayName()) },
                                onClick = {
                                    expanded = false
                                    if (role != selectedRole) {
                                        selectedRole = role
                                        onRoleChange(role)
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
