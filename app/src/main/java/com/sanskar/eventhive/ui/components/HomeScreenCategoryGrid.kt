package com.sanskar.eventhive.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sanskar.eventhive.data.model.AppRole
import com.sanskar.eventhive.data.model.Category
import com.sanskar.eventhive.ui.navigation.NavigationItem


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenCategoryGrid(
    list: List<Category>,
    appRole: AppRole,
    navController: NavController,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),                         // 2 rows
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp),                       // adjust height for 2 rows
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp) // start/end padding
    ) {
        items(list) { cat ->
            HomeScreenCategoryCard(
                cat,
                modifier = Modifier.size(width = 180.dp, height = 160.dp)
            ) {
                navController.navigate(
                    NavigationItem.SingleCategory.createRoute(cat.categoryId)
                )
            }
        }

        if (appRole == AppRole.ADMIN) {
            item {
                CreateCategoryCard(modifier = Modifier.size(width = 180.dp, height = 160.dp)) {
                    navController.navigate(NavigationItem.CreateCategory.route)
                }
            }
        }
    }
}