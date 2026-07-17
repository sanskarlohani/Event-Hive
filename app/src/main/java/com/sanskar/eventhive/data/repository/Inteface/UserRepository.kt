package com.sanskar.eventhive.data.repository.Inteface

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.model.UserClub
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUser(user: User): Resource<Unit>
    suspend fun getUser(userId: String): Resource<User?>
    suspend fun updateUser(user: User): Resource<Unit>
    suspend fun deleteUser(userId: String): Resource<Unit>
    suspend fun getAllUsers(): Resource<List<User>>
    suspend fun getCurrentUser(): User?
    fun observeAllUsers(): Flow<List<User>>
    fun observeUser(userId: String): Flow<User?>
    fun observeCurrentUser(): Flow<User?>


    suspend fun joinClubForUser(userId: String, categoryId: String, clubId: String): Resource<Unit>
    suspend fun leaveClubForUser(userId: String, categoryId: String, clubId: String): Resource<Unit>


    suspend fun issueTicketForUser(userId: String, categoryId: String, clubId: String, eventId: String,ticketId:String): Resource<Unit>
    suspend fun cancelTicketForUser(userId: String,ticketId: String): Resource<Unit>
    fun observeJoinedClubsForUser(userId: String): Flow<List<UserClub>>

//    suspend fun searchUsers(query: String): Resource<List<User>>
}
