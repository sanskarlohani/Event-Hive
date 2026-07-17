package com.sanskar.eventhive.ui.navigation

sealed class NavigationItem(val route: String) {
    object Intro : NavigationItem(Screen.INTRO.name)
    object Login : NavigationItem(Screen.LOGIN.name)
    object SignUp : NavigationItem(Screen.SIGNUP.name)
    object Verify : NavigationItem(Screen.VERIFY.name)
    object CollegeCodeEntry : NavigationItem("college_code_entry")
    object Home : NavigationItem(Screen.HOME.name)

    object CreateCategory : NavigationItem(Screen.CREATE_CATEGORY_SCREEN.name)
    object AllCategory : NavigationItem(Screen.ALL_CATEGORY_SCREEN.name)
    object SingleCategory : NavigationItem("${Screen.SINGLE_CATEGORY_SCREEN.name}/{categoryId}") {
        fun createRoute(categoryId: String) = "${Screen.SINGLE_CATEGORY_SCREEN.name}/$categoryId"
        fun createDeepLink(categoryId: String) = "eventhive://app/${createRoute(categoryId)}"
    }
    object CategorySetting : NavigationItem("${Screen.CATEGORY_SETTING_SCREEN.name}/{categoryId}") {
        fun createRoute(categoryId: String) = "${Screen.CATEGORY_SETTING_SCREEN.name}/$categoryId"
        fun createDeepLink(categoryId: String) = "eventhive://app/${createRoute(categoryId)}"
    }

    object CreateClub : NavigationItem("${Screen.CREATE_CLUB_SCREEN.name}/{categoryId}") {
        fun createRoute(categoryId: String) = "${Screen.CREATE_CLUB_SCREEN.name}/$categoryId"
        fun createDeepLink(categoryId: String) = "eventhive://app/${createRoute(categoryId)}"
    }

    object CreateEvent :
        NavigationItem("${Screen.CREATE_EVENT_SCREEN.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.CREATE_EVENT_SCREEN.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "eventhive://app/${createRoute(categoryId, clubId)}"
    }

    object EventDetail : NavigationItem(
        route = "${Screen.EVENT_DETAIL_SCREEN.name}/{categoryId}/{clubId}/{eventId}"
    ) {
        fun createRoute(categoryId: String, clubId: String, eventId: String) =
            "${Screen.EVENT_DETAIL_SCREEN.name}/$categoryId/$clubId/$eventId"
        fun createDeepLink(categoryId: String, clubId: String, eventId: String) = "eventhive://app/${createRoute(categoryId, clubId, eventId)}"
    }

    object EventRegistration :
        NavigationItem("${Screen.EVENT_REGISTRATION_SCREEN.name}/{categoryId}/{clubId}/{eventId}") {
        fun createRoute(categoryId: String, clubId: String, eventId: String) =
            "${Screen.EVENT_REGISTRATION_SCREEN.name}/$categoryId/$clubId/$eventId"
        fun createDeepLink(categoryId: String, clubId: String, eventId: String) = "eventhive://app/${createRoute(categoryId, clubId, eventId)}"
    }

    object EventSetting : NavigationItem("${Screen.EVENT_SETTING_SCREEN.name}/{categoryId}/{clubId}/{eventId}") {
        fun createRoute(categoryId: String, clubId: String, eventId: String) =
            "${Screen.EVENT_SETTING_SCREEN.name}/$categoryId/$clubId/$eventId"
        fun createDeepLink(categoryId: String, clubId: String, eventId: String) = "eventhive://app/${createRoute(categoryId, clubId, eventId)}"
    }

    object AllTeamsForEvent : NavigationItem("${Screen.ALL_TEAMS_FOR_EVENT.name}/{categoryId}/{clubId}/{eventId}") {
        fun createRoute(categoryId: String, clubId: String, eventId: String) =
            "${Screen.ALL_TEAMS_FOR_EVENT.name}/$categoryId/$clubId/$eventId"
        fun createDeepLink(categoryId: String, clubId: String, eventId: String) = "eventhive://app/${createRoute(categoryId, clubId, eventId)}"
    }

    object TeamDetail : NavigationItem("${Screen.TEAM_DETAIL_SCREEN.name}/{ticketId}/{teamId}") {
        fun createRoute(ticketId: String, teamId: String) =
            "${Screen.TEAM_DETAIL_SCREEN.name}/$ticketId/$teamId"
        fun createDeepLink(ticketId: String, teamId: String) = "eventhive://app/${createRoute(ticketId, teamId)}"
    }

    object AllClubs : NavigationItem(Screen.All_CLUBS_SCREEN.name)
    object ClubDetail : NavigationItem("${Screen.CLUB_DETAIL_SCREEN.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.CLUB_DETAIL_SCREEN.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "eventhive://app/${createRoute(categoryId, clubId)}"
    }

    object ClubSetting : NavigationItem("${Screen.CLUB_SETTING_SCREEN.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.CLUB_SETTING_SCREEN.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "eventhive://app/${createRoute(categoryId, clubId)}"
    }

    object EditClub : NavigationItem("${Screen.EDIT_CLUB_SCREEN.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.EDIT_CLUB_SCREEN.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "eventhive://app/${createRoute(categoryId, clubId)}"
    }

    object ManageClubRoles : NavigationItem("${Screen.MANAGE_CLUB_ROLES.name}/{categoryId}/{clubId}") {
        fun createRoute(categoryId: String, clubId: String) =
            "${Screen.MANAGE_CLUB_ROLES.name}/$categoryId/$clubId"
        fun createDeepLink(categoryId: String, clubId: String) = "eventhive://app/${createRoute(categoryId, clubId)}"
    }

    object Chats : NavigationItem("chat_home")
    object ChatRoom : NavigationItem("chat/{roomId}?isAnon={isAnon}") {
        fun createRoute(roomId: String, isAnon: Boolean) = "chat/$roomId?isAnon=$isAnon"
    }
    object ChatAnon : NavigationItem("chat/anon/{collegeId}") {
        fun createRoute(collegeId: String) = "chat/anon/$collegeId"
    }

    object CollegeAdminDashboard : NavigationItem("college_admin/dashboard")
    object CollegeAdminRoles : NavigationItem("college_admin/roles")
    object CollegeAdminAssignRole : NavigationItem("college_admin/assign_role/{uid}") {
        fun createRoute(uid: String) = "college_admin/assign_role/$uid"
    }
    object CollegeAdminMembers : NavigationItem("college_admin/members?collegeId={collegeId}") {
        fun createRoute(collegeId: String? = null): String {
            return if (collegeId.isNullOrBlank()) "college_admin/members"
            else "college_admin/members?collegeId=$collegeId"
        }
    }
    object CollegeAdminCollegeCode : NavigationItem("college_admin/college_code")
    object CollegeAdminReports : NavigationItem("college_admin/reports")

    object SuperAdminColleges : NavigationItem("super_admin/colleges")
    object SuperAdminAddCollege : NavigationItem("super_admin/add_college")
    object SuperAdminCollegeDetail : NavigationItem("super_admin/college_detail/{id}") {
        fun createRoute(id: String) = "super_admin/college_detail/$id"
    }
    object SuperAdminAnalytics : NavigationItem("super_admin/analytics")

    object AdminTab : NavigationItem("admin_tab")
    object Profile : NavigationItem(Screen.PROFILE.name)
    object Notifications : NavigationItem(Screen.NOTIFICATIONS.name)

    //User
    object UserTicket : NavigationItem(Screen.User_TICKET_SCREEN.name)
    object UserEvent : NavigationItem(Screen.User_EVENT_SCREEN.name)
    object UserClub : NavigationItem(Screen.User_CLUB_SCREEN.name)
    object EditProfile : NavigationItem(Screen.EDIT_PROFILE_SCREEN.name)
    object UserTicketDetail :
        NavigationItem("${Screen.USER_TICKET_DETAIL_SCREEN.name}/{userId}/{ticketId}") {
        fun createRoute(userId: String, ticketId: String) =
            "${Screen.USER_TICKET_DETAIL_SCREEN.name}/$userId/$ticketId"
        fun createDeepLink(userId: String, ticketId: String) = "eventhive://app/${createRoute(userId, ticketId)}"
    }


    object PrivacyPolicy : NavigationItem(Screen.PRIVACY_POLICY_SCREEN.name)
    object ReportBug : NavigationItem(Screen.REPORT_BUG_SCREEN.name)

    object Notification : NavigationItem(Screen.NOTIFICATION_SCREEN.name)
}
