package com.sanskar.eventhive.domain

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.data.model.ClubUser
import com.sanskar.eventhive.data.repository.ChatRepository
import com.sanskar.eventhive.data.repository.Inteface.ClubCategoryRepository
import com.sanskar.eventhive.data.repository.Inteface.ClubRepository
import javax.inject.Inject

class CreateClubUseCase @Inject constructor(
    private val clubRepository: ClubRepository,
    private val categoryRepository: ClubCategoryRepository,
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(club: Club, clubUser: ClubUser): Resource<Unit> {
        when (val result = clubRepository.createClub(club, clubUser)) {
            is Resource.Error -> return result
            is Resource.Success -> {
                categoryRepository.addClubToCategory(club.categoryId, club.clubId)
                // Grant access to club chat for the creator
                chatRepository.grantAccess(
                    roomId = "club_${club.clubId}",
                    type = "club",
                    relatedId = club.clubId,
                    categoryId = club.categoryId
                )
                return result
            }
            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))
    }
}
