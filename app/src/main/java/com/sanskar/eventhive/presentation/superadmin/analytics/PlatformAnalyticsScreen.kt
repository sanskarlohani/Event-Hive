package com.sanskar.eventhive.presentation.superadmin.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanskar.eventhive.presentation.common.EmptyScreen
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PlatformAnalyticsScreen(
    viewModel: PlatformAnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val range by viewModel.range.collectAsStateWithLifecycle()

    val onRange7 = remember(viewModel) { { viewModel.load(AnalyticsRange.DAYS_7) } }
    val onRange30 = remember(viewModel) { { viewModel.load(AnalyticsRange.DAYS_30) } }
    val onRangeAll = remember(viewModel) { { viewModel.load(AnalyticsRange.ALL_TIME) } }

    Scaffold(topBar = { TopAppBar(title = { Text("Platform Analytics") }) }) { padding ->
        when (val state = uiState) {
            is PlatformAnalyticsUiState.Loading -> LoadingScreen()
            is PlatformAnalyticsUiState.Error -> ErrorScreen(state.message) { viewModel.load(range) }
            is PlatformAnalyticsUiState.Success -> {
                if (state.rows.isEmpty()) {
                    EmptyScreen("No analytics data")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = range == AnalyticsRange.DAYS_7, onClick = onRange7, label = { Text("7 days") })
                            FilterChip(selected = range == AnalyticsRange.DAYS_30, onClick = onRange30, label = { Text("30 days") })
                            FilterChip(selected = range == AnalyticsRange.ALL_TIME, onClick = onRangeAll, label = { Text("All time") })
                        }
                        Text("Users: ${state.stats.totalUsers}")
                        Text("Events: ${state.stats.totalEvents}")
                        Text("Tickets: ${state.stats.totalTickets}")
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.rows, key = { it.id }) { row ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(row.collegeName)
                                    Text("Users ${row.users} | Events ${row.events} | Bookings ${row.bookings}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
