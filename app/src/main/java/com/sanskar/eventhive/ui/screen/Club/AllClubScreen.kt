package com.sanskar.eventhive.ui.screen.Club

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.ui.components.BottomBarScaffold
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.permissions.canCreateClub
import com.sanskar.eventhive.ui.theme.AppDimens
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel

private enum class ClubTab(val label: String) {
    ALL("All"),
    JOINED("Joined"),
    POPULAR("Popular"),
    NEARBY("Nearby")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllClubScreen(
    navController: NavController,
    viewModel: ClubViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val allClubs by viewModel.allClubs.collectAsStateWithLifecycle()
    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)
    val userId = (currentUserRes as? Resource.Success)?.data?.userId.orEmpty()
    val currentUser = (currentUserRes as? Resource.Success)?.data
    val canCreateClub = canCreateClub(currentUser)

    var selectedTab by remember { mutableStateOf(ClubTab.ALL) }
    var showFilters by remember { mutableStateOf(false) }
    var onlyPublic by remember { mutableStateOf(false) }

    val filteredClubs by remember(allClubs, selectedTab, userId, onlyPublic) {
        derivedStateOf {
            val byTab = when (selectedTab) {
                ClubTab.ALL -> allClubs
                ClubTab.JOINED -> allClubs.filter { it.members.contains(userId) }
                ClubTab.POPULAR -> allClubs.sortedByDescending { it.members.size }
                ClubTab.NEARBY -> allClubs
            }
            if (onlyPublic) byTab.filter { it.isPublic } else byTab
        }
    }

    BottomBarScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Discover Clubs",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter clubs")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (canCreateClub) {
                FloatingActionButton(
                    onClick = { navController.navigate(NavigationItem.CreateClub.createRoute("__select_category__")) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create club")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = AppDimens.ScreenHorizontalPadding,
                vertical = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                TabStrip(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it }
                )
            }

            if (filteredClubs.isEmpty()) {
                item {
                    EmptyStateCard("No clubs found in this filter.")
                }
            } else {
                items(filteredClubs, key = { it.clubId }) { club ->
                    ClubDiscoveryCard(
                        club = club,
                        isJoined = userId.isNotBlank() && club.members.contains(userId),
                        onJoinClick = {
                            if (userId.isNotBlank() && !club.members.contains(userId)) {
                                viewModel.joinClub(club.categoryId, club.clubId, userId)
                            }
                        },
                        onOpenClick = {
                            navController.navigate(
                                NavigationItem.ClubDetail.createRoute(club.categoryId, club.clubId)
                            )
                        }
                    )
                }
            }
        }

        if (showFilters) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Filters", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Visibility",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onlyPublic = false }) {
                            Text("All")
                        }
                        FilledTonalButton(onClick = { onlyPublic = true }) {
                            Text("Open Only")
                        }
                    }
                    TextButton(
                        onClick = { showFilters = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun TabStrip(
    selectedTab: ClubTab,
    onTabSelect: (ClubTab) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        ClubTab.entries.forEach { tab ->
            Column(
                modifier = Modifier.clickable { onTabSelect(tab) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selectedTab == tab) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedTab == tab) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                )
            }
        }
    }
}

@Composable
private fun ClubDiscoveryCard(
    club: Club,
    isJoined: Boolean,
    onJoinClick: () -> Unit,
    onOpenClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 124.dp, height = 96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                Color(0xFF8C74FF)
                            )
                        )
                    )
            ) {
                if (!club.bannerUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = club.bannerUrl,
                        contentDescription = club.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (!club.logoUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = club.logoUrl,
                            contentDescription = "${club.name} logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = club.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = club.description.orEmpty().ifBlank { "No description available." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${club.members.size} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (club.isPublic) "Open club" else "Closed club",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
            if (isJoined) {
                FilledTonalButton(onClick = onOpenClick) {
                    Text("Open")
                }
            } else {
                OutlinedButton(onClick = onJoinClick) {
                    Text("Join")
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}
