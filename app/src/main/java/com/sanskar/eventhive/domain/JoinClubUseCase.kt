package com.sanskar.eventhive.domain

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.repository.ChatRepository
import com.sanskar.eventhive.data.repository.Inteface.ClubRepository
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import javax.inject.Inject

class JoinClubUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val clubRepository: ClubRepository,
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(categoryId: String, clubId: String, userId: String): Resource<Unit> {
        when (val result = clubRepository.joinClub(categoryId, clubId, userId)) {
            is Resource.Error -> return result
            is Resource.Success -> {
                userRepository.joinClubForUser(userId, categoryId, clubId)
                // Grant access to club chat
                chatRepository.grantAccess(
                    roomId = "club_$clubId",
                    type = "club",
                    relatedId = clubId,
                    categoryId = categoryId
                )
                return result
            }
            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))
    }
}
