package com.sanskar.eventhive.ui.screen.Club

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    val club by clubViewModel.club.collectAsStateWithLifecycle()
    val operationStatus by clubViewModel.operationStatus.collectAsState(initial = Resource.Idle)

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf<String?>(null) }
    var bannerUrl by remember { mutableStateOf<String?>(null) }

    // Image pickers
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { logoUrl = it.toString() }
    }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { bannerUrl = it.toString() }
    }

    // Load existing club data
    LaunchedEffect(categoryId, clubId) {
        clubViewModel.getClub(categoryId, clubId)
    }

    // Sync form state when club data arrives
    LaunchedEffect(club) {
        club?.let {
            name = it.name
            description = it.description.orEmpty()
            logoUrl = it.logoUrl
            bannerUrl = it.bannerUrl
        }
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
                val currentClub = club ?: return@FloatingActionButton
                val updated = currentClub.copy(
                    name = name.trim(),
                    description = description.trim().takeIf { it.isNotBlank() },
                    logoUrl = logoUrl,
                    bannerUrl = bannerUrl,
                    editedAt = System.currentTimeMillis()
                )
                clubViewModel.updateClub(updated)
            }) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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


            Text("Club Logo", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable { logoPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (!logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = "Club Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(32.dp))
                        Text("Tap to select logo", color = Color.Gray)
                    }
                }
            }

            Text("Banner Image", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable { bannerPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (!bannerUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = bannerUrl,
                        contentDescription = "Club Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(32.dp))
                        Text("Tap to select banner", color = Color.Gray)
                    }
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
