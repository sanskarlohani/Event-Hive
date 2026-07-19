package com.sanskar.eventhive.domain

import android.util.Log
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Team
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.data.repository.ChatRepository
import com.sanskar.eventhive.data.repository.Inteface.EventRepository
import com.sanskar.eventhive.data.repository.Inteface.TicketRepository
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class IssueTicketUseCaseTest {

    private lateinit var ticketRepository: TicketRepository
    private lateinit var eventRepository: EventRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var issueTicketUseCase: IssueTicketUseCase

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0

        ticketRepository = mockk()
        eventRepository = mockk()
        userRepository = mockk()
        chatRepository = mockk()
        issueTicketUseCase = IssueTicketUseCase(ticketRepository, eventRepository, userRepository, chatRepository)
    }

    @Test
    fun `when all steps succeed, returns success`() = runBlocking {
        // Arrange
        val ticket = Ticket(ticketId = "t1", categoryId = "c1", clubId = "cl1", eventId = "e1", userId = "u1", teamId = "tm1")
        val team = Team(teamId = "tm1", teamMemberIds = listOf("u1"))
        
        coEvery { ticketRepository.issueTicket(any(), any()) } returns Resource.Success(Unit)
        coEvery { eventRepository.saveTicketInEvent(any(), any(), any(), any(), any(), any()) } returns Resource.Success(Unit)
        coEvery { userRepository.issueTicketForUser(any(), any(), any(), any(), any()) } returns Resource.Success(Unit)
        coEvery { chatRepository.grantAccess(any(), any(), any(), any(), any()) } returns Unit

        // Act
        val result = issueTicketUseCase(ticket, team)

        // Assert
        assertEquals(Resource.Success(Unit), result)
        coVerify(exactly = 1) { ticketRepository.issueTicket(ticket, team) }
        coVerify(exactly = 1) { eventRepository.saveTicketInEvent("c1", "cl1", "e1", "t1", "tm1", listOf("u1")) }
        coVerify(exactly = 1) { userRepository.issueTicketForUser("u1", "c1", "cl1", "e1", "t1") }
        coVerify(exactly = 1) { chatRepository.grantAccess("event_e1", "event", "e1", "c1", "cl1") }
    }

    @Test
    fun `when step 1 fails, returns error and stops`() = runBlocking {
        // Arrange
        val ticket = Ticket()
        val team = Team()
        val error = Exception("Step 1 failed")
        coEvery { ticketRepository.issueTicket(any(), any()) } returns Resource.Error(error)

        // Act
        val result = issueTicketUseCase(ticket, team)

        // Assert
        assertEquals(Resource.Error(error), result)
        coVerify(exactly = 1) { ticketRepository.issueTicket(any(), any()) }
        coVerify(exactly = 0) { eventRepository.saveTicketInEvent(any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { userRepository.issueTicketForUser(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `when step 2 fails, returns error and stops`() = runBlocking {
        // Arrange
        val ticket = Ticket()
        val team = Team()
        val error = Exception("Step 2 failed")
        coEvery { ticketRepository.issueTicket(any(), any()) } returns Resource.Success(Unit)
        coEvery { eventRepository.saveTicketInEvent(any(), any(), any(), any(), any(), any()) } returns Resource.Error(error)

        // Act
        val result = issueTicketUseCase(ticket, team)

        // Assert
        assertEquals(Resource.Error(error), result)
        coVerify(exactly = 1) { eventRepository.saveTicketInEvent(any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { userRepository.issueTicketForUser(any(), any(), any(), any(), any()) }
    }
}
