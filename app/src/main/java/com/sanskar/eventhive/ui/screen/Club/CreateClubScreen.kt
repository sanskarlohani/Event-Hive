package com.sanskar.eventhive.ui.screen.Club

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Category
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.data.model.ClubRole
import com.sanskar.eventhive.data.model.ClubUser
import com.sanskar.eventhive.ui.components.ClayButton
import com.sanskar.eventhive.ui.components.ClayCard
import com.sanskar.eventhive.ui.permissions.canCreateClub
import com.sanskar.eventhive.ui.viewModel.ClubCategoryViewModel
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    categoryId: String,
    userId: String,
    navController: NavController,
    clubViewModel: ClubViewModel = hiltViewModel(),
    categoryViewModel: ClubCategoryViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val selectCategoryToken = "__select_category__"
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)
    val currentUser = (currentUserRes as? Resource.Success)?.data
    val canCreateClubAccess = canCreateClub(currentUser)
    val requiresCategorySelection = (categoryId == selectCategoryToken) || categoryId.isBlank()

    // State
    var clubName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(value = true) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var selectedCategoryName by remember {
        mutableStateOf(
            if (requiresCategorySelection) "" else categories.firstOrNull { it.categoryId == categoryId }?.name.orEmpty()
        )
    }
    var customDefaultCategoryOne by remember { mutableStateOf("") }
    var customDefaultCategoryTwo by remember { mutableStateOf("") }

    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }

    // Image pickers
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        logoUri = uri
    }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        bannerUri = uri
    }

    // Operation result
    val operationStatus by clubViewModel.operationStatus.collectAsStateWithLifecycle(null)
    LaunchedEffect(operationStatus) {
        if (operationStatus is Resource.Success) {
            Toast.makeText(context, "Club Created!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    val defaultCategoryChoices = remember(customDefaultCategoryOne, customDefaultCategoryTwo) {
        buildList {
            add("Tech")
            add("Sports")
            add("Music")
            if (customDefaultCategoryOne.isNotBlank()) add(customDefaultCategoryOne.trim())
            if (customDefaultCategoryTwo.isNotBlank()) add(customDefaultCategoryTwo.trim())
        }.distinct()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Create Club", 
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = colors.primary
                        )
                        Text(
                            "Build your own community", 
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.05f),
                            colors.background,
                            colors.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ClayCard(
                    cornerRadius = 24.dp,
                    elevation = 10.dp,
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Basic Details", 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        
                        OutlinedTextField(
                            value = clubName,
                            onValueChange = { clubName = it },
                            label = { Text("Club Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4,
                            shape = RoundedCornerShape(16.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Public Club", fontWeight = FontWeight.Bold)
                                Text("Anyone can find and join", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                            Switch(
                                checked = isPublic,
                                onCheckedChange = { isPublic = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                            )
                        }
                    }
                }

                if (requiresCategorySelection) {
                    ClayCard(
                        cornerRadius = 24.dp,
                        elevation = 8.dp,
                        backgroundColor = colors.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Category Selection", 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            
                            ExposedDropdownMenuBox(
                                expanded = categoryMenuExpanded,
                                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                            ) {
                                OutlinedTextField(
                                    value = selectedCategoryName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Choose Category") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                DropdownMenu(
                                    expanded = categoryMenuExpanded,
                                    onDismissRequest = { categoryMenuExpanded = false },
                                ) {
                                    defaultCategoryChoices.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedCategoryName = option
                                                categoryMenuExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = customDefaultCategoryOne,
                                onValueChange = { customDefaultCategoryOne = it },
                                label = { Text("Add Custom Suggestion") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }

                Text("Club Branding", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Logo Box
                    Box(modifier = Modifier.weight(1f)) {
                        ClayCard(
                            cornerRadius = 20.dp,
                            elevation = 6.dp,
                            backgroundColor = colors.surface,
                            onClick = { logoPicker.launch("image/*") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoUri != null) {
                                    AsyncImage(
                                        model = logoUri,
                                        contentDescription = "Logo",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = colors.primary)
                                        Text("Logo", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    // Banner Box
                    Box(modifier = Modifier.weight(1.5f)) {
                        ClayCard(
                            cornerRadius = 20.dp,
                            elevation = 6.dp,
                            backgroundColor = colors.surface,
                            onClick = { bannerPicker.launch("image/*") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bannerUri != null) {
                                    AsyncImage(
                                        model = bannerUri,
                                        contentDescription = "Banner",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = colors.primary)
                                        Text("Banner", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                ClayButton(
                    text = "Create Club",
                    onClick = {
                        if (!canCreateClubAccess) {
                            Toast.makeText(context, "No permission", Toast.LENGTH_SHORT).show()
                            return@ClayButton
                        }
                        coroutineScope.launch {
                            val resolvedCategoryId = if (requiresCategorySelection) {
                                val selectedName = selectedCategoryName.trim()
                                if (selectedName.isBlank()) {
                                    Toast.makeText(context, "Choose category", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val existingCategory = categories.firstOrNull { it.name.equals(selectedName, ignoreCase = true) }
                                if (existingCategory != null) {
                                    existingCategory.categoryId
                                } else {
                                    val newCategory = Category(
                                        categoryId = System.currentTimeMillis().toString(),
                                        name = selectedName,
                                    )
                                    when (categoryViewModel.saveCategoryDirect(newCategory)) {
                                        is Resource.Success -> newCategory.categoryId
                                        is Resource.Error -> {
                                            Toast.makeText(context, "Category fail", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        else -> return@launch
                                    }
                                }
                            } else {
                                categoryId
                            }

                            val id = System.currentTimeMillis().toString()
                            val clubUser = ClubUser(clubId = id, categoryId = resolvedCategoryId, userId = userId, role = ClubRole.ADMIN)
                            val club = Club(
                                clubId = id,
                                name = clubName,
                                categoryId = resolvedCategoryId,
                                description = description,
                                logoUrl = logoUri?.toString(),
                                bannerUrl = bannerUri?.toString(),
                                isPublic = isPublic,
                            )
                            clubViewModel.createClub(club, clubUser)
                        }
                    },
                    enabled = clubName.isNotBlank() && canCreateClubAccess && (operationStatus !is Resource.Loading),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
                
                if (operationStatus is Resource.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = colors.primary)
                }
            }
        }
    }
}
