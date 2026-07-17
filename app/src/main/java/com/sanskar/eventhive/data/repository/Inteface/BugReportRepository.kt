package com.sanskar.eventhive.data.repository.Inteface

import android.net.Uri
import com.sanskar.eventhive.data.Resource

interface BugReportRepository {
    suspend fun submitBug(
        userId: String,
        title: String,
        description: String,
        appVersion: String?,
        deviceInfo: String?,
        screenshotUri: Uri?
    ): Resource<Unit>
}
