package com.sanskar.eventhive.data.repository.Implementation

import android.util.Log
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.RegistrationStatus
import com.sanskar.eventhive.data.model.Team
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.repository.Inteface.TicketRepository
import com.sanskar.eventhive.data.repository.toSafeUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TicketRepository {

    private val USER = "users"
    private val TICKETS = "Tickets"
    private val CATEGORIES = "Categories"
    private val CLUBS = "Clubs"
    private val EVENTS = "Events"
    private val TEAMS = "Teams"


    private val ticketsRoot = firestore.collection("Tickets")
    private fun userCollection() = firestore
        .collection(USER)


    private fun eventCollection(categoryId: String, clubId: String) = firestore
        .collection(CATEGORIES)
        .document(categoryId)
        .collection(CLUBS)
        .document(clubId)
        .collection(EVENTS)


    override suspend fun issueTicket(ticket: Ticket,team: Team): Resource<Unit> = try {

        val batch = firestore.batch()

        val ticketRef = firestore
            .collection(TICKETS)
            .document(ticket.ticketId)


        //save team collection
        val teamRef = firestore
            .collection(TICKETS)
            .document(ticket.ticketId)
            .collection(TEAMS)
            .document(team.teamId)

        batch.set(ticketRef, ticket)
        batch.set(teamRef, team)

        batch.commit().await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun updateTicket(ticket: Ticket,team: Team): Resource<Unit> = try {

        val batch = firestore.batch()
        //update in team collection
        val teamRef = firestore
            .collection(TICKETS)
            .document(ticket.ticketId)
            .collection(TEAMS)
            .document(team.teamId)


        val ticketRef = firestore
            .collection(TICKETS)
            .document(ticket.ticketId)

        batch.set(teamRef, team)
        batch.set(ticketRef, ticket)

        batch.commit().await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun cancelTicket(ticketId: String): Resource<Unit> = try {
        Log.d("TicketRepository", "Marking ticket cancelled: $ticketId")
        firestore
            .collection(TICKETS)
            .document(ticketId)
            .update(
                mapOf(
                    "status" to RegistrationStatus.CANCELLED,
                    "valid" to false,
                    "redeemedAt" to null
                )
            )
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun redeemTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        userId: String
    ): Resource<Unit> {
        return try {
            val ticketRef = firestore.collection(TICKETS).document(ticketId)
            val ticketSnap = ticketRef.get().await()
            val ticket = ticketSnap.toObject(Ticket::class.java)
                ?: return Resource.Error(IllegalStateException("Ticket not found"))

            if (ticket.eventId != eventId || ticket.categoryId != categoryId || ticket.clubId != clubId) {
                return Resource.Error(IllegalArgumentException("Ticket does not belong to this event"))
            }
            if (userId !in ticket.participantIds && ticket.userId != userId) {
                return Resource.Error(IllegalAccessException("User is not part of this ticket"))
            }
            if (!ticket.valid || ticket.status == RegistrationStatus.CANCELLED || ticket.status == RegistrationStatus.CLAIMED) {
                return Resource.Error(IllegalStateException("Ticket is not redeemable"))
            }

            ticketRef
                .update(
                    mapOf(
                        "valid" to false,
                        "redeemedAt" to FieldValue.serverTimestamp(),
                        "status" to RegistrationStatus.CLAIMED
                    )
                )
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override fun getTicket(categoryId: String, clubId: String, eventId: String, ticketId: String): Flow<Ticket?> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .document(ticketId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObject(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }


    override fun getAllTicketsForEvent(categoryId: String, clubId: String, eventId: String): Flow<List<Ticket>> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .whereEqualTo("categoryId", categoryId)
            .whereEqualTo("clubId", clubId)
            .whereEqualTo("eventId", eventId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }

    }

    override fun getAllTickets(): Flow<List<Ticket>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection(TICKETS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }

    override fun getAllTicketsForUser(userId: String): Flow<List<Ticket>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList()).isSuccess
            return@callbackFlow
        }
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }

    override fun getSingleTicketForUser(userId: String, ticketId: String): Flow<Ticket?> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .document(ticketId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let {
                    val ticket = it.toObject(Ticket::class.java)
                    val isAllowed = ticket?.participantIds?.contains(userId) == true || ticket?.userId == userId
                    trySend(if (isAllowed) ticket else null).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override fun getTeamForTicket(
        ticketId: String,
        teamId: String
    ): Flow<Team?> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .document(ticketId)
            .collection(TEAMS)
            .document(teamId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObject(Team::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }

    }

    override fun getAllTeamsForEvent(
        categoryId: String,
        clubId: String,
        eventId: String
    ): Flow<List<Team>> = callbackFlow{
        val registration: ListenerRegistration = firestore
            .collectionGroup(TEAMS)
            .whereEqualTo("categoryId", categoryId)
            .whereEqualTo("clubId", clubId)
            .whereEqualTo("eventId", eventId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Team::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }

    override fun getAllMembersNotRegistered(
        categoryId: String,
        clubId:     String,
        eventId:    String,
        participantIds: List<String>,
        requesterUserId: String
    ): Flow<List<User>> = callbackFlow {
        val queryOld = when {
            participantIds.isEmpty() -> {
                // no filter → get everyone
                firestore.collection(USER)
            }
            participantIds.size <= 10 -> {
                // safe to use whereNotIn
                firestore.collection(USER)
                    .whereNotIn("userId", participantIds)
            }
            else -> {
                // too many IDs for whereNotIn: fetch all, filter in code below
                firestore.collection(USER)
            }
        }
        val requester = firestore.collection(USER).document(requesterUserId).get().await()
        val collegeId = requester.getString("collegeId").orEmpty()
        val query = if (collegeId.isBlank()) {
            queryOld
        } else {
            firestore.collection(USER).whereEqualTo("collegeId", collegeId)
        }

        val registration = query.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            snap?.let {
                val all = it.documents.mapNotNull { doc -> doc.toSafeUser() }
                val filtered = all.filter { user ->
                    user.userId !in participantIds && user.userId != requesterUserId
                }
                trySend(filtered).isSuccess
            }
        }

        awaitClose { registration.remove() }
    }

    override fun getTicketForUserInEvent(eventId: String, userId: String): Flow<Ticket?> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let {
                    val ticket = it.toObjects(Ticket::class.java)
                        .filter { t -> t.eventId == eventId }
                        .sortedByDescending { t -> t.issuedAt }
                        .firstOrNull()
                    trySend(ticket).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

}
