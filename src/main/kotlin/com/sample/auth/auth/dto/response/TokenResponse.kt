package com.sample.auth.auth.dto.response


data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val refreshExpiresIn: Long,
)
