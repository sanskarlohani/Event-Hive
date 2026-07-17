package com.sanskar.eventhive.data.model


enum class ChatType { GLOBAL, CLUB, EVENT, PRIVATE }
enum class ChatRole { OWNER, ADMIN, MOD, MEMBER }
enum class MessageType { TEXT, IMAGE, FILE }

data class ChatParticipant(
    val userId: String,
    val role: ChatRole,
    val joinedAt: Long = System.currentTimeMillis()
)

data class Chat(
    val chatId: String = System.currentTimeMillis().toString(),
    val type: ChatType,
    val relatedTo: RelatedTo? = null,
    val participants: List<ChatParticipant>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessagePreview: String? = null,
    val unreadCount: Int = 0
) {
    data class RelatedTo(
        val clubId: String? = null,
        val eventId: String? = null
    )
}

data class ChatMessage(
    val messageId: String = System.currentTimeMillis().toString(),  // Auto-generated ID
    val chatId: String,
    val senderId: String,
    val text: String? = null,
    val sentAt: Long = System.currentTimeMillis(),  // Auto-set timestamp
    val messageType: MessageType,
    val mediaUrl: String? = null,
    val fileSize: Long? = null
)