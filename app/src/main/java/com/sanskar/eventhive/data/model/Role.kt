package com.sanskar.eventhive.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Role(
    val id: String = "",
    val name: String = "",
    val permissions: Map<String, Boolean> = emptyMap(),
)
