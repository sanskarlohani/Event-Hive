package com.sanskar.eventhive.presentation.superadmin.collegelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.presentation.common.EmptyScreen
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen
import com.sanskar.eventhive.ui.components.BottomBarScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeListScreen(
    navController: NavController,
    viewModel: CollegeListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val collegeAdded by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("college_added", false)
        ?.collectAsStateWithLifecycle()
        ?: run { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(collegeAdded) {
        if (collegeAdded) {
            viewModel.refresh()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("college_added", false)
        }
    }

    val onRetry = remember(viewModel) { { viewModel.refresh() } }
    val onFabClick = remember(navController) { { navController.navigate("super_admin/add_college") } }

    BottomBarScaffold(
        navController = navController,
        topBar = { TopAppBar(title = { Text("Colleges") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Default.Add, contentDescription = "Add college")
            }
        },
    ) { padding ->
        when (val state = uiState) {
            is CollegeListUiState.Loading -> LoadingScreen()
            is CollegeListUiState.Error -> ErrorScreen(message = state.message, onRetry = onRetry)
            is CollegeListUiState.Success -> {
                if (state.items.isEmpty()) {
                    EmptyScreen("No colleges found")
                } else {
                    val onEndReached = remember(viewModel) { { viewModel.loadNextPage() } }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.items, key = { it.id }) { college ->
                            val onItemClick = remember(navController, college.id) {
                                { navController.navigate("super_admin/college_detail/${college.id}") }
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clickable(onClick = onItemClick),
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(college.name, style = MaterialTheme.typography.titleMedium)
                                    Text(college.emailDomain ?: "No domain")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text("Code: ${college.collegeCode}")
                                        Text(if (college.isActive) "Active" else "Inactive")
                                    }
                                }
                            }
                        }
                        item {
                            onEndReached()
                            if (isLoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
