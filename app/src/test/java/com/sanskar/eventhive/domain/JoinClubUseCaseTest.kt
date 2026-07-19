package com.sanskar.eventhive.domain

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.repository.ChatRepository
import com.sanskar.eventhive.data.repository.Inteface.ClubRepository
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class JoinClubUseCaseTest {

    private lateinit var clubRepository: ClubRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var joinClubUseCase: JoinClubUseCase

    @Before
    fun setUp() {
        clubRepository = mockk()
        userRepository = mockk()
        chatRepository = mockk()
        joinClubUseCase = JoinClubUseCase(userRepository, clubRepository, chatRepository)
    }

    @Test
    fun `when joinClub succeeds, userRepo joinClubForUser is called`() = runBlocking {
        // Arrange
        val categoryId = "cat1"
        val clubId = "club1"
        val userId = "user1"
        coEvery { clubRepository.joinClub(categoryId, clubId, userId) } returns Resource.Success(Unit)
        coEvery { userRepository.joinClubForUser(userId, categoryId, clubId) } returns Resource.Success(Unit)
        coEvery { chatRepository.grantAccess(any(), any(), any(), any()) } returns Unit

        // Act
        val result = joinClubUseCase(categoryId, clubId, userId)

        // Assert
        assertEquals(Resource.Success(Unit), result)
        coVerify(exactly = 1) { clubRepository.joinClub(categoryId, clubId, userId) }
        coVerify(exactly = 1) { userRepository.joinClubForUser(userId, categoryId, clubId) }
        coVerify(exactly = 1) { chatRepository.grantAccess("club_club1", "club", "club1", "cat1") }
    }

    @Test
    fun `when joinClub fails, userRepo joinClubForUser is NOT called`() = runBlocking {
        // Arrange
        val categoryId = "cat1"
        val clubId = "club1"
        val userId = "user1"
        val error = Exception("Network error")
        coEvery { clubRepository.joinClub(categoryId, clubId, userId) } returns Resource.Error(error)

        // Act
        val result = joinClubUseCase(categoryId, clubId, userId)

        // Assert
        assertEquals(Resource.Error(error), result)
        coVerify(exactly = 1) { clubRepository.joinClub(categoryId, clubId, userId) }
        coVerify(exactly = 0) { userRepository.joinClubForUser(any(), any(), any()) }
    }
}
