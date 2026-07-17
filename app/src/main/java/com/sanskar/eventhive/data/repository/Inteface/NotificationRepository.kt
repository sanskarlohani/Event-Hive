package com.sanskar.eventhive.data.repository.Inteface

import com.sanskar.eventhive.data.model.Notification
import com.sanskar.eventhive.data.model.SendMessageDto
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun sendMessage(dto: SendMessageDto)
    suspend fun broadcast(dto: SendMessageDto)
    fun saveNotification(notification: Notification)
    fun getNotifications(): Flow<List<Notification>>
}