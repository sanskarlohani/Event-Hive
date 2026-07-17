package com.sanskar.eventhive.ui.screen.clubcategory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.permissions.canManageCategories
import com.sanskar.eventhive.ui.components.CategoryCard
import com.sanskar.eventhive.ui.components.CreateCategoryCard
import com.sanskar.eventhive.ui.viewModel.ClubCategoryViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCategoryScreen(
    navController: NavController,
    viewModel: ClubCategoryViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currentUserRes by userViewModel.observeUser.collectAsState()
    val currentUser = (currentUserRes as? Resource.Success)?.data
    val canManageCategories = canManageCategories(currentUser)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Categories", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (canManageCategories) {
                FloatingActionButton(onClick = { navController.navigate(NavigationItem.CreateCategory.route) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }

        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.White
                        )
                    )
                )
                .padding(padding)
        ) {

            if (categories.isEmpty()) {
                Text(
                    text = "No categories found",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            modifier = Modifier,
                            onClick = {
                                navController.navigate(
                                    NavigationItem.SingleCategory.createRoute(
                                        category.categoryId
                                    )
                                )
                            }
                        )

                    }
                    if (canManageCategories) {
                        item {
                            CreateCategoryCard(modifier = Modifier) {
                                navController.navigate(NavigationItem.CreateCategory.route)
                            }
                        }
                    }
                }

            }
        }
    }
}

//@Composable
//fun CategoryCard(
//    category: Category,
//    onClick: () -> Unit
//) {
//    Card(
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(100.dp)
//                .background(
//                    Brush.horizontalGradient(
//                        colors = listOf(
//                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
//                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
//                        )
//                    )
//                )
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp)
//            ) {
//                category.iconUrl?.let { url ->
//                    Image(
//                        painter = rememberAsyncImagePainter(url),
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .size(56.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                }
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = category.name,
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.onSecondaryContainer,
//                        fontSize = 18.sp
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    category.description?.let {
//                        Text(
//                            text = it,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSecondaryContainer
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
