package com.sanskar.eventhive.domain

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Team
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.data.repository.Inteface.EventRepository
import com.sanskar.eventhive.data.repository.Inteface.TicketRepository
import javax.inject.Inject

class UpdateTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    ){
    suspend operator fun invoke(
        oldParticipantIds: List<String>,
        ticket: Ticket,
        team: Team
        ): Resource<Unit> {
        when(val result = ticketRepository.updateTicket(
            ticket,
            team
        )){
            is Resource.Error -> return result
            is Resource.Success -> {
                val eventResult = eventRepository.updateTicketInEvent(
                    ticket.categoryId,
                    ticket.clubId,
                    ticket.eventId,
                    ticket.ticketId,
                    ticket.teamId,
                    oldParticipantIds,
                    ticket.participantIds
                )
                return when (eventResult) {
                    is Resource.Error -> eventResult
                    is Resource.Success -> result
                    else -> Resource.Error(Exception("Unknown error while updating event ticket mapping"))
                }
            }
            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))


    }
}
