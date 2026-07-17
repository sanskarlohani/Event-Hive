package com.sanskar.eventhive.presentation.collegeadmin.roles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanskar.eventhive.data.model.Role
import com.sanskar.eventhive.presentation.admin.PermissionKeys
import com.sanskar.eventhive.presentation.common.AccessDeniedScreen
import com.sanskar.eventhive.presentation.common.EmptyScreen
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleManagerScreen(
    viewModel: RoleManagerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSheet by remember { mutableStateOf(false) }
    var editRole by remember { mutableStateOf<Role?>(null) }
    var roleToDelete by remember { mutableStateOf<Role?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    when (val state = uiState) {
        is RoleManagerUiState.Loading -> LoadingScreen()
        is RoleManagerUiState.Error -> ErrorScreen(state.message) { viewModel.load() }
        is RoleManagerUiState.Success -> {
            if (!state.isStudentGuide) {
                AccessDeniedScreen()
            } else {
                val onFab = remember { { editRole = null; showSheet = true } }
                Scaffold(
                    topBar = { TopAppBar(title = { Text("Role Manager") }) },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    floatingActionButton = {
                        FloatingActionButton(onClick = onFab) {
                            Icon(Icons.Default.Add, contentDescription = "Create role")
                        }
                    },
                ) { padding ->
                    if (state.roles.isEmpty()) {
                        EmptyScreen("No roles created yet")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.roles, key = { it.id }) { role ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(role.name)
                                    Text(role.permissions.filterValues { it }.keys.joinToString())
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = { editRole = role; showSheet = true }) { Text("Edit") }
                                        TextButton(onClick = { roleToDelete = role }) { Text("Delete") }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showSheet) {
                    CreateEditRoleBottomSheet(
                        initialRole = editRole,
                        onSave = { name, permissions, role ->
                            if (role == null) viewModel.createRole(name, permissions)
                            else viewModel.updateRole(role.copy(name = name, permissions = permissions))
                            showSheet = false
                        },
                        onCancel = { showSheet = false },
                    )
                }

                roleToDelete?.let { role ->
                    AlertDialog(
                        onDismissRequest = { roleToDelete = null },
                        title = { Text("Delete role") },
                        text = { Text("Delete ${role.name}?") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteRole(role.id)
                                roleToDelete = null
                            }) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { roleToDelete = null }) { Text("Cancel") }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEditRoleBottomSheet(
    initialRole: Role?,
    onSave: (String, Map<String, Boolean>, Role?) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember(initialRole?.id) { mutableStateOf(initialRole?.name.orEmpty()) }
    val permissions = remember(initialRole?.id) {
        mutableStateMapOf<String, Boolean>().apply {
            PermissionKeys.forEach { key -> put(key, initialRole?.permissions?.get(key) == true) }
        }
    }

    ModalBottomSheet(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Role name") },
                modifier = Modifier.fillMaxWidth(),
            )
            PermissionKeys.forEach { key ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(key)
                    Checkbox(
                        checked = permissions[key] == true,
                        onCheckedChange = { permissions[key] = it },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSave(name, permissions.toMap(), initialRole) }, modifier = Modifier.weight(1f)) {
                    Text("Save")
                }
                Button(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
            }
        }
    }
}
