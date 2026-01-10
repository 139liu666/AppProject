package com.peizhe.todo.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("email")
    val email: String,
    @SerialName("full_name")
    val name: String,
    @SerialName("avatar_medium")
    val avatar: String? = null
)
