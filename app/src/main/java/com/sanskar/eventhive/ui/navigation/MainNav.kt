package com.sanskar.eventhive.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.presentation.collegeadmin.assignrole.AssignRoleScreen
import com.sanskar.eventhive.presentation.collegeadmin.codemanager.CollegeCodeManagerScreen
import com.sanskar.eventhive.presentation.collegeadmin.dashboard.CollegeAdminDashboardScreen
import com.sanskar.eventhive.presentation.collegeadmin.members.CollegeMemberListScreen
import com.sanskar.eventhive.presentation.collegeadmin.reports.CollegeAdminReportsScreen
import com.sanskar.eventhive.presentation.collegeadmin.roles.RoleManagerScreen
import com.sanskar.eventhive.presentation.collegecode.CollegeCodeEntryScreen
import com.sanskar.eventhive.presentation.common.AccessDeniedScreen
import com.sanskar.eventhive.presentation.superadmin.addcollege.AddCollegeScreen
import com.sanskar.eventhive.presentation.superadmin.analytics.PlatformAnalyticsScreen
import com.sanskar.eventhive.presentation.superadmin.collegedetail.CollegeDetailScreen
import com.sanskar.eventhive.presentation.superadmin.collegelist.CollegeListScreen
import com.sanskar.eventhive.settings.PrivacyPolicyScreen
import com.sanskar.eventhive.settings.ReportBugScreen
import com.sanskar.eventhive.settings.UserTicketDetailedScreen
import com.sanskar.eventhive.ui.screen.AnonymousChatScreen
import com.sanskar.eventhive.ui.screen.ChatHomeScreen
import com.sanskar.eventhive.ui.screen.GroupOrDmChatScreen
import com.sanskar.eventhive.ui.screen.NotificationsScreen
import com.sanskar.eventhive.ui.screen.Club.AllClubScreen
import com.sanskar.eventhive.ui.screen.Club.ClubDetailScreen
import com.sanskar.eventhive.ui.screen.Club.ClubSettingScreen
import com.sanskar.eventhive.ui.screen.Club.CreateClubScreen
import com.sanskar.eventhive.ui.screen.Club.EditClubDetailScreen
import com.sanskar.eventhive.ui.screen.Club.Event.AllTeamsForEventScreen
import com.sanskar.eventhive.ui.screen.Club.Event.CreateEventScreen
import com.sanskar.eventhive.ui.screen.Club.Event.EventDetailScreen
import com.sanskar.eventhive.ui.screen.Club.Event.EventRegistrationScreen
import com.sanskar.eventhive.ui.screen.Club.Event.EventSettingScreen
import com.sanskar.eventhive.ui.screen.Club.Event.TeamDetailedScreen
import com.sanskar.eventhive.ui.screen.Club.ManageClubRolesScreen
import com.sanskar.eventhive.ui.screen.HomeScreen
import com.sanskar.eventhive.ui.screen.ProfileScreen
import com.sanskar.eventhive.ui.screen.User.EditProfileScreen
import com.sanskar.eventhive.ui.screen.User.UserJoinedClubScreen
import com.sanskar.eventhive.ui.screen.User.UserTicketScreen

import com.sanskar.eventhive.ui.screen.auth.LoginScreen
import com.sanskar.eventhive.ui.screen.auth.SignUpScreen
import com.sanskar.eventhive.ui.screen.auth.IntroScreen
import com.sanskar.eventhive.ui.screen.auth.VerifyEmailScreen
import com.sanskar.eventhive.ui.screen.clubcategory.AllCategoryScreen

import com.sanskar.eventhive.ui.screen.clubcategory.ClubCategorySettingScreen
import com.sanskar.eventhive.ui.screen.clubcategory.CreateCategoryScreen
import com.sanskar.eventhive.ui.screen.clubcategory.SingleCategoryScreen
import com.sanskar.eventhive.ui.permissions.canAccessCollegeAdmin
import com.sanskar.eventhive.ui.permissions.canCreateClub
import com.sanskar.eventhive.ui.permissions.canManageCategories
import com.sanskar.eventhive.ui.permissions.normalizeSystemRole
import com.sanskar.eventhive.ui.viewModel.AuthViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val onboardingPrefs = remember(context) {
        context.getSharedPreferences("sit_event_prefs", android.content.Context.MODE_PRIVATE)
    }
    val seenOnboarding = onboardingPrefs.getBoolean("seen_onboarding", false)

    val currentUserRes by userViewModel.observeUser.collectAsStateWithLifecycle()
    val currentUser = (currentUserRes as? Resource.Success)?.data
    val role = when (currentUserRes) {
        is Resource.Loading -> "loading"
        is Resource.Error -> "error"
        is Resource.Success -> normalizeSystemRole(currentUser?.systemRole)
        else -> "student"
    }
    val canAccessCollegeAdminArea = canAccessCollegeAdmin(currentUser)
    val canManageCategoriesArea = canManageCategories(currentUser)
    val canCreateClubArea = canCreateClub(currentUser)

    val userId = when (currentUserRes) {
        is Resource.Success -> (currentUserRes as Resource.Success).data?.userId ?: ""
        else -> ""
    }

    val startDestination = if (authViewModel.signInStatus()) {
        NavigationItem.Home.route
    } else if (!seenOnboarding) {
        NavigationItem.Intro.route
    } else {
        NavigationItem.Login.route
    }

    NavHost(navController, startDestination = startDestination) {
        composable(route = NavigationItem.Intro.route) {
            IntroScreen(
                onGetStarted = {
                    onboardingPrefs.edit().putBoolean("seen_onboarding", true).apply()
                    navController.navigate(NavigationItem.Login.route) {
                        popUpTo(NavigationItem.Intro.route) { inclusive = true }
                    }
                },
            )
        }

        // Auth Screens
        composable(
            route = NavigationItem.Login.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.LOGIN.name}" })
        ) {
            LoginScreen(navController)
        }

        composable(
            route = NavigationItem.SignUp.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.SIGNUP.name}" })
        ) {
            SignUpScreen(navController)
        }

        composable(
            route = NavigationItem.Verify.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.VERIFY.name}" })
        ) {
            VerifyEmailScreen(navController)
        }

        composable(route = NavigationItem.CollegeCodeEntry.route) {
            CollegeCodeEntryScreen(navController = navController)
        }

        // Bottom bar screens
        composable(
            route = NavigationItem.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.HOME.name}" })
        ) {
            HomeScreen(navController)
        }

        composable(
            route = NavigationItem.CreateCategory.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.CREATE_CATEGORY_SCREEN.name}" })
        ) {
            NavGuard(
                allowed = canManageCategoriesArea,
                deniedContent = { AccessDeniedScreen() },
            ) {
                CreateCategoryScreen(navController)
            }
        }

        composable(
            route = NavigationItem.AllCategory.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.ALL_CATEGORY_SCREEN.name}" })
        ) {
            AllCategoryScreen(navController)
        }

        composable(
            route = NavigationItem.CategorySetting.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.CATEGORY_SETTING_SCREEN.name}/{categoryId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            NavGuard(
                allowed = canManageCategoriesArea,
                deniedContent = { AccessDeniedScreen() },
            ) {
                ClubCategorySettingScreen(navController = navController, categoryId = categoryId)
            }
        }

        composable(
            route = NavigationItem.SingleCategory.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.SINGLE_CATEGORY_SCREEN.name}/{categoryId}"
            })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            SingleCategoryScreen(categoryId = categoryId, navController = navController)
        }

        composable(
            route = NavigationItem.CreateClub.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.CREATE_CLUB_SCREEN.name}/{categoryId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            NavGuard(
                allowed = canCreateClubArea,
                deniedContent = { AccessDeniedScreen() },
            ) {
                CreateClubScreen(categoryId = categoryId, userId = userId, navController = navController)
            }
        }

        composable(
            route = NavigationItem.AllClubs.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.All_CLUBS_SCREEN.name}" })
        ) {
            AllClubScreen(navController)
        }

        composable(
            route = NavigationItem.ClubDetail.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.CLUB_DETAIL_SCREEN.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            ClubDetailScreen(navController = navController, categoryId = categoryId, clubId = clubId, userId = userId)
        }

        composable(
            route = NavigationItem.ClubSetting.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.CLUB_SETTING_SCREEN.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            ClubSettingScreen(navController = navController, categoryId = categoryId, clubId = clubId, userId = userId)
        }

        composable(
            route = NavigationItem.EditClub.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.EDIT_CLUB_SCREEN.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            EditClubDetailScreen(navController = navController, categoryId = categoryId, clubId = clubId)
        }

        composable(
            route = NavigationItem.ManageClubRoles.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.MANAGE_CLUB_ROLES.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            ManageClubRolesScreen(categoryId = categoryId, clubId = clubId, currentUserId = userId, navController)
        }

        composable(
            route = NavigationItem.CreateEvent.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.CREATE_EVENT_SCREEN.name}/{categoryId}/{clubId}"
            })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            val clubId = it.arguments?.getString("clubId") ?: ""
            CreateEventScreen(navController = navController, categoryId = categoryId, clubId = clubId, userId = userId)
        }

        composable(
            route = NavigationItem.EventDetail.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.EVENT_DETAIL_SCREEN.name}/{categoryId}/{clubId}/{eventId}"
            })
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val categoryId = args.getString("categoryId") ?: ""
            val clubId = args.getString("clubId") ?: ""
            val eventId = args.getString("eventId") ?: ""
            EventDetailScreen(navController = navController, categoryId = categoryId, clubId = clubId, eventId = eventId)
        }

        composable(
            route = NavigationItem.EventRegistration.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/{categoryId}/{clubId}/{eventId}"
            })
        ) {
            val args = it.arguments!!
            val categoryId = args.getString("categoryId") ?: ""
            val clubId = args.getString("clubId") ?: ""
            val eventId = args.getString("eventId") ?: ""
            EventRegistrationScreen(navController = navController, categoryId = categoryId, clubId = clubId, eventId = eventId, userId = userId)
        }

        composable(
            route = NavigationItem.EventSetting.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.EVENT_SETTING_SCREEN.name}/{categoryId}/{clubId}/{eventId}"
            })
        ) {
            val args = it.arguments!!
            val categoryId = args.getString("categoryId") ?: ""
            val clubId = args.getString("clubId") ?: ""
            val eventId = args.getString("eventId") ?: ""
            EventSettingScreen(navController = navController, categoryId = categoryId, clubId = clubId, eventId = eventId)
        }

        composable(
            route = NavigationItem.AllTeamsForEvent.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("clubId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.ALL_TEAMS_FOR_EVENT.name}/{categoryId}/{clubId}/{eventId}"
            })
        ) {
            val args = it.arguments!!
            val categoryId = args.getString("categoryId") ?: ""
            val clubId = args.getString("clubId") ?: ""
            val eventId = args.getString("eventId") ?: ""
            AllTeamsForEventScreen(navController = navController, categoryId = categoryId, clubId = clubId, eventId = eventId)
        }

        composable(
            route = NavigationItem.TeamDetail.route,
            arguments = listOf(
                navArgument("ticketId") { type = NavType.StringType },
                navArgument("teamId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.TEAM_DETAIL_SCREEN.name}/{ticketId}/{teamId}"
            })
        ) {
            val ticketId = it.arguments?.getString("ticketId") ?: ""
            val teamId = it.arguments?.getString("teamId") ?: ""
            TeamDetailedScreen(ticketId = ticketId, teamId = teamId, navController = navController)
        }

        composable(
            route = NavigationItem.Chats.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/chat_home" })
        ) {
            ChatHomeScreen(navController = navController)
        }

        composable(
            route = NavigationItem.ChatRoom.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("isAnon") { type = NavType.BoolType; defaultValue = false },
            ),
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/chat/{roomId}" }),
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId").orEmpty()
            val isAnon = backStackEntry.arguments?.getBoolean("isAnon") ?: false
            if (isAnon) {
                val collegeId = roomId.removePrefix("anon_")
                AnonymousChatScreen(navController = navController, collegeId = collegeId)
            } else {
                GroupOrDmChatScreen(navController = navController, roomId = roomId)
            }
        }

        composable(
            route = NavigationItem.ChatAnon.route,
            arguments = listOf(navArgument("collegeId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val collegeId = backStackEntry.arguments?.getString("collegeId").orEmpty()
            AnonymousChatScreen(navController = navController, collegeId = collegeId)
        }

        composable(
            route = NavigationItem.Profile.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.PROFILE.name}" })
        ) {
            ProfileScreen(navController)
        }

        composable(
            route = NavigationItem.Notifications.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.NOTIFICATIONS.name}" })
        ) {
            NotificationsScreen(navController)
        }

        // User Screens
        composable(
            route = NavigationItem.UserTicket.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.User_TICKET_SCREEN.name}" })
        ) {
            UserTicketScreen(navController, userId)
        }

        composable(
            route = NavigationItem.UserEvent.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.User_EVENT_SCREEN.name}" })
        ) {
            ProfileScreen(navController)
        }

        composable(
            route = NavigationItem.UserClub.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.User_CLUB_SCREEN.name}" })
        ) {
            UserJoinedClubScreen(navController)
        }

        composable(
            route = NavigationItem.EditProfile.route,
            deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/${Screen.EDIT_PROFILE_SCREEN.name}" })
        ) {
            EditProfileScreen(navController)
        }

        composable(
            route = NavigationItem.UserTicketDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("ticketId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "eventhive://app/${Screen.USER_TICKET_DETAIL_SCREEN.name}/{userId}/{ticketId}"
            })
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            val ticketId = it.arguments?.getString("ticketId") ?: ""
            UserTicketDetailedScreen(userId = userId, ticketId = ticketId, navController = navController)
        }

        navigation(
            startDestination = NavigationItem.CollegeAdminDashboard.route,
            route = "college_admin",
        ) {
            composable(
                route = NavigationItem.CollegeAdminDashboard.route,
                deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/college_admin/dashboard" }),
            ) {
                NavGuard(
                    allowed = canAccessCollegeAdminArea,
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    CollegeAdminDashboardScreen(navController = navController)
                }
            }
            composable(route = NavigationItem.CollegeAdminRoles.route) {
                NavGuard(
                    allowed = canAccessCollegeAdminArea,
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    RoleManagerScreen()
                }
            }
            composable(
                route = NavigationItem.CollegeAdminAssignRole.route,
                arguments = listOf(navArgument("uid") { type = NavType.StringType }),
            ) {
                val uid = it.arguments?.getString("uid").orEmpty()
                NavGuard(
                    allowed = canAccessCollegeAdminArea,
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    AssignRoleScreen(navController = navController, uid = uid)
                }
            }
            composable(
                route = NavigationItem.CollegeAdminMembers.route,
                arguments = listOf(
                    navArgument("collegeId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                val collegeId = it.arguments?.getString("collegeId")
                NavGuard(
                    allowed = canAccessCollegeAdminArea,
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    CollegeMemberListScreen(navController = navController, collegeId = collegeId)
                }
            }
            composable(route = NavigationItem.CollegeAdminCollegeCode.route) {
                NavGuard(
                    allowed = canAccessCollegeAdminArea,
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    CollegeCodeManagerScreen()
                }
            }
            composable(route = NavigationItem.CollegeAdminReports.route) {
                NavGuard(
                    allowed = canAccessCollegeAdminArea,
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    CollegeAdminReportsScreen()
                }
            }
        }

        navigation(
            startDestination = NavigationItem.SuperAdminColleges.route,
            route = "super_admin",
        ) {
            composable(
                route = NavigationItem.SuperAdminColleges.route,
                deepLinks = listOf(navDeepLink { uriPattern = "eventhive://app/super_admin/colleges" }),
            ) {
                NavGuard(
                    allowed = role == "superAdmin",
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    CollegeListScreen(navController = navController)
                }
            }
            composable(route = NavigationItem.SuperAdminAddCollege.route) {
                NavGuard(
                    allowed = role == "superAdmin",
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    AddCollegeScreen(navController = navController)
                }
            }
            composable(
                route = NavigationItem.SuperAdminCollegeDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) {
                val id = it.arguments?.getString("id").orEmpty()
                NavGuard(
                    allowed = role == "superAdmin",
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    CollegeDetailScreen(navController = navController, collegeId = id)
                }
            }
            composable(route = NavigationItem.SuperAdminAnalytics.route) {
                NavGuard(
                    allowed = role == "superAdmin",
                    deniedContent = { AccessDeniedScreen() },
                ) {
                    PlatformAnalyticsScreen()
                }
            }
        }


        composable(NavigationItem.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController)
        }

        composable(NavigationItem.ReportBug.route) {
            ReportBugScreen(navController)
        }
        composable(NavigationItem.Notification.route) {
            // Placeholder for settings screen
            NotificationsScreen(
                navController
            )
        }

    }
}

@Composable
private fun NavGuard(
    allowed: Boolean,
    deniedContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    if (allowed) content() else deniedContent()
}

