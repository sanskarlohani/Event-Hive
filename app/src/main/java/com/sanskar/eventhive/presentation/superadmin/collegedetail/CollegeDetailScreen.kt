package com.sanskar.eventhive.presentation.superadmin.collegedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CollegeDetailScreen(
    navController: NavController,
    collegeId: String,
    viewModel: CollegeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(collegeId) {
        viewModel.start(collegeId)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("College Detail") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val state = uiState) {
            is CollegeDetailUiState.Loading -> LoadingScreen()
            is CollegeDetailUiState.Error -> ErrorScreen(message = state.message, onRetry = { viewModel.start(collegeId) })
            is CollegeDetailUiState.Success -> {
                var editableCode by remember(state.college.collegeCode) { mutableStateOf(state.college.collegeCode) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Name: ${state.college.name}")
                    Text("Domain: ${state.college.emailDomain ?: "No domain"}")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active")
                        Switch(checked = state.college.isActive, onCheckedChange = { viewModel.toggleActive() })
                    }
                    Text("Student Guide: ${state.college.studentGuideName}")
                    Text("Guide Email: ${state.college.studentGuideEmail}")
                    OutlinedTextField(
                        value = editableCode,
                        onValueChange = { editableCode = it.uppercase().take(6) },
                        label = { Text("College Code") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = { viewModel.updateCode(editableCode) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Edit Code")
                    }
                    Text("Total students: ${state.stats.totalStudents}")
                    Text("Total events: ${state.stats.totalEvents}")
                    Text("Total clubs: ${state.stats.totalClubs}")
                    Button(
                        onClick = { navController.navigate("college_admin/members?collegeId=${state.college.id}") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("View Members")
                    }
                }
            }
        }
    }
}
