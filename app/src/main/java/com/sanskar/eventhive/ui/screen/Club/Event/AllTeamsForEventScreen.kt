package com.sanskar.eventhive.ui.screen.Club.Event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.ui.viewModel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTeamsForEventScreen(
    categoryId: String,
    clubId: String,
    eventId: String,
    navController: NavController,
    ticketViewModel: TicketViewModel = hiltViewModel()
) {
    // Observe teams for this event
    val teams by ticketViewModel.teamsForEvent.collectAsStateWithLifecycle(initialValue = emptyList())

    // Trigger load
    LaunchedEffect(eventId) {
        ticketViewModel.getTeamsForEvent(categoryId, clubId, eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("All Teams")
                },
                navigationIcon = {
                    navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {

            if (teams.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "No teams have registered yet.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(teams, key = { it.teamId }) { team ->
                        // For now, use teamLeaderId as leaderName placeholder
                        TeamCard(
                            team = team,
                        ) {
//                            navController.navigate(NavigationItem.TeamDetail.createRoute(team.teamId, team.teamId))
                        }
                    }
                }
            }
        }
    }
}