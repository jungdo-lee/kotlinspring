package com.sample.auth.auth.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties @ConstructorBinding constructor(
    val issuer: String,
    val audience: String,
    val accessToken: TokenSettings,
    val refreshToken: TokenSettings,
) {
    data class TokenSettings(
        val expirationSeconds: Long,
    )
}
