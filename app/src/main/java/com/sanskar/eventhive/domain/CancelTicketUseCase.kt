package com.sanskar.eventhive.domain

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.repository.ChatRepository
import com.sanskar.eventhive.data.repository.Inteface.EventRepository
import com.sanskar.eventhive.data.repository.Inteface.TicketRepository
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import javax.inject.Inject

class CancelTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        userId: String,
        participantIds: List<String>,
    ): Resource<Unit> {

        val ticketRes = ticketRepository.cancelTicket(ticketId)
        if (ticketRes is Resource.Error) {
            return ticketRes
        }

        val eventRes = eventRepository.cancelTicketInEvent(
            categoryId = categoryId,
            clubId = clubId,
            eventId = eventId,
            ticketId = ticketId,
            teamId = teamId,
            participantIds = participantIds
        )

        if (eventRes is Resource.Error) {
            return eventRes
        }

        val userRes = userRepository.cancelTicketForUser(
            userId = userId,
            ticketId = ticketId
        )

        if (userRes is Resource.Error) {
            return userRes
        }

        // Remove chat access for the current user
        chatRepository.removeAccess("event_$eventId")

        return Resource.Success(Unit)
    }
}
