package com.sanskar.eventhive.data.repository.Inteface

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.User
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<Unit>
    suspend fun signUp(user: User, password: String): Resource<Unit>
    suspend fun sendVerificationEmail(): Resource<Unit>
    suspend fun refreshUserEmailVerification(): Resource<Unit>
    suspend fun signOut(): Resource<Unit>
    suspend fun resetPassword(email: String): Resource<Unit>
    fun signInStatus(): Boolean
    fun getCurrentUser(): FirebaseUser?

}