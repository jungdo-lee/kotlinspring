package com.sample.auth.auth.dto.response


data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshExpiresIn: Long,
    val user: UserSummary,
) {
    data class UserSummary(
        val userId: String,
        val email: String,
        val name: String,
    )
}
