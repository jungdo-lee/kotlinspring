package com.sample.auth.infra.logging

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component
class AuthEventLogger {
    private val logger = LoggerFactory.getLogger("com.sample.auth.event")

    fun logLoginSuccess(userId: String, deviceId: String, ipAddress: String) {
        enrichMdc(userId, deviceId, ipAddress)
        logger.info("LOGIN_SUCCESS - User logged in successfully")
    }

    fun logLoginFailure(email: String, deviceId: String, ipAddress: String, reason: String) {
        MDC.put("email", email)
        MDC.put("deviceId", deviceId)
        MDC.put("clientIp", ipAddress)
        MDC.put("failureReason", reason)
        logger.warn("LOGIN_FAILURE - Login attempt failed: {}", reason)
    }

    fun logLogout(userId: String, deviceId: String, logoutType: String) {
        enrichMdc(userId, deviceId, null)
        MDC.put("logoutType", logoutType)
        logger.info("LOGOUT - User logged out: {}", logoutType)
    }

    fun logTokenRefresh(userId: String, deviceId: String) {
        enrichMdc(userId, deviceId, null)
        logger.info("TOKEN_REFRESH - Access token refreshed")
    }

    fun logTokenRevoked(userId: String, deviceId: String, reason: String) {
        enrichMdc(userId, deviceId, null)
        MDC.put("revokeReason", reason)
        logger.warn("TOKEN_REVOKED - Token revoked: {}", reason)
    }

    private fun enrichMdc(userId: String?, deviceId: String?, ipAddress: String?) {
        userId?.let { MDC.put("userId", it) }
        deviceId?.let { MDC.put("deviceId", it) }
        ipAddress?.let { MDC.put("clientIp", it) }
    }
}
