package com.sanskar.eventhive.presentation.collegeadmin.members

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CollegeMemberListScreen(
    navController: NavController,
    collegeId: String?,
    viewModel: CollegeMemberListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()

    LaunchedEffect(collegeId) {
        viewModel.loadInitial(collegeId)
    }

    val onRetry = remember(viewModel, collegeId) { { viewModel.loadInitial(collegeId) } }

    Scaffold(topBar = { TopAppBar(title = { Text("College Members") }) }) { padding ->
        when (val state = uiState) {
            is CollegeMemberListUiState.Loading -> LoadingScreen()
            is CollegeMemberListUiState.Error -> ErrorScreen(state.message, onRetry)
            is CollegeMemberListUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { viewModel.setSearch(it) },
                        label = { Text("Search member") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                    if (state.items.isEmpty()) {
                        EmptyScreen("No members found")
                    } else {
                        val onEndReached = remember(viewModel) { { viewModel.loadNextPage() } }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.items, key = { it.userId.ifBlank { it.email } }) { user ->
                                val uid = user.userId.ifBlank { user.email }
                                val onItemClick = remember(navController, uid) {
                                    { navController.navigate("college_admin/assign_role/$uid") }
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onItemClick)
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                ) {
                                    Text(user.name)
                                    Text(user.email)
                                    Text("systemRole: ${user.systemRole}")
                                    Text("customRoles: ${user.customRoleIds.joinToString()}")
                                }
                            }
                            item {
                                onEndReached()
                                if (isLoadingMore) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
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
