package com.sanskar.eventhive.data.repository

import com.sanskar.eventhive.data.model.College
import com.sanskar.eventhive.data.model.CollegeAnalyticsRow
import com.sanskar.eventhive.data.model.CollegeStats
import com.sanskar.eventhive.data.model.Event
import com.sanskar.eventhive.data.model.PlatformStats
import com.sanskar.eventhive.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollegeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val colleges = firestore.collection("colleges")
    private val users = firestore.collection("users")

    suspend fun getCollegeByDomain(domain: String): College? {
        val snap = colleges
            .whereEqualTo("emailDomain", domain.lowercase())
            .limit(1)
            .get()
            .await()
        val doc = snap.documents.firstOrNull() ?: return null
        return doc.toObject(College::class.java)?.copy(id = doc.id)
    }

    suspend fun getCollegeByCode(code: String): College? {
        val normalized = code.uppercase()
        val snap = colleges
            .whereEqualTo("collegeCode", normalized)
            .whereEqualTo("isActive", true)
            .limit(1)
            .get()
            .await()
        val doc = snap.documents.firstOrNull() ?: return null
        return doc.toObject(College::class.java)?.copy(id = doc.id)
    }

    suspend fun isCodeAvailable(code: String): Boolean {
        val snap = colleges
            .whereEqualTo("collegeCode", code.uppercase())
            .limit(1)
            .get()
            .await()
        return snap.isEmpty
    }

    suspend fun createCollege(college: College): Result<String> = runCatching {
        val id = if (college.id.isBlank()) colleges.document().id else college.id
        val now = Timestamp.now()
        val payload = college.copy(
            id = id,
            emailDomain = college.emailDomain?.lowercase(),
            collegeCode = college.collegeCode.uppercase(),
            createdAt = now,
            updatedAt = now,
        )
        colleges.document(id).set(payload).await()
        id
    }

    suspend fun updateCollegeCode(collegeId: String, newCode: String): Result<Unit> = runCatching {
        val normalized = newCode.uppercase()
        val existing = colleges
            .whereEqualTo("collegeCode", normalized)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        if (existing != null && existing.id != collegeId) {
            throw IllegalArgumentException("Code already taken")
        }

        val batch = firestore.batch()
        val ref = colleges.document(collegeId)
        batch.update(ref, mapOf("collegeCode" to normalized, "updatedAt" to Timestamp.now()))
        batch.commit().await()
    }

    suspend fun assignUserCollege(uid: String, collegeId: String): Result<Unit> = runCatching {
        users.document(uid).update("collegeId", collegeId).await()
    }

    suspend fun setCollegeActive(collegeId: String, isActive: Boolean): Result<Unit> = runCatching {
        colleges.document(collegeId).update(
            mapOf(
                "isActive" to isActive,
                "updatedAt" to Timestamp.now(),
            )
        ).await()
    }

    fun getAllColleges(): Flow<List<College>> = callbackFlow {
        val registration = colleges
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(College::class.java)?.copy(id = doc.id)
                }
                trySend(items).isSuccess
            }
        awaitClose { registration.remove() }
    }

    fun getCollegeById(id: String): Flow<College> = callbackFlow {
        val registration = colleges.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val value = snapshot?.toObject(College::class.java)?.copy(id = snapshot.id)
            if (value != null) {
                trySend(value).isSuccess
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun getCollegesPage(
        pageSize: Long = 20,
        lastDocument: DocumentSnapshot? = null,
    ): Pair<List<College>, DocumentSnapshot?> {
        var query = colleges
            .orderBy("createdAt")
            .limit(pageSize)
        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }
        val snapshot = query.get().await()
        val data = snapshot.documents.mapNotNull { it.toObject(College::class.java)?.copy(id = it.id) }
        return data to snapshot.documents.lastOrNull()
    }

    suspend fun getCollegeStats(collegeId: String): CollegeStats {
        val usersCount = users.whereEqualTo("collegeId", collegeId).count().get(AggregateSource.SERVER).await().count
        val eventsCount = firestore.collection("events").whereEqualTo("collegeId", collegeId).count().get(AggregateSource.SERVER).await().count
        val clubsCount = firestore.collection("clubs").whereEqualTo("collegeId", collegeId).count().get(AggregateSource.SERVER).await().count
        return CollegeStats(
            totalStudents = usersCount,
            totalEvents = eventsCount,
            totalClubs = clubsCount,
        )
    }

    suspend fun getPlatformStats(cutoffMillis: Long?): PlatformStats {
        val usersQuery = if (cutoffMillis == null) users else users.whereGreaterThanOrEqualTo("createdAt", cutoffMillis)
        val eventsQuery = if (cutoffMillis == null) firestore.collection("events") else firestore.collection("events").whereGreaterThanOrEqualTo("createdAt", cutoffMillis)
        val ticketsQuery = if (cutoffMillis == null) firestore.collection("tickets") else firestore.collection("tickets").whereGreaterThanOrEqualTo("createdAt", cutoffMillis)
        val usersCount = usersQuery.count().get(AggregateSource.SERVER).await().count
        val eventsCount = eventsQuery.count().get(AggregateSource.SERVER).await().count
        val ticketCount = ticketsQuery.count().get(AggregateSource.SERVER).await().count
        return PlatformStats(
            totalUsers = usersCount,
            totalEvents = eventsCount,
            totalTickets = ticketCount,
        )
    }

    suspend fun getPerCollegeAnalytics(cutoffMillis: Long?): List<CollegeAnalyticsRow> {
        val allColleges = colleges.get().await().documents.mapNotNull { it.toObject(College::class.java)?.copy(id = it.id) }
        return allColleges.map { college ->
            val usersQuery = users.whereEqualTo("collegeId", college.id)
            val eventsQuery = firestore.collection("events").whereEqualTo("collegeId", college.id)
            val ticketsQuery = firestore.collection("tickets").whereEqualTo("collegeId", college.id)
            val usersCount = if (cutoffMillis == null) usersQuery.count().get(AggregateSource.SERVER).await().count
            else usersQuery.whereGreaterThanOrEqualTo("createdAt", cutoffMillis).count().get(AggregateSource.SERVER).await().count
            val eventsCount = if (cutoffMillis == null) eventsQuery.count().get(AggregateSource.SERVER).await().count
            else eventsQuery.whereGreaterThanOrEqualTo("createdAt", cutoffMillis).count().get(AggregateSource.SERVER).await().count
            val bookingsCount = if (cutoffMillis == null) ticketsQuery.count().get(AggregateSource.SERVER).await().count
            else ticketsQuery.whereGreaterThanOrEqualTo("createdAt", cutoffMillis).count().get(AggregateSource.SERVER).await().count
            CollegeAnalyticsRow(
                id = college.id,
                collegeName = college.name,
                users = usersCount,
                events = eventsCount,
                bookings = bookingsCount,
            )
        }
    }

    suspend fun getCollegeMembersPage(
        collegeId: String,
        pageSize: Long = 20,
        lastDocument: DocumentSnapshot? = null,
    ): Pair<List<User>, DocumentSnapshot?> {
        var query = users.whereEqualTo("collegeId", collegeId).orderBy("name").limit(pageSize)
        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }
        val snap = query.get().await()
        return snap.documents.mapNotNull { it.toSafeUser() } to snap.documents.lastOrNull()
    }

    suspend fun getTopClubsForCollege(collegeId: String, topN: Long = 5): List<Pair<String, Long>> {
        val snap = firestore.collection("clubs")
            .whereEqualTo("collegeId", collegeId)
            .get()
            .await()
        return snap.documents.map { doc ->
            val name = doc.getString("name").orEmpty()
            val count = (doc.get("memberIds") as? List<*>)?.size?.toLong() ?: 0L
            name to count
        }.sortedByDescending { it.second }.take(topN.toInt())
    }

    suspend fun getUserCollegeId(uid: String): String {
        val snap = users.document(uid).get().await()
        return snap.getString("collegeId").orEmpty()
    }

    fun getRecentEventsForCollege(collegeId: String): Flow<List<Event>> = callbackFlow {
        val registration = firestore.collection("events")
            .whereEqualTo("collegeId", collegeId)
            .orderBy("createdAt")
            .limitToLast(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.toObjects(Event::class.java).orEmpty().sortedByDescending { it.createdAt }
                trySend(events).isSuccess
            }
        awaitClose { registration.remove() }
    }

    suspend fun getMonthlyAndTotalEventCount(collegeId: String): Pair<Long, Long> {
        val startOfMonth = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val total = firestore.collection("events")
            .whereEqualTo("collegeId", collegeId)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count
        val month = firestore.collection("events")
            .whereEqualTo("collegeId", collegeId)
            .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count
        return month to total
    }

    suspend fun getMonthlyAndTotalBookingCount(collegeId: String): Pair<Long, Long> {
        val startOfMonth = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val total = firestore.collection("tickets")
            .whereEqualTo("collegeId", collegeId)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count
        val month = firestore.collection("tickets")
            .whereEqualTo("collegeId", collegeId)
            .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count
        return month to total
    }
}
