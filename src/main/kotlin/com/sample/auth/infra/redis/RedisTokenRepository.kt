package com.sample.auth.infra.redis

import org.springframework.stereotype.Repository

@Repository
class RedisTokenRepository {
    fun storeRefreshToken(userId: String, deviceId: String, data: RefreshTokenData) {
        // TODO: persist to Redis
    }

    fun findRefreshToken(userId: String, deviceId: String): RefreshTokenData? {
        // TODO: fetch from Redis
        return null
    }

    fun blacklistToken(jti: String, data: BlacklistData) {
        // TODO: persist blacklist
    }
}
