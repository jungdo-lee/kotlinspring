package com.sample.auth.user.dto.request


data class UpdateUserRequest(
    val name: String? = null,
    val phoneNumber: String? = null,
)
