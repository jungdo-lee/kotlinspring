package com.sample.auth.auth.dto.request

data class LogoutRequest(
    val refreshToken: String? = null,
)
