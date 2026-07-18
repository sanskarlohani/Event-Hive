package com.sanskar.eventhive.ui.screen.Club.Event

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Event
import com.sanskar.eventhive.data.model.EventMode
import com.sanskar.eventhive.ui.viewModel.EventViewModel
import com.sanskar.eventhive.ui.viewModel.TicketViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class EventRegistrationUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navController = mockk<NavController>(relaxed = true)
    private val eventViewModel = mockk<EventViewModel>(relaxed = true)
    private val ticketViewModel = mockk<TicketViewModel>(relaxed = true)
    private val userViewModel = mockk<UserViewModel>(relaxed = true)

    @Test
    fun individualRegistration_registerButtonIsEnabledByDefault() {
        // Arrange
        val mockEvent = Event(
            title = "Solo Marathon",
            mode = EventMode.SINGLE,
            minTeamSize = 1,
            maxTeamSize = 1
        )
        
        setupMocks(mockEvent)

        // Act
        startScreen()

        // Assert
        composeTestRule.onNodeWithText("Solo Marathon").assertExists()
        composeTestRule.onNodeWithText("Register").assertIsEnabled()
    }

    @Test
    fun groupRegistration_registerButtonIsDisabledUntilTeamNameEntered() {
        // Arrange
        val mockEvent = Event(
            title = "Hackathon",
            mode = EventMode.GROUP,
            minTeamSize = 2,
            maxTeamSize = 4
        )
        
        setupMocks(mockEvent)

        // Act
        startScreen()

        // Assert
        // Initially disabled because teamName is empty and totalParticipantCount (1) < minTeamSize (2)
        composeTestRule.onNodeWithText("Register").assertIsNotEnabled()

        // Enter team name
        composeTestRule.onNodeWithText("Team Name").performTextInput("Team Rocket")
        
        // Still disabled because members < minTeamSize
        composeTestRule.onNodeWithText("Register").assertIsNotEnabled()
    }

    private fun setupMocks(event: Event) {
        every { eventViewModel.event } returns MutableStateFlow(event)
        every { ticketViewModel.actionStatus } returns MutableStateFlow(Resource.Idle)
        every { userViewModel.observeUser } returns MutableStateFlow(Resource.Success(mockk(relaxed = true)))
        every { ticketViewModel.membersNotRegistered } returns MutableStateFlow(emptyList())
    }

    private fun startScreen() {
        composeTestRule.setContent {
            EventRegistrationScreen(
                navController = navController,
                categoryId = "cat1",
                clubId = "club1",
                eventId = "event1",
                userId = "user1",
                eventViewModel = eventViewModel,
                ticketViewModel = ticketViewModel,
                userViewModel = userViewModel
            )
        }
    }
}
