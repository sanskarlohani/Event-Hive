package com.sanskar.eventhive.ui.screen.Club

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.ClubRole
import com.sanskar.eventhive.ui.components.ClayCard
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
    val operationStatus by viewModel.operationStatus.collectAsState(initial = Resource.Idle)
    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle()
    val currentUser = (currentUserRes as? Resource.Success)?.data

    // Trigger initial load
    LaunchedEffect(Unit) {
        viewModel.getClub(categoryId, clubId)
        viewModel.getClubMember(categoryId, clubId, userId)
        viewModel.getAllClubMembers(categoryId, clubId)
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

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { logoUrl = it.toString() }
    }

    val isAdmin = clubMember?.role == ClubRole.ADMIN
    val normalizedRole = normalizeSystemRole(currentUser?.systemRole)
    val canManageSettings = isAdmin ||
        normalizedRole == "studentGuide" ||
        normalizedRole == "superAdmin" ||
        canManageClubMembers(currentUser)
    val canDeleteClub = isAdmin || normalizedRole == "studentGuide" || normalizedRole == "superAdmin"

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
                        IconButton(
                            enabled = operationStatus !is Resource.Loading,
                            onClick = {
                                val updated = club!!.copy(
                                    name = name.trim(),
                                    description = description?.trim(),
                                    logoUrl = logoUrl,
                                    isPublic = isPublic,
                                    editedAt = System.currentTimeMillis()
                                )
                                viewModel.updateClub(updated)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (operationStatus is Resource.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LaunchedEffect(operationStatus) {
                if (operationStatus is Resource.Success) {
                    navController.popBackStack()
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Club Info Card
                item {
                    ClayCard(
                        cornerRadius = 20.dp,
                        elevation = 8.dp,
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(Modifier.padding(4.dp)) {
                            Text(
                                text = "Club Info",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                        .clickable(enabled = canManageSettings) {
                                            logoPicker.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = logoUrl ?: com.sanskar.eventhive.R.drawable.ic_launcher_foreground,
                                        contentDescription = "Club Logo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = "Change Logo",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.clickable(enabled = canManageSettings) {
                                        logoPicker.launch("image/*")
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
                                enabled = canManageSettings,
                                shape = RoundedCornerShape(16.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = description ?: "",
                                onValueChange = { description = it },
                                label = { Text("Description") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                enabled = canManageSettings,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }

                // Visibility & Privacy Card
                item {
                    ClayCard(
                        cornerRadius = 20.dp,
                        elevation = 8.dp,
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Visibility & Privacy",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
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

                if (canManageSettings) {
                    item {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            elevation = 8.dp,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            onClick = {
                                navController.navigate(
                                    NavigationItem.ManageClubRoles.createRoute(categoryId, clubId)
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Manage Roles",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Check, // Reusing check as a "go" icon or could use Chevron
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Danger Zone (Admins Only)
                if (canDeleteClub) {
                    item {
                        ClayCard(
                            cornerRadius = 20.dp,
                            elevation = 8.dp,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Danger Zone",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(Modifier.height(12.dp))
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
                                    ),
                                    shape = RoundedCornerShape(12.dp)
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
    }
}
