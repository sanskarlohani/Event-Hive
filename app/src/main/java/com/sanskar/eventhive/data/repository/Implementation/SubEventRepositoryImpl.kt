package com.sanskar.eventhive.data.repository.Implementation

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.SubEvent
import com.sanskar.eventhive.data.repository.Inteface.SubEventRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SubEventRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : SubEventRepository {

    private val CATEGORIES = "Categories"
    private val CLUBS = "Clubs"
    private val EVENTS = "Events"
    private val SUB_EVENTS = "SubEvents"
    private val MEMBERS = "Members"
    private val ORGANIZERS = "Organizers"
    private val SPONSORS = "Sponsors"
    private val TICKETS = "Tickets"

    private fun subEventCollection(
        categoryId: String,
        clubId: String,
        eventId: String
    ) = firebaseFirestore.collection(CATEGORIES)
        .document(categoryId)
        .collection(CLUBS)
        .document(clubId)
        .collection(EVENTS)
        .document(eventId)
        .collection(SUB_EVENTS)

    override suspend fun fetchSubEvents(
        categoryId: String,
        clubId: String,
        eventId: String,
    ): Resource<List<SubEvent>> = try {
        val snapshot = subEventCollection(categoryId, clubId, eventId)
            .get()
            .await()
        val list = snapshot.documents.mapNotNull { it.toObject(SubEvent::class.java)?.copy(subEventId = it.id) }
        Resource.Success(list)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun fetchSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
    ): Resource<SubEvent?> = try {
        val doc = subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .get()
            .await()
        val subEvent = doc.toObject(SubEvent::class.java)?.copy(subEventId = doc.id)
        Resource.Success(subEvent)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun createSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEvent: SubEvent,
    ): Resource<Unit> = try {
        subEventCollection(categoryId, clubId, eventId)
            .document(subEvent.subEventId)
            .set(subEvent)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun updateSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        subEvent: SubEvent,
    ): Resource<Unit> = try {
        subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .set(subEvent)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun deleteSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
    ): Resource<Unit> = try {
        subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .delete()
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun registerForSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        userId: String,
    ): Resource<Unit> = try {
        subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(MEMBERS)
            .document(userId)
            .set(mapOf("registeredAt" to System.currentTimeMillis()))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun unregisterFromSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        userId: String,
    ): Resource<Unit> = try {
        subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(MEMBERS)
            .document(userId)
            .delete()
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getSubEventRegistrations(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
    ): Resource<List<String>> = try {
        val snapshot = subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(MEMBERS)
            .get()
            .await()
        val ids = snapshot.documents.map { it.id }
        Resource.Success(ids)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getSubEventOrganizers(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
    ): Resource<List<String>> = try {
        val snapshot = subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(ORGANIZERS)
            .get()
            .await()
        val ids = snapshot.documents.map { it.id }
        Resource.Success(ids)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getSubEventSponsors(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
    ): Resource<List<String>> = try {
        val snapshot = subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(SPONSORS)
            .get()
            .await()
        val urls = snapshot.documents.mapNotNull { it.getString("logoUrl") }
        Resource.Success(urls)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getSubEventTickets(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
    ): Resource<List<String>> = try {
        val snapshot = subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(TICKETS)
            .get()
            .await()
        val ids = snapshot.documents.map { it.id }
        Resource.Success(ids)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getSubEventTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        ticketId: String,
    ): Resource<String?> = try {
        val doc = subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(TICKETS)
            .document(ticketId)
            .get()
            .await()
        Resource.Success(doc.getString("userId"))
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun issueSubEventTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        ticket: String,
    ): Resource<Unit> = try {
        subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(TICKETS)
            .document(ticket)
            .set(mapOf("issuedAt" to System.currentTimeMillis()))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun redeemSubEventTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        ticketId: String,
    ): Resource<Unit> = try {
        subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(TICKETS)
            .document(ticketId)
            .update("redeemedAt", System.currentTimeMillis())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getMySubEventTickets(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        userId: String,
    ): Resource<String?> = try {
        val doc = subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(TICKETS)
            .document(userId)
            .get()
            .await()
        Resource.Success(doc.getString("ticketId"))
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getMySubEventTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        ticketId: String,
        userId: String,
    ): Resource<String?> = try {
        val doc = subEventCollection(categoryId, clubId, eventId)
            .document(subEventId)
            .collection(TICKETS)
            .document(ticketId)
            .get()
            .await()
        Resource.Success(doc.getString("userId"))
    } catch (e: Exception) {
        Resource.Error(e)
    }
}
