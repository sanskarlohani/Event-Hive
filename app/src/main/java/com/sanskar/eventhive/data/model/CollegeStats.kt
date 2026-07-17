package com.sanskar.eventhive.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class CollegeStats(
    val totalStudents: Long = 0,
    val totalEvents: Long = 0,
    val totalClubs: Long = 0,
)

@Immutable
data class PlatformStats(
    val totalUsers: Long = 0,
    val totalEvents: Long = 0,
    val totalTickets: Long = 0,
)

@Immutable
data class CollegeAnalyticsRow(
    val id: String = "",
    val collegeName: String = "",
    val users: Long = 0,
    val events: Long = 0,
    val bookings: Long = 0,
)
