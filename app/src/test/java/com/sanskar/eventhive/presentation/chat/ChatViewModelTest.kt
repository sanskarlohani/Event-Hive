package com.sanskar.eventhive.presentation.chat

import app.cash.turbine.test
import com.sanskar.eventhive.MainDispatcherRule
import com.sanskar.eventhive.data.model.ChatRoomMetadata
import com.sanskar.eventhive.data.model.ChatRoomPreview
import com.sanskar.eventhive.data.model.RtdbChatMessage
import com.sanskar.eventhive.data.repository.ChatRepository
import com.sanskar.eventhive.presentation.permission.PermissionHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var chatRepository: ChatRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        chatRepository = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        permissionHelper = mockk(relaxed = true)
        viewModel = ChatViewModel(chatRepository, auth, firestore, permissionHelper)
    }

    @Test
    fun `loadRooms updates state to Success with rooms`() = runTest {
        val rooms = listOf(ChatRoomPreview(id = "r1", name = "Room 1"))
        every { chatRepository.getRoomsForUser() } returns flowOf(rooms)

        viewModel.roomsState.test {
            assertEquals(UiState.Loading, awaitItem())
            viewModel.loadRooms()
            val success = awaitItem() as UiState.Success
            assertEquals(rooms, success.rooms)
        }
    }

    @Test
    fun `openRoom updates messagesState with messages and metadata`() = runTest {
        val roomId = "r1"
        val metadata = ChatRoomMetadata(id = roomId, type = "dm")
        val messages = listOf(RtdbChatMessage(id = "m1", text = "Hello"))
        
        every { chatRepository.getRoomMetadata(roomId) } returns flowOf(metadata)
        every { chatRepository.getMessages(roomId) } returns flowOf(messages)

        viewModel.messagesState.test {
            assertEquals(MessageUiState.Loading, awaitItem())
            viewModel.openRoom(roomId)
            
            // It might emit multiple times as metadata and messages arrive
            val result = expectMostRecentItem() as MessageUiState.Success
            assertEquals(messages, result.messages)
            assertEquals(metadata, result.roomMetadata)
        }
    }

    @Test
    fun `canDelete returns true if own message`() {
        val uid = "user1"
        val firebaseUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns uid
        
        val message = RtdbChatMessage(id = "m1", senderId = uid)
        
        val result = viewModel.canDelete(message)
        
        assertTrue(result)
    }

    @Test
    fun `canDelete returns true if has moderate permission`() {
        val uid = "user1"
        val firebaseUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "other"
        
        every { permissionHelper.hasPermission("canModerateChat") } returns true
        
        val message = RtdbChatMessage(id = "m1", senderId = "sender")
        
        val result = viewModel.canDelete(message)
        
        assertTrue(result)
    }
}
