package com.sanskar.eventhive.data.repository.Inteface

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun createEvent(event: Event): Resource<Unit>
    suspend fun updateEvent(event: Event): Resource<Unit>
    suspend fun deleteEvent(categoryId: String, clubId: String, eventId: String): Resource<Unit>

    fun getEvent(categoryId: String, clubId: String, eventId: String): Flow<Event?>
    fun getAllEvents(): Flow<List<Event>>
    fun getEventsByClubId(categoryId: String, clubId: String): Flow<List<Event>>

    suspend fun saveTicketInEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        participantIds: List<String>,
    ): Resource<Unit>

    suspend fun updateTicketInEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        oldParticipantIds: List<String>,
        participantIds: List<String>,
    ): Resource<Unit>

    suspend fun cancelTicketInEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        participantIds: List<String>,
    ): Resource<Unit>



//    suspend fun getEventsByOrganizerId(organizerId: String): Resource<List<Event>>
//    suspend fun getEventsByCategoryId(categoryId: String): Resource<List<Event>>
//    fun observeAllEvents(): Flow<List<Event>>
//    fun observeEvent(categoryId: String, clubId: String, eventId: String): Flow<Event?>
//    fun observeEventsByClub(categoryId: String, clubId: String): Flow<List<Event>>
//    fun observeEventsByOrganizerId(organizerId: String): Flow<List<Event>>
//    fun observeEventsByCategoryId(categoryId: String): Flow<List<Event>>

}