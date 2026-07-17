package com.sanskar.eventhive.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.model.ChatRoomMetadata
import com.sanskar.eventhive.data.model.RtdbChatMessage
import com.sanskar.eventhive.data.repository.ChatRepository
import com.sanskar.eventhive.presentation.permission.PermissionHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val permissionHelper: PermissionHelper,
) : ViewModel() {
    private val _roomsState = MutableStateFlow<UiState>(UiState.Loading)
    val roomsState: StateFlow<UiState> = _roomsState.asStateFlow()

    private val _messagesState = MutableStateFlow<MessageUiState>(MessageUiState.Loading)
    val messagesState: StateFlow<MessageUiState> = _messagesState.asStateFlow()

    val inputText = MutableStateFlow("")

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var roomsJob: Job? = null
    private var messagesJob: Job? = null
    private var metadataJob: Job? = null
    private var currentRoomId: String = ""
    private var currentMetadata: ChatRoomMetadata = ChatRoomMetadata()
    private var loadedMessages: List<RtdbChatMessage> = emptyList()

    fun loadRooms() {
        roomsJob?.cancel()
        _roomsState.value = UiState.Loading
        roomsJob = viewModelScope.launch {
            chatRepository.getRoomsForUser().collect { rooms ->
                _roomsState.value = UiState.Success(rooms)
            }
        }
    }

    fun openRoom(roomId: String) {
        currentRoomId = roomId
        _messagesState.value = MessageUiState.Loading
        messagesJob?.cancel()
        metadataJob?.cancel()

        metadataJob = viewModelScope.launch {
            chatRepository.getRoomMetadata(roomId).collect { metadata ->
                currentMetadata = metadata
                if (loadedMessages.isNotEmpty()) {
                    _messagesState.value = MessageUiState.Success(loadedMessages, metadata)
                }
            }
        }

        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(roomId).collect { messages ->
                loadedMessages = messages
                _messagesState.value = MessageUiState.Success(messages, currentMetadata.copy(id = roomId))
            }
        }
    }

    fun sendMessage(text: String) {
        val roomId = currentRoomId
        if (roomId.isBlank()) return
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val uid = auth.currentUser?.uid.orEmpty()
            if (uid.isBlank()) {
                _messagesState.value = MessageUiState.Error("Not signed in")
                return@launch
            }
            val profile = firestore.collection("users").document(uid).get().await()
            val displayName = profile.getString("name") ?: auth.currentUser?.displayName ?: "User"
            val collegeId = profile.getString("collegeId")
            chatRepository.sendMessage(
                roomId = roomId,
                text = trimmed,
                displayName = displayName,
                isAnonymous = currentMetadata.type == "anonymous",
                collegeId = collegeId,
            ).onSuccess {
                inputText.value = ""
            }.onFailure {
                _messagesState.value = MessageUiState.Error(it.message ?: "Failed to send message")
            }
        }
    }

    fun loadOlderMessages() {
        val roomId = currentRoomId
        val oldest = loadedMessages.minByOrNull { it.timestamp } ?: return
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            runCatching {
                chatRepository.loadMoreMessages(roomId = roomId, beforeTimestamp = oldest.timestamp)
            }.onSuccess { older ->
                if (older.isNotEmpty()) {
                    val dedup = (loadedMessages + older).distinctBy { it.id }.sortedByDescending { it.timestamp }
                    loadedMessages = dedup
                    _messagesState.value = MessageUiState.Success(dedup, currentMetadata)
                }
            }.onFailure {
                _messagesState.value = MessageUiState.Error(it.message ?: "Failed to load messages")
            }
            _isLoadingMore.value = false
        }
    }

    fun deleteMessage(messageId: String) {
        val roomId = currentRoomId
        if (roomId.isBlank()) return
        viewModelScope.launch {
            chatRepository.deleteMessage(roomId, messageId)
                .onFailure {
                    _messagesState.value = MessageUiState.Error(it.message ?: "Delete failed")
                }
        }
    }

    fun canDelete(message: RtdbChatMessage): Boolean {
        val uid = auth.currentUser?.uid.orEmpty()
        val isOwn = message.senderId == uid
        val canModerate = permissionHelper.hasPermission("canModerateChat")
        return isOwn || canModerate
    }

    fun currentUid(): String = auth.currentUser?.uid.orEmpty()

    override fun onCleared() {
        roomsJob?.cancel()
        messagesJob?.cancel()
        metadataJob?.cancel()
        super.onCleared()
    }
}
