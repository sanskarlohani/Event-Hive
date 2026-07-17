package com.sanskar.eventhive.ui.screen.Club

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClubDetailScreen(
    navController: NavController,
    categoryId: String,
    clubId: String,
    clubViewModel: ClubViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val club by clubViewModel.club.collectAsStateWithLifecycle()
    val operationStatus by clubViewModel.operationStatus.collectAsState(initial = Resource.Idle)

    // Form state
    var name by remember { mutableStateOf(club?.name ?: "no name") }
    var description by remember { mutableStateOf(club?.description ?: "") }
    var logoUrl by remember { mutableStateOf(club?.logoUrl) }
    var bannerUrl by remember { mutableStateOf<String?>(club?.bannerUrl ?: "") }

    // Load existing club data
    LaunchedEffect(categoryId, clubId) {
        clubViewModel.getClub(categoryId, clubId)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Club") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Build updated Club object using current form state
                val existing = (club as? Resource.Success<Club>)?.data
                if (existing != null) {
                    val updated = existing.copy(
                        name = name,
                        description = description.takeIf { it.isNotBlank() },
                        logoUrl = logoUrl,
                        bannerUrl = bannerUrl,
                        editedAt = System.currentTimeMillis()
                    )
                    scope.launch {
                        clubViewModel.updateClub(updated)
                    }
                }
            }) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {


            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Club Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )


                Text("Logo Image")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFF202020), RoundedCornerShape(8.dp))
                        .clickable { /* TODO: launch image picker */ },
                    contentAlignment = Alignment.Center
                ) {
                    if (!logoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = "Club Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Tap to select logo", color = Color.Gray)
                    }
                }

                Text("Banner Image")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFF202020), RoundedCornerShape(8.dp))
                        .clickable { /* TODO: launch image picker */ },
                    contentAlignment = Alignment.Center
                ) {
                    if (!bannerUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = bannerUrl,
                            contentDescription = "Club Banner",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Tap to select banner", color = Color.Gray)
                    }
                }

                // Show operation status
                when (operationStatus) {
                    is Resource.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    is Resource.Success -> {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }

                    is Resource.Error -> {
                        Text(
                            text = "Save failed: ${(operationStatus as Resource.Error).exception.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> Unit
                }
            }
        }

    }
}
