package com.sanskar.eventhive.data.model

data class BugReport(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val screenshotUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val appVersion: String? = null,
    val deviceInfo: String? = null,
    val status: String = "open"
)

