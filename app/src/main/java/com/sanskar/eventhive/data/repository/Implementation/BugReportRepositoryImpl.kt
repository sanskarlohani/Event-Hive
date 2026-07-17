package com.sanskar.eventhive.data.repository.Implementation

import android.net.Uri
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.BugReport
import com.sanskar.eventhive.data.repository.Inteface.BugReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BugReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
): BugReportRepository {

    override suspend fun submitBug(
        userId: String,
        title: String,
        description: String,
        appVersion: String?,
        deviceInfo: String?,
        screenshotUri: Uri?
    ): Resource<Unit> = try {
        val reports = firestore.collection("BugReports")
        val docRef = reports.document()
        val reportId = docRef.id

        var screenshotUrl: String? = null
        if (screenshotUri != null) {
            val filename = "screenshot_${System.currentTimeMillis()}"
            val ref = storage.reference.child("bug_reports/$userId/$filename")
            ref.putFile(screenshotUri).await()
            screenshotUrl = ref.downloadUrl.await().toString()
        }

        val report = BugReport(
            id = reportId,
            userId = userId,
            title = title,
            description = description,
            screenshotUrl = screenshotUrl,
            appVersion = appVersion,
            deviceInfo = deviceInfo
        )
        docRef.set(report).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }
}
