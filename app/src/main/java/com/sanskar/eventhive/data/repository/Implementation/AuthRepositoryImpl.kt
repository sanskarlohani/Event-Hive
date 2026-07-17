package com.sanskar.eventhive.data.repository.Implementation

// Add these imports
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.repository.Inteface.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository
{
    private val allPermissionsEnabled = mapOf(
        "canCreateEvent" to true,
        "canEditEvent" to true,
        "canDeleteEvent" to true,
        "canCreateClub" to true,
        "canManageClubMembers" to true,
        "canViewReports" to true,
        "canModerateChat" to true,
        "canSendAnnouncements" to true,
    )

    override suspend fun login(email: String, password: String): Resource<Unit> {
        return try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
            val user = result.user
            if (user != null && !user.isEmailVerified) {
                Resource.Error(Exception("Email not verified, check your inbox"))
            } else {
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    private val users = firestore.collection("users")

    override suspend fun signUp(user: User, password: String): Resource<Unit> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(user.email, password)
                .await()

            val uid = authResult.user?.uid ?: user.userId.ifBlank { user.email }
            val normalizedEmail = user.email.trim().lowercase()
            val collegeSnap = if (user.collegeId.isNotBlank()) {
                firestore.collection("colleges").document(user.collegeId).get().await()
            } else {
                null
            }
            val assignedGuideEmail = collegeSnap?.getString("studentGuideEmail")?.trim()?.lowercase().orEmpty()
            val isAssignedGuide = assignedGuideEmail.isNotBlank() && assignedGuideEmail == normalizedEmail

            val payload = user.copy(
                userId = uid,
                email = normalizedEmail,
                systemRole = if (isAssignedGuide) "studentGuide" else user.systemRole,
                effectivePermissions = if (isAssignedGuide) allPermissionsEnabled else user.effectivePermissions,
            )
            users.document(uid).set(payload).await()
            if (isAssignedGuide && collegeSnap != null && collegeSnap.exists()) {
                firestore.collection("colleges")
                    .document(user.collegeId)
                    .update(
                        mapOf(
                            "studentGuideUid" to uid,
                            "updatedAt" to Timestamp.now(),
                        ),
                    )
                    .await()
            }

            sendVerificationEmail()

        }  catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun sendVerificationEmail(): Resource<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Resource.Success(Unit)
            } else {
                Resource.Error(Exception("User Not Logged In"))
            }

        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun refreshUserEmailVerification(): Resource<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.reload().await()
                if (user.isEmailVerified) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(Exception("Email still not verified"))
                }
            } else {
                Resource.Error(Exception("No logged-in user to refresh"))
            }

        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return  try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error(e)
        }

    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override fun signInStatus(): Boolean {
        val user = firebaseAuth.currentUser
        return user != null && user.isEmailVerified
    }
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}
