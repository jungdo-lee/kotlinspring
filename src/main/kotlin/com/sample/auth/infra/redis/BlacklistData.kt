package com.sample.auth.infra.redis

import java.time.Instant

data class BlacklistData(
    val userId: String,
    val deviceId: String,
    val reason: String,
    val revokedAt: Instant,
)
