package com.sanskar.eventhive.presentation.collegeadmin.codemanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CollegeCodeManagerScreen(
    viewModel: CollegeCodeManagerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("College Code Manager") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val state = uiState) {
            is CollegeCodeManagerUiState.Loading -> LoadingScreen()
            is CollegeCodeManagerUiState.Error -> ErrorScreen(state.message) { viewModel.load() }
            is CollegeCodeManagerUiState.Success -> {
                var code by remember(state.code) { mutableStateOf(state.code) }
                val onSave = remember(viewModel, code) { { viewModel.updateCode(code) } }
                val onGenerate = remember(viewModel) { { code = viewModel.autoGenerate() } }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Current code: ${state.code}")
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase().take(6) },
                        label = { Text("New Code") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) { Text("Auto-generate") }
                    Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save") }
                }
            }
        }
    }
}
