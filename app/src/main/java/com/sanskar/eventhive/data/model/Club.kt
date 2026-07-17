package com.sanskar.eventhive.data.model


data class Category(
    val categoryId: String = System.currentTimeMillis().toString(),
    val name: String = "",
    val description: String? = null,
    val clubs: List<String> = emptyList(),
    val iconName: String? = null,             // for remote/uploaded images
    val iconVector: String? = null
)

//save as collection in firebase
data class ClubUser(
    val userId: String = "",
    val clubId: String = "",
    val categoryId: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val role: ClubRole = ClubRole.MEMBER,
)

enum class ClubRole { ADMIN, MEMBER ,FIC,FC,FACULTY_MEMBER,SECRETARY,JOINT_SECRETARY,NOMINATED_REPRESENTATIVE}

data class Club(
    val clubId: String = System.currentTimeMillis().toString(),
    val name: String = "",
    val description: String? = null,
    val categoryId: String = "",
    val logoUrl: String? = null,
    val bannerUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long = System.currentTimeMillis(),
    val lastEditor: String? = null,
    val members: List<String> = emptyList(),
    val events: List<String> = emptyList(),
    var isPublic: Boolean = false,
)

