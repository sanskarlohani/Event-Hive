package com.sanskar.eventhive.domain.usecase

import com.sanskar.eventhive.data.model.College
import com.sanskar.eventhive.data.repository.CollegeRepository
import com.sanskar.eventhive.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserCollegeUseCaseTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var collegeRepository: CollegeRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var userCollegeUseCase: UserCollegeUseCase

    @Before
    fun setUp() {
        auth = mockk()
        firebaseUser = mockk()
        collegeRepository = mockk()
        chatRepository = mockk()
        userCollegeUseCase = UserCollegeUseCase(auth, collegeRepository, chatRepository)

        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "u1"
        every { firebaseUser.email } returns "user@college.edu"
        coEvery { chatRepository.grantAccess(any(), any(), any(), any(), any(), any()) } returns Unit
    }

    @Test
    fun `assignAfterSignup returns AutoAssigned if already has college`() = runBlocking {
        coEvery { collegeRepository.getUserCollegeId("u1") } returns "col1"

        val result = userCollegeUseCase.assignAfterSignup()

        assertEquals(CollegeAssignResult.AutoAssigned("col1"), result)
    }

    @Test
    fun `assignAfterSignup returns AutoAssigned if domain matches a college`() = runBlocking {
        coEvery { collegeRepository.getUserCollegeId("u1") } returns ""
        val college = College(id = "col2", emailDomain = "college.edu")
        coEvery { collegeRepository.getCollegeByDomain("college.edu") } returns college
        coEvery { collegeRepository.assignUserCollege("u1", "col2") } returns Result.success(Unit)

        val result = userCollegeUseCase.assignAfterSignup()

        assertEquals(CollegeAssignResult.AutoAssigned("col2"), result)
    }

    @Test
    fun `assignAfterSignup returns NeedsCode if domain does not match any college`() = runBlocking {
        coEvery { collegeRepository.getUserCollegeId("u1") } returns ""
        coEvery { collegeRepository.getCollegeByDomain("college.edu") } returns null

        val result = userCollegeUseCase.assignAfterSignup()

        assertEquals(CollegeAssignResult.NeedsCode, result)
    }

    @Test
    fun `submitCode returns AutoAssigned if code is valid`() = runBlocking {
        val college = College(id = "col3", collegeCode = "CODE123")
        coEvery { collegeRepository.getCollegeByCode("CODE123") } returns college
        coEvery { collegeRepository.assignUserCollege("u1", "col3") } returns Result.success(Unit)

        val result = userCollegeUseCase.submitCode("code123")

        assertEquals(CollegeAssignResult.AutoAssigned("col3"), result)
    }

    @Test
    fun `submitCode returns InvalidCode if code is invalid`() = runBlocking {
        coEvery { collegeRepository.getCollegeByCode("WRONG") } returns null

        val result = userCollegeUseCase.submitCode("wrong")

        assertEquals(CollegeAssignResult.InvalidCode, result)
    }
}
