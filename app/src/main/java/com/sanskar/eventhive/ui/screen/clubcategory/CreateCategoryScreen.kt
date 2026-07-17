package com.sanskar.eventhive.ui.screen.clubcategory

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Category
import com.sanskar.eventhive.ui.viewModel.ClubCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryScreen(
    navController: NavController,
    viewModel: ClubCategoryViewModel = hiltViewModel()
) {
    var categoryName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<ImageVector?>(null) }
    val defaultColor = MaterialTheme.colorScheme.primary
    var selectedColor by remember { mutableStateOf(defaultColor) }

    val isCreateEnabled = categoryName.isNotBlank()

    // Observe creation status
    val context = LocalContext.current
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle(null)

    // React to status changes
    LaunchedEffect(operationStatus) {
        when (operationStatus) {
            is Resource.Success -> {
                Toast.makeText(context, "Category Created!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is Resource.Error -> {
                val message = (operationStatus as Resource.Error).exception.message ?: "Error"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Category", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category Name
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name *") },
                placeholder = { Text("Enter category name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor             = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor   = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor             = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor   = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Icon picker
            Text(text = "Choose Icon", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            val icons = listOf(
                Icons.Default.Person, Icons.Default.FlashOn, Icons.Default.Lightbulb,
                Icons.Default.Laptop, Icons.Default.Handshake, Icons.Default.Language,
                Icons.Default.Palette, Icons.Default.MusicNote, Icons.AutoMirrored.Filled.MenuBook,
                Icons.Default.EmojiEvents, Icons.Default.TheaterComedy, Icons.Default.Science
            )
            LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.height(100.dp)) {
                items(icons) { icon ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedIcon == icon)
                                    selectedColor.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedIcon = icon }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selectedIcon == icon)
                                selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Accent Color picker
            Text(text = "Accent Color", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            val colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onPrimary
            )
            Row {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Show loader when saving
            if (operationStatus is Resource.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        // Build Category with default club list and auto-id
                        val rawName = selectedIcon?.name         // e.g. "Filled.EmojiEvents"
                        val iconName = rawName
                            ?.substringAfterLast('.')              // -> "EmojiEvents"
                            ?: ""
                        val category = Category(
                            name    = categoryName,
                            description = description.takeIf { it.isNotBlank() },
                            iconName = iconName
                        )
                        viewModel.saveCategory(category)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isCreateEnabled && operationStatus !is Resource.Loading
                ) {
                    Text("Create")
                }
            }
        }
    }
}
