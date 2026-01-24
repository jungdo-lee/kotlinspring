package com.sample.auth.auth.security.jwt

import com.sample.auth.common.exception.ApiException
import com.sample.auth.common.exception.ErrorCode
import com.sample.auth.user.entity.User
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Long,
        val refreshExpiresIn: Long,
    )

    fun createTokenPair(user: User, deviceId: String): TokenPair {
        val accessToken = createToken(TokenType.ACCESS, user.id.toString(), deviceId)
        val refreshToken = createToken(TokenType.REFRESH, user.id.toString(), deviceId)
        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessToken.expirationSeconds,
            refreshExpiresIn = jwtProperties.refreshToken.expirationSeconds,
        )
    }

    fun refreshTokenPair(refreshToken: String, deviceId: String): TokenPair {
        if (refreshToken.isBlank()) {
            throw ApiException(ErrorCode.AUTH_INVALID_REFRESH)
        }
        val userId = UUID.randomUUID().toString()
        val newAccessToken = createToken(TokenType.ACCESS, userId, deviceId)
        val newRefreshToken = createToken(TokenType.REFRESH, userId, deviceId)
        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = jwtProperties.accessToken.expirationSeconds,
            refreshExpiresIn = jwtProperties.refreshToken.expirationSeconds,
        )
    }

    fun parseUserId(token: String): String? {
        if (token.isBlank()) return null
        return token.split(":").getOrNull(1)
    }

    private fun createToken(type: TokenType, userId: String, deviceId: String): String {
        val now = Instant.now().epochSecond
        val exp = now + when (type) {
            TokenType.ACCESS -> jwtProperties.accessToken.expirationSeconds
            TokenType.REFRESH -> jwtProperties.refreshToken.expirationSeconds
        }
        return "${type.name.lowercase()}:$userId:$deviceId:$exp:${UUID.randomUUID()}"
    }
}
