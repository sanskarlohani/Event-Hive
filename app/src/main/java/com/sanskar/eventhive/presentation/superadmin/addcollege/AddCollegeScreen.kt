package com.sanskar.eventhive.presentation.superadmin.addcollege

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavController

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddCollegeScreen(
    navController: NavController,
    viewModel: AddCollegeViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var listedCoursesInput by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var code by remember { mutableStateOf(viewModel.autoGenerateCode()) }
    var guideName by remember { mutableStateOf("") }
    var guideEmail by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val codeAvailable by viewModel.codeAvailable.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AddCollegeUiState.Success -> {
                snackbarHostState.showSnackbar("College added")
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("college_added", true)
                navController.popBackStack()
            }
            is AddCollegeUiState.Error -> snackbarHostState.showSnackbar((uiState as AddCollegeUiState.Error).message)
            else -> Unit
        }
    }

    val onAutoCode = remember(viewModel) { { code = viewModel.autoGenerateCode() } }
    val onCheckCode = remember(viewModel, code) { { viewModel.validateCode(code) } }
    val onSubmit = remember(viewModel, name, listedCoursesInput, domain, code, guideName, guideEmail) {
        {
            val listedCourses = listedCoursesInput
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
            viewModel.submit(
                name = name,
                listedCourses = listedCourses,
                domain = domain.ifBlank { null },
                code = code,
                guideName = guideName,
                guideEmail = guideEmail,
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add College") },
                actions = {
                    IconButton(onClick = onAutoCode) {
                        Icon(Icons.Default.Refresh, contentDescription = "Auto-generate")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("College name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = listedCoursesInput,
                onValueChange = { listedCoursesInput = it },
                label = { Text("Listed courses (comma-separated)") },
                supportingText = { Text("Example: BTech CSE, BTech ECE, MBA") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(value = domain, onValueChange = { domain = it }, label = { Text("Email domain (optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(6) },
                label = { Text("College code") },
                supportingText = {
                    when (codeAvailable) {
                        false -> Text("Code already taken", color = MaterialTheme.colorScheme.error)
                        true -> Text("Code available")
                        null -> Text("6-char code")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = onCheckCode, modifier = Modifier.fillMaxWidth()) {
                Text("Check code availability")
            }
            OutlinedTextField(value = guideName, onValueChange = { guideName = it }, label = { Text("Student Guide full name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = guideEmail, onValueChange = { guideEmail = it }, label = { Text("Student Guide email") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddCollegeUiState.Loading,
            ) {
                if (uiState is AddCollegeUiState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Submit")
                }
            }
        }
    }
}
