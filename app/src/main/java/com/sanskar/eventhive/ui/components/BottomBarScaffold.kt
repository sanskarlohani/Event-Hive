package com.sanskar.eventhive.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import com.sanskar.eventhive.ui.navigation.NavigationItem

@Composable
fun BottomBarScaffold(
    navController: NavController,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    userViewModel: UserViewModel = hiltViewModel(),
    content: @Composable (PaddingValues) -> Unit,
) {
    fun normalizeSystemRole(rawRole: String?): String {
        return when (rawRole?.trim()?.lowercase()) {
            "superadmin", "super_admin", "super user", "superuser" -> "superAdmin"
            "studentguide", "student_guide", "student guide" -> "studentGuide"
            else -> "student"
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentUser = userViewModel.observeUser.collectAsStateWithLifecycle().value
    val successUser = (currentUser as? com.sanskar.eventhive.data.Resource.Success)?.data
    val systemRole = normalizeSystemRole(successUser?.systemRole)
    val hasAnyEffectivePermission = successUser?.effectivePermissions?.values?.any { it } == true
    val canAccessAdmin = systemRole != "student" || hasAnyEffectivePermission

    val showBottomBar = currentRoute == NavigationItem.Home.route ||
        currentRoute == NavigationItem.AllClubs.route ||
        currentRoute == NavigationItem.Chats.route ||
        currentRoute == NavigationItem.Profile.route ||
        currentRoute?.startsWith("college_admin") == true ||
        currentRoute?.startsWith("super_admin") == true

    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        bottomBar = {
            if (showBottomBar) {
                EventHiveBottomBar(
                    navController = navController,
                    systemRole = systemRole,
                    canAccessAdmin = canAccessAdmin,
                )
            }
        }
    ) { padding ->
        content(padding)
    }
}
