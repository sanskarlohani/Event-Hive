package com.sanskar.eventhive.presentation.collegeadmin.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.presentation.common.EmptyScreen
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen
import com.sanskar.eventhive.ui.components.BottomBarScaffold

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CollegeAdminDashboardScreen(
    navController: NavController,
    viewModel: CollegeAdminDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    BottomBarScaffold(
        navController = navController,
        topBar = { TopAppBar(title = { Text("College Admin Dashboard") }) },
    ) { padding ->
        when (val state = uiState) {
            is CollegeAdminDashboardUiState.Loading -> LoadingScreen()
            is CollegeAdminDashboardUiState.Error -> ErrorScreen(state.message) { viewModel.load() }
            is CollegeAdminDashboardUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Total members: ${state.membersCount}")
                    Text("Upcoming events: ${state.upcomingEventsCount}")
                    Text("Active clubs: ${state.activeClubsCount}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Manage Roles", modifier = Modifier.clickable { navController.navigate("college_admin/roles") })
                        Text("View Members", modifier = Modifier.clickable { navController.navigate("college_admin/members") })
                        Text("Manage Code", modifier = Modifier.clickable { navController.navigate("college_admin/college_code") })
                        Text("View Reports", modifier = Modifier.clickable { navController.navigate("college_admin/reports") })
                    }
                    if (state.recentEvents.isEmpty()) {
                        EmptyScreen("No recent activity")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.recentEvents, key = { it.eventId }) { event ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(event.title)
                                        Text(event.description)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
