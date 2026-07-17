package com.sanskar.eventhive.data.repository

import retrofit2.http.Body
import retrofit2.http.POST

data class CreateStudentGuideRequest(
    val name: String,
    val email: String,
    val collegeId: String,
)

data class CreateStudentGuideResponse(
    val uid: String = "",
)

data class ValidateCollegeCodeRequest(
    val code: String,
)

data class ValidateCollegeCodeResponse(
    val available: Boolean = false,
)

interface CloudFunctionApi {
    @POST("createStudentGuide")
    suspend fun createStudentGuide(@Body request: CreateStudentGuideRequest): CreateStudentGuideResponse

    @POST("validateCollegeCode")
    suspend fun validateCollegeCode(@Body request: ValidateCollegeCodeRequest): ValidateCollegeCodeResponse
}
