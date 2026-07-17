package com.sanskar.eventhive.data

sealed class Resource<out R> {
    object Idle: Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: Exception) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
