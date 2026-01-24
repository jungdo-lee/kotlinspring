package com.sample.auth.infra.redis

import java.time.Instant

data class RefreshTokenData(
    val tokenId: String,
    val userId: String,
    val deviceId: String,
    val deviceName: String? = null,
    val osType: String? = null,
    val appVersion: String? = null,
    val ipAddress: String? = null,
    val issuedAt: Instant,
    val expiresAt: Instant,
)
