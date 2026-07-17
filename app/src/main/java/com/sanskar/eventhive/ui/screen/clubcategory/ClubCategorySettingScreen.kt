package com.sanskar.eventhive.ui.screen.clubcategory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.graphics.vector.ImageVector
import com.sanskar.eventhive.data.model.Category
import com.sanskar.eventhive.ui.viewModel.ClubCategoryViewModel

// Define available icons and mapping helper
private val availableIcons = listOf(
    "Person" to Icons.Default.Person,
    "FlashOn" to Icons.Default.FlashOn,
    "Lightbulb" to Icons.Default.Lightbulb,
    "Laptop" to Icons.Default.Laptop,
    "Handshake" to Icons.Default.Handshake,
    "Language" to Icons.Default.Language,
    "Palette" to Icons.Default.Palette,
    "MusicNote" to Icons.Default.MusicNote,
    "MenuBook" to Icons.AutoMirrored.Filled.MenuBook,
    "EmojiEvents" to Icons.Default.EmojiEvents,
    "TheaterComedy" to Icons.Default.TheaterComedy,
    "Science" to Icons.Default.Science
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubCategorySettingScreen(
    navController: NavController,
    categoryId: String,
    categoryViewModel: ClubCategoryViewModel = hiltViewModel()
) {
    // Observe nullable Category
    val categoryState by categoryViewModel.category.collectAsState(initial = null)

    // Local editable states re-initialized whenever categoryState changes
    var name by remember(categoryState) {
        mutableStateOf(TextFieldValue(categoryState?.name ?: ""))
    }
    var description by remember(categoryState) {
        mutableStateOf(TextFieldValue(categoryState?.description.orEmpty()))
    }
    // Selected icon name key
    var selectedIconName by remember(categoryState) {
        mutableStateOf(categoryState?.iconName.orEmpty())
    }

    // Trigger loading
    LaunchedEffect(categoryId) {
        categoryViewModel.getCategory(categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (categoryState?.categoryId == categoryId) "Edit Category" else "New Category"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        categoryViewModel.saveCategory(
                            Category(
                                categoryId = categoryId,
                                name = name.text,
                                description = description.text.ifBlank { null },
                                clubs = categoryState?.clubs.orEmpty(),
                                iconName = selectedIconName.ifBlank { null },
                                iconVector = null
                            )
                        )
                        navController.popBackStack()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )

            Text(text = "Select Icon", style = MaterialTheme.typography.titleMedium)
            // Display currently selected icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = availableIcons
                        .firstOrNull { it.first == selectedIconName }
                        ?.second
                        ?: Icons.Outlined.Category,
                    contentDescription = selectedIconName.ifBlank { "Default" },
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = selectedIconName.ifBlank { "Default Category" })
            }

            // Icon picker row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(availableIcons) { (key, imageVec) ->
                    IconToggleButton(
                        checked = (key == selectedIconName),
                        onCheckedChange = { selectedIconName = key }
                    ) {
                        Icon(
                            imageVector = imageVec,
                            contentDescription = key,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper to map name to ImageVector if needed elsewhere
fun imageVectorForName(name: String?): ImageVector =
    availableIcons.firstOrNull { it.first == name }?.second
        ?: Icons.Outlined.Category
