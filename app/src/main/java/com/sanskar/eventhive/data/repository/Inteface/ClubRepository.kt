package com.sanskar.eventhive.data.repository.Inteface

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.data.model.ClubUser
import kotlinx.coroutines.flow.Flow

interface ClubRepository {
    suspend fun createClub(club: Club,clubUsr: ClubUser): Resource<Unit>
    suspend fun updateClub(club: Club): Resource<Unit>
    suspend fun deleteClub(categoryId: String, clubId: String): Resource<Unit>

    fun getClub(categoryId: String, clubId: String): Flow<Club?>
    fun getAllClubsByCategory(categoryId: String): Flow<List<Club>>
    fun getAllClubs(): Flow<List<Club>>

    suspend fun joinClub(categoryId: String, clubId: String, userId: String): Resource<Unit>
    suspend fun leaveClub(categoryId: String, clubId: String,userId: String): Resource<Unit>
    suspend fun changeClubVisibility(categoryId: String, clubId: String, visibility: Boolean): Resource<Unit>


    suspend fun addEventToClub(categoryId: String, clubId: String, eventId: String): Resource<Unit>
    suspend fun removeEventFromClub(categoryId: String, clubId: String, eventId: String): Resource<Unit>

    //club member collection
    fun getAllClubMembers(categoryId: String, clubId: String): Flow<List<ClubUser>>
    fun getClubMember(categoryId: String, clubId: String, userId: String): Flow<ClubUser?>
    suspend fun changeClubUserRole(categoryId: String, clubId: String, userId: String, role: String): Resource<Unit>

}
