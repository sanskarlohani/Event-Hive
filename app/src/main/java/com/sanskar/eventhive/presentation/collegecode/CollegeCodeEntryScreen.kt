package com.sanskar.eventhive.presentation.collegecode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.ui.navigation.NavigationItem

@Composable
fun CollegeCodeEntryScreen(
    navController: NavController,
    viewModel: CollegeCodeEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val code by viewModel.code.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkAutoAssign()
    }

    LaunchedEffect(uiState) {
        if (uiState is CollegeCodeEntryUiState.Success) {
            navController.navigate(NavigationItem.Home.route) {
                popUpTo(0)
            }
        }
    }

    val onCodeChange = remember(viewModel) { { value: String ->
        viewModel.clearError()
        viewModel.setCode(value)
    } }
    val onConfirm = remember(viewModel) { { viewModel.confirmCode() } }
    val onSkip = remember(navController) {
        {
            navController.navigate(NavigationItem.Home.route) {
                popUpTo(0)
            }
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Enter College Code",
                    style = MaterialTheme.typography.headlineSmall,
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    singleLine = true,
                    supportingText = {
                        when (uiState) {
                            is CollegeCodeEntryUiState.InvalidCode -> Text("Invalid code")
                            is CollegeCodeEntryUiState.Error -> Text((uiState as CollegeCodeEntryUiState.Error).message)
                            else -> Text("Code must be 6 uppercase characters")
                        }
                    },
                )
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is CollegeCodeEntryUiState.Loading,
                ) {
                    if (uiState is CollegeCodeEntryUiState.Loading) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        Text("Confirm")
                    }
                }
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Skip for now")
                }
            }
        }
    }
}
