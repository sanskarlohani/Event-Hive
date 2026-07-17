package com.sanskar.eventhive.domain

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.data.model.ClubUser
import com.sanskar.eventhive.data.repository.Inteface.ClubCategoryRepository
import com.sanskar.eventhive.data.repository.Inteface.ClubRepository
import javax.inject.Inject

class CreateClubUseCase @Inject constructor(
    private val clubRepository: ClubRepository,
    private val categoryRepository: ClubCategoryRepository
) {
    suspend operator fun invoke(club: Club,clubUser: ClubUser): Resource<Unit> {
        when(val result = clubRepository.createClub(club,clubUser)){
            is Resource.Error -> return result
            is Resource.Success -> {
                categoryRepository.addClubToCategory(club.categoryId, club.clubId)
                return result
            }
            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))
    }

}