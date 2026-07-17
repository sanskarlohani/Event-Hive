package com.sanskar.eventhive.presentation.chat

import androidx.compose.runtime.Immutable
import com.sanskar.eventhive.data.model.RtdbChatMessage
import com.sanskar.eventhive.data.model.ChatRoomMetadata
import com.sanskar.eventhive.data.model.ChatRoomPreview

sealed class UiState {
    data object Loading : UiState()
    @Immutable
    data class Success(val rooms: List<ChatRoomPreview>) : UiState()
    data class Error(val message: String) : UiState()
}

sealed class MessageUiState {
    data object Loading : MessageUiState()
    @Immutable
    data class Success(
        val messages: List<RtdbChatMessage>,
        val roomMetadata: ChatRoomMetadata,
    ) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}
