package com.sample.auth.infra.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ratelimit")
data class RateLimitProperties(
    val loginLimit: Int = 5,
    val signupLimit: Int = 3,
    val refreshLimit: Int = 10,
    val apiLimit: Int = 100,
)
