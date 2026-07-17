package com.sanskar.eventhive.domain

import android.util.Log
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Team
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.data.repository.Inteface.EventRepository
import com.sanskar.eventhive.data.repository.Inteface.TicketRepository
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import javax.inject.Inject

class IssueTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val eventRepository:  EventRepository,
    private val userRepository:   UserRepository,
) {
    suspend operator fun invoke(ticket: Ticket, team: Team): Resource<Unit> {
        // 1) create the ticket + team
        val ticketRes = ticketRepository.issueTicket(ticket, team)
        if (ticketRes is Resource.Error) {
            Log.e("IssueTicket", "step1 failed", ticketRes.exception)
            return ticketRes
        }

        // 2) add this ticketId to the event
        val eventRes = eventRepository.saveTicketInEvent(
            categoryId = ticket.categoryId,
            clubId     = ticket.clubId,
            eventId    = ticket.eventId,
            ticketId   = ticket.ticketId,
            teamId     = ticket.teamId,
            participantIds = ticket.participantIds.ifEmpty { team.teamMemberIds }
        )
        Log.d("IssueTicket", "step2 ${ticket}")
        Log.d("IssueTicket", "step2 ${team.teamMemberIds}")

        if (eventRes is Resource.Error) {
            Log.e("IssueTicket", "step2 failed", eventRes.exception)
            return eventRes
        }

        // 3) link the ticket to the user
        val userRes = userRepository.issueTicketForUser(
            userId     = ticket.userId,
            categoryId = ticket.categoryId,
            clubId     = ticket.clubId,
            eventId    = ticket.eventId,
            ticketId   = ticket.ticketId
        )
        if (userRes is Resource.Error) {
            Log.e("IssueTicket", "step3 failed", userRes.exception)
            return userRes
        }

        return Resource.Success(Unit)
    }
}
