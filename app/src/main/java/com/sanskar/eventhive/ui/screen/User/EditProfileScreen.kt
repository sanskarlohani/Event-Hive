package com.sanskar.eventhive.ui.screen.User

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsStateWithLifecycle()
    val operationStatus by userViewModel.operationStatus.collectAsStateWithLifecycle(initialValue = Resource.Idle)

    var name by remember { mutableStateOf("") }
    var registrationNo by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Initialize form fields when user data is loaded
    LaunchedEffect(currentUser) {
        if (currentUser is Resource.Success) {
            val user = (currentUser as Resource.Success<User>).data
            name = user?.name ?: ""
            email = user?.email ?: ""
            registrationNo = user?.registrationNo?.ifBlank { user.sic } ?: ""
            phone = user?.phone ?: ""
            profileImageUrl = user?.profileImageUrl ?: ""
        }
    }

    // Handle operation status
    LaunchedEffect(operationStatus) {
        when (operationStatus) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                showSuccessDialog = true
            }
            is Resource.Error -> {
                isLoading = false
                errorMessage = (operationStatus as Resource.Error).exception.message ?: "Unknown error occurred"
                showErrorDialog = true
            }
            else -> isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentUser) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = "Error loading profile: ${(currentUser as Resource.Error).exception.message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                is Resource.Success -> {
                    val user = (currentUser as Resource.Success<User>).data
                    if (user != null) {
                        // Profile Form
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Personal Information",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Profile Picture Section
                                val context = LocalContext.current
                                var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
                                var showImageOptions by remember { mutableStateOf(false) }

                                val launcher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.GetContent()
                                ) { uri: Uri? ->
                                    uri?.let {
                                        selectedImageUri = it
                                        // Convert URI to string for storage (you might want to upload to cloud storage)
                                        profileImageUrl = it.toString()
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Profile Picture Display
                                    Card(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clickable { showImageOptions = true },
                                        shape = CircleShape,
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when {
                                                selectedImageUri != null -> {
                                                    // Show selected image from gallery
                                                    AsyncImage(
                                                        model = selectedImageUri,
                                                        contentDescription = "Selected Profile Image",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                profileImageUrl.isNotEmpty() -> {
                                                    // Show existing profile image from URL
                                                    AsyncImage(
                                                        model = profileImageUrl,
                                                        contentDescription = "Current Profile Image",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop,
                                                        onError = {
                                                            // If URL fails to load, show placeholder
                                                        }
                                                    )
                                                }
                                                else -> {
                                                    // Show placeholder when no image
                                                    Icon(
                                                        Icons.Default.Person,
                                                        contentDescription = "No Profile Image",
                                                        modifier = Modifier.size(48.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            // Camera/Edit overlay
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(36.dp)
                                                    .offset(x = (-10).dp, y = (-10).dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primary,
                                                        CircleShape
                                                    )
                                                    .border(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.surface,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Image",
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Tap to change profile picture",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Image Options Bottom Sheet
                                if (showImageOptions) {
                                    AlertDialog(
                                        onDismissRequest = { showImageOptions = false },
                                        title = { Text("Change Profile Picture") },
                                        text = {
                                            Column {
                                                Text("Choose how you want to update your profile picture:")
                                                Spacer(modifier = Modifier.height(16.dp))

                                                // Gallery Option
                                                TextButton(
                                                    onClick = {
                                                        showImageOptions = false
                                                        launcher.launch("image/*")
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(
                                                        Icons.Default.PhotoLibrary,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Choose from Gallery")
                                                }

                                                // Remove Picture Option (if there's a current image)
                                                if (profileImageUrl.isNotEmpty() || selectedImageUri != null) {
                                                    TextButton(
                                                        onClick = {
                                                            showImageOptions = false
                                                            selectedImageUri = null
                                                            profileImageUrl = ""
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(20.dp),
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            "Remove Picture",
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { showImageOptions = false }) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }

                                // Name Field
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Full Name") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                // Email Field (Read-only)
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { },
                                    label = { Text("Email (Cannot be changed)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Email, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )

                                // Registration Number Field (Read-only)
                                OutlinedTextField(
                                    value = registrationNo,
                                    onValueChange = { },
                                    label = { Text("Registration No / Roll No (Cannot be changed)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Badge, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )

                                // Phone Field (Now Editable)
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text("Phone Number") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Phone, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Save Button
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val updatedUser = user.copy(
                                        name = name.trim(),
                                        phone = if (phone.isNotBlank()) phone.trim() else null,
                                        profileImageUrl = if (profileImageUrl.isNotBlank()) profileImageUrl.trim() else null
                                    )
                                    userViewModel.updateUser(updatedUser)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isLoading && name.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Changes")
                            }
                        }

                        // Validation message
                        if (name.isBlank()) {
                            Text(
                                "Please fill in the required field (Name)",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                else -> {
                    // Idle state - show loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigateUp()
            },
            title = { Text("Success") },
            text = { Text("Profile updated successfully!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("OK")
                }
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.Green
                )
            }
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            },
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}
