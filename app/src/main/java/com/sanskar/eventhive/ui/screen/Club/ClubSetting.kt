package com.sanskar.eventhive.ui.screen.Club

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.model.ClubRole
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.permissions.canManageClubMembers
import com.sanskar.eventhive.ui.permissions.normalizeSystemRole
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubSettingScreen(
    navController: NavController,
    categoryId: String,
    clubId: String,
    userId: String,
    viewModel: ClubViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    // Collect Firestore data
    val club by viewModel.club.collectAsStateWithLifecycle()
    val clubMember by viewModel.clubMember.collectAsStateWithLifecycle()
    val clubMembers by viewModel.clubAllMembers.collectAsStateWithLifecycle()
    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle()
    val currentUser = (currentUserRes as? com.sanskar.eventhive.data.Resource.Success)?.data

    LaunchedEffect(Unit) {

    }
    // Trigger initial load
    LaunchedEffect(Unit) {
        viewModel.getClub(categoryId, clubId)
        viewModel.getClubMember(categoryId, clubId, userId)
        viewModel.getAllClubMembers(categoryId,clubId)
    }

    // Local editable state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf<String?>(null) }
    var logoUrl by remember { mutableStateOf<String?>(null) }
    var isPublic by remember { mutableStateOf(false) }

    // Initialize when `club` is first loaded or changed
    LaunchedEffect(club) {
        club?.let {
            name = it.name
            description = it.description
            logoUrl = it.logoUrl
            isPublic = it.isPublic
        }
    }

    val isAdmin = clubMember?.role == ClubRole.ADMIN
    val normalizedRole = normalizeSystemRole(currentUser?.systemRole)
    val canManageSettings = isAdmin ||
        normalizedRole == "studentGuide" ||
        normalizedRole == "superAdmin" ||
        canManageClubMembers(currentUser)
    val canDeleteClub = isAdmin || normalizedRole == "studentGuide" || normalizedRole == "superAdmin"
    val canManageRoles = canManageSettings

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text("Settings") },
                actions = {
                    if (canManageSettings && club != null) {
                        IconButton(onClick = {
                            val updated = club!!.copy(
                                name = name,
                                description = description,
                                logoUrl = logoUrl,
                                isPublic = isPublic
                            )
                            viewModel.updateClub(updated)
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save"
                            )
                        }
                    }
                }
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Club Info Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "Club Info",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = logoUrl ?: "",
                                    contentDescription = "Club Logo",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onSurface
                                                .copy(alpha = 0.1f)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = "Change Logo",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.clickable {
                                        // TODO: Launch image picker and assign URI to `logoUrl`
                                    }
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Club Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = canManageSettings
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description ?: "",
                                onValueChange = { description = it },
                                label = { Text("Description") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                enabled = canManageSettings
                            )
                        }
                    }
                }

                // Visibility & Privacy Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Visibility & Privacy",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Open Club",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Anyone can view and join",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(
                                    checked = isPublic,
                                    onCheckedChange = { isPublic = it },
                                    enabled = canManageSettings
                                )
                            }
                        }
                    }
                }

                if (canManageRoles) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(
                                            NavigationItem.ManageClubRoles.createRoute(categoryId, clubId)
                                        )
                                    },
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Manage Roles",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                // Danger Zone (Admins Only)
                if (canDeleteClub) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            elevation = CardDefaults.cardElevation()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Danger Zone",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.error
                                    )
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.deleteClub(categoryId, clubId)
                                        navController.navigate(
                                            NavigationItem.SingleCategory.createRoute(categoryId)
                                        ) {
                                            popUpTo(NavigationItem.AllClubs.route) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Club"
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Delete Club",
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
