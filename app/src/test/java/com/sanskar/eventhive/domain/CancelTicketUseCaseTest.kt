package com.sanskar.eventhive.domain

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.repository.ChatRepository
import com.sanskar.eventhive.data.repository.Inteface.EventRepository
import com.sanskar.eventhive.data.repository.Inteface.TicketRepository
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CancelTicketUseCaseTest {

    private lateinit var ticketRepository: TicketRepository
    private lateinit var eventRepository: EventRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var cancelTicketUseCase: CancelTicketUseCase

    @Before
    fun setUp() {
        ticketRepository = mockk()
        eventRepository = mockk()
        userRepository = mockk()
        chatRepository = mockk()
        cancelTicketUseCase = CancelTicketUseCase(ticketRepository, eventRepository, userRepository, chatRepository)
    }

    @Test
    fun `when all steps succeed, returns success`() = runBlocking {
        // Arrange
        val categoryId = "c1"
        val clubId = "cl1"
        val eventId = "e1"
        val ticketId = "t1"
        val teamId = "tm1"
        val userId = "u1"
        val participantIds = listOf("u1")

        coEvery { ticketRepository.cancelTicket(any()) } returns Resource.Success(Unit)
        coEvery { eventRepository.cancelTicketInEvent(any(), any(), any(), any(), any(), any()) } returns Resource.Success(Unit)
        coEvery { userRepository.cancelTicketForUser(any(), any()) } returns Resource.Success(Unit)
        coEvery { chatRepository.removeAccess(any()) } returns Unit

        // Act
        val result = cancelTicketUseCase(categoryId, clubId, eventId, ticketId, teamId, userId, participantIds)

        // Assert
        assertEquals(Resource.Success(Unit), result)
        coVerify(exactly = 1) { ticketRepository.cancelTicket(ticketId) }
        coVerify(exactly = 1) { eventRepository.cancelTicketInEvent(categoryId, clubId, eventId, ticketId, teamId, participantIds) }
        coVerify(exactly = 1) { userRepository.cancelTicketForUser(userId, ticketId) }
        coVerify(exactly = 1) { chatRepository.removeAccess("event_$eventId") }
    }

    @Test
    fun `when step 1 fails, returns error and stops`() = runBlocking {
        val error = Exception("Cancel failed")
        coEvery { ticketRepository.cancelTicket(any()) } returns Resource.Error(error)

        val result = cancelTicketUseCase("c1", "cl1", "e1", "t1", "tm1", "u1", emptyList())

        assertEquals(Resource.Error(error), result)
        coVerify(exactly = 0) { eventRepository.cancelTicketInEvent(any(), any(), any(), any(), any(), any()) }
    }
}
