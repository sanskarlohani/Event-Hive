package com.sanskar.eventhive.ui.screen.User

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserJoinedClubScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    clubViewModel: ClubViewModel = hiltViewModel()
) {
    val userResource by userViewModel.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)
    val joinedClubRefs by userViewModel.joinedClubRefs.collectAsStateWithLifecycle()
    val clubs by clubViewModel.allClubs.collectAsStateWithLifecycle()
    val currentUserId = (userResource as? Resource.Success)?.data?.userId.orEmpty()

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            userViewModel.observeJoinedClubs(currentUserId)
        }
    }

    val joinedClubs = remember(clubs, currentUserId, joinedClubRefs) {
        clubs.filter { club ->
            club.members.contains(currentUserId) ||
                joinedClubRefs.any { ref -> ref.clubId == club.clubId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Joined Clubs") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (joinedClubs.isEmpty()) {
                Text(
                    text = "No joined clubs found",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(joinedClubs, key = { it.clubId }) { club ->
                        JoinedClubCard(club = club) {
                            navController.navigate(
                                NavigationItem.ClubDetail.createRoute(club.categoryId, club.clubId)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JoinedClubCard(
    club: Club,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = club.name,
                style = MaterialTheme.typography.titleMedium,
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
            Text(
                text = "${club.members.size} members",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
