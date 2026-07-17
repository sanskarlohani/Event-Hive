package com.sanskar.eventhive.data.repository.Inteface

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.SubEvent

interface SubEventRepository {
    suspend fun fetchSubEvents(categoryId: String, clubId: String, eventId: String): Resource<List<SubEvent>>
    suspend fun fetchSubEvent(categoryId: String, clubId: String, eventId: String, subEventId: String): Resource<SubEvent?>
    suspend fun createSubEvent(categoryId: String, clubId: String, eventId: String, subEvent: SubEvent): Resource<Unit>
    suspend fun updateSubEvent(categoryId: String, clubId: String, eventId: String, subEventId: String, subEvent: SubEvent): Resource<Unit>
    suspend fun deleteSubEvent(categoryId: String, clubId: String, eventId: String, subEventId: String): Resource<Unit>
    suspend fun registerForSubEvent(categoryId: String, clubId: String, eventId: String, subEventId: String, userId: String): Resource<Unit>
    suspend fun unregisterFromSubEvent(categoryId: String, clubId: String, eventId: String, subEventId: String, userId: String): Resource<Unit>
    suspend fun getSubEventRegistrations(categoryId: String, clubId: String, eventId: String, subEventId: String): Resource<List<String>>
    suspend fun getSubEventOrganizers(categoryId: String, clubId: String, eventId: String, subEventId: String): Resource<List<String>>
    suspend fun getSubEventSponsors(categoryId: String, clubId: String, eventId: String, subEventId: String): Resource<List<String>>
    suspend fun getSubEventTickets(categoryId: String, clubId: String, eventId: String, subEventId: String): Resource<List<String>>
    suspend fun getSubEventTicket(categoryId: String, clubId: String, eventId: String, subEventId: String, ticketId: String): Resource<String?>
    suspend fun issueSubEventTicket(categoryId: String, clubId: String, eventId: String, subEventId: String, ticket: String): Resource<Unit>
    suspend fun redeemSubEventTicket(categoryId: String, clubId: String, eventId: String, subEventId: String, ticketId: String): Resource<Unit>
    suspend fun getMySubEventTickets(categoryId: String, clubId: String, eventId: String, subEventId: String, userId: String): Resource<String?>
    suspend fun getMySubEventTicket(categoryId: String, clubId: String, eventId: String, subEventId: String, ticketId: String, userId: String): Resource<String?>
}