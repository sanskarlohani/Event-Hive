package com.sanskar.eventhive.ui.screen.Club.Event


import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.ui.viewModel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailedScreen(
    ticketId: String,
    teamId: String,
    navController: NavController,
    ticketViewModel: TicketViewModel = hiltViewModel()
) {
    // Observe teams for this event
    val team by ticketViewModel.team.collectAsStateWithLifecycle(initialValue = null)

    // Trigger load
    LaunchedEffect(ticketId) {
        ticketViewModel.getTeamForTicket(ticketId,teamId)
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
        Column (
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(8.dp)
        ){

        }
    }
}
