package com.sanskar.eventhive.data.repository.Implementation

import android.util.Log
import androidx.core.net.toUri
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Event
import com.sanskar.eventhive.data.repository.Inteface.EventRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : EventRepository {

    private val EVENTS = "Events"
    private val CLUBS = "Clubs"
    private val categoryCol = firebaseFirestore.collection("Categories")

    private fun eventsCollection(categoryId: String, clubId: String) =
        categoryCol
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)
            .collection(EVENTS)


    override suspend fun createEvent(event: Event): Resource<Unit> = try {
        val payload = uploadEventMediaIfNeeded(event)
        eventsCollection(event.categoryId, event.clubId)
            .document(event.eventId)
            .set(payload)
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    // ← now takes all three IDs and reads directly by path

    override suspend fun updateEvent(event: Event): Resource<Unit> = try {
        val payload = uploadEventMediaIfNeeded(event)
        eventsCollection(event.categoryId, event.clubId)
            .document(event.eventId)
            .set(payload)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    private suspend fun uploadEventMediaIfNeeded(event: Event): Event {
        var poster = event.posterUrl
        var banner = event.bannerUrl

        if (!poster.isNullOrBlank() && (poster.startsWith("content://") || poster.startsWith("file://"))) {
            val posterRef = firebaseStorage.reference.child(
                "event_posters/${event.categoryId}/${event.clubId}/${event.eventId}_${System.currentTimeMillis()}"
            )
            posterRef.putFile(poster.toUri()).await()
            poster = posterRef.downloadUrl.await().toString()
        }

        if (!banner.isNullOrBlank() && (banner.startsWith("content://") || banner.startsWith("file://"))) {
            val bannerRef = firebaseStorage.reference.child(
                "event_banners/${event.categoryId}/${event.clubId}/${event.eventId}_${System.currentTimeMillis()}"
            )
            bannerRef.putFile(banner.toUri()).await()
            banner = bannerRef.downloadUrl.await().toString()
        }

        return event.copy(
            posterUrl = poster,
            bannerUrl = banner,
        )
    }

    override suspend fun deleteEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
    ): Resource<Unit> = try {
        eventsCollection(categoryId, clubId)
            .document(eventId)
            .delete()
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override fun getEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
    ): Flow<Event?> = callbackFlow {
        val registration = eventsCollection(categoryId, clubId)
            .document(eventId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err); return@addSnapshotListener
                }
                snap?.let { trySend(it.toObject(Event::class.java)) }
            }
        awaitClose { registration.remove() }
    }


    override fun getAllEvents(): Flow<List<Event>> = callbackFlow {
        val registration: ListenerRegistration = firebaseFirestore
            .collectionGroup(EVENTS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err); return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Event::class.java)) }
            }
        awaitClose { registration.remove() }
    }

    override fun getEventsByClubId(categoryId: String, clubId: String): Flow<List<Event>> =
        callbackFlow {
            val registration: ListenerRegistration = eventsCollection(categoryId, clubId)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        close(err); return@addSnapshotListener
                    }
                    snap?.let { trySend(it.toObjects(Event::class.java)) }
                }
            awaitClose { registration.remove() }
        }

    override suspend fun saveTicketInEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        participantIds: List<String>,
    ): Resource<Unit> {
        return try {
            val batch = firebaseFirestore.batch()
            val eventRef = eventsCollection(categoryId, clubId).document(eventId)

            // append one ticketId
            batch.update(
                eventRef,
                "ticketIds",
                FieldValue.arrayUnion(ticketId)
            )
            // append one teamId
            batch.update(
                eventRef,
                "teamsIds",
                FieldValue.arrayUnion(teamId)
            )

            Log.d("EventRepository", "Participant IDs: $participantIds")
            // append ALL participant IDs at once
            batch.update(
                eventRef,
                "participantsIds",
                FieldValue.arrayUnion(*participantIds.toTypedArray())
            )

            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }


    override suspend fun updateTicketInEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        oldParticipantIds: List<String>,
        participantIds: List<String>,
    ): Resource<Unit> {
        return try {
            val batch = firebaseFirestore.batch()
            val eventRef = eventsCollection(categoryId, clubId).document(eventId)

            batch.update(eventRef, "ticketIds", FieldValue.arrayUnion(ticketId))
            batch.update(eventRef, "teamsIds", FieldValue.arrayUnion(teamId))
            batch.update(
                eventRef,
                "participantsIds",
                FieldValue.arrayRemove(*oldParticipantIds.toTypedArray())
            )
            batch.update(
                eventRef,
                "participantsIds",
                FieldValue.arrayUnion(*participantIds.toTypedArray())
            )

            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun cancelTicketInEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        participantIds: List<String>,
    ): Resource<Unit> {
        return try {
            val batch = firebaseFirestore.batch()
            val eventRef = eventsCollection(categoryId, clubId).document(eventId)

            Log.d("EventRepository","teamIds: $teamId")
            Log.d("EventRepository","ticketIds: $ticketId")

            batch.update(eventRef, "ticketIds", FieldValue.arrayRemove(ticketId))
            batch.update(eventRef, "teamsIds", FieldValue.arrayRemove(teamId))
            batch.update(
                eventRef,
                "participantsIds",
                FieldValue.arrayRemove(*participantIds.toTypedArray())
            )


            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
