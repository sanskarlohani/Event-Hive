package com.sanskar.eventhive.ui.screen.clubcategory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.permissions.canManageCategories
import com.sanskar.eventhive.ui.permissions.canCreateClub
import com.sanskar.eventhive.ui.components.ClubCard
import com.sanskar.eventhive.ui.components.SmallClubCard
import com.sanskar.eventhive.ui.viewModel.ClubCategoryViewModel
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleCategoryScreen(
    categoryId: String,
    navController: NavController,
    clubCategoryViewModel: ClubCategoryViewModel = hiltViewModel(),
    clubViewModel: ClubViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val category by clubCategoryViewModel.category.collectAsStateWithLifecycle()
    val clubsByCategory by clubViewModel.clubByCategory.collectAsState()

    LaunchedEffect(categoryId) {
        clubCategoryViewModel.getCategory(categoryId)
        clubViewModel.getClubByCategory(categoryId)
    }

    val currentUserRes by userViewModel.observeUser.collectAsState()
    val currentUser = (currentUserRes as? Resource.Success)?.data
    val canManageClubs = canCreateClub(currentUser)
    val canManageCategory = canManageCategories(currentUser)

    var showSmallCards by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = category?.name ?: "no category"
                    Text(title, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = { showSmallCards = !showSmallCards }) {
                            Icon(
                                imageVector = if (showSmallCards) Icons.Default.GridOn else Icons.AutoMirrored.Filled.List,
                                contentDescription = "Toggle View"
                            )
                        }
                        if (canManageCategory) {
                            IconButton(onClick = {
                                navController.navigate(NavigationItem.CategorySetting.createRoute(categoryId))
                            }) {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        }
                    }

                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (canManageClubs) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(
                            NavigationItem.CreateClub.createRoute(
                                categoryId
                            )
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }

        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(paddingValues)
        ) {


            if (clubsByCategory.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No clubs here.")
                    Spacer(Modifier.height(16.dp))
                    if (canManageClubs) {
                        Button(onClick = {
                            navController.navigate(
                                NavigationItem.CreateClub.createRoute(
                                    categoryId
                                )
                            )
                        }) {
                            Text("Create Club")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clubsByCategory) { club ->
                        if (showSmallCards) {
                            SmallClubCard(
                                club = club,
                                isMember = canManageClubs, // or your actual membership logic
                                onClick = {
                                    navController.navigate(
                                        NavigationItem.ClubDetail.createRoute(
                                            categoryId,
                                            club.clubId
                                        )
                                    )
                                }
                            )
                        } else {
                            ClubCard(
                                club = club,
                                onClick = {
                                    navController.navigate(
                                        NavigationItem.ClubDetail.createRoute(
                                            categoryId,
                                            club.clubId
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}

