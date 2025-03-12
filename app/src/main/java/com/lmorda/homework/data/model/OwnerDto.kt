package com.lmorda.homework.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwnerDto(

    @SerialName("login")
    val login: String,

    @SerialName("avatar_url")
    val avatarUrl: String,

    @SerialName("html_url")
    val htmlUrl: String,
)
