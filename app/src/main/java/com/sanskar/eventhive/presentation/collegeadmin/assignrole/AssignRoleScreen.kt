package com.sanskar.eventhive.presentation.collegeadmin.assignrole

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AssignRoleScreen(
    navController: NavController,
    uid: String,
    viewModel: AssignRoleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uid) { viewModel.load(uid) }
    LaunchedEffect(Unit) {
        viewModel.events.collect {
            snackbarHostState.showSnackbar(it)
            if (it == "Roles updated") navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Assign Roles") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val state = uiState) {
            is AssignRoleUiState.Loading -> LoadingScreen()
            is AssignRoleUiState.Error -> ErrorScreen(state.message) { viewModel.load(uid) }
            is AssignRoleUiState.Success -> {
                if (state.roles.isEmpty()) {
                    EmptyScreen("No roles available")
                } else {
                    val onSave = remember(viewModel) { { viewModel.save() } }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.roles, key = { it.id }) { role ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(role.name)
                                    Checkbox(
                                        checked = state.selectedRoleIds.contains(role.id),
                                        onCheckedChange = { viewModel.toggle(role.id) },
                                    )
                                }
                            }
                        }
                        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
