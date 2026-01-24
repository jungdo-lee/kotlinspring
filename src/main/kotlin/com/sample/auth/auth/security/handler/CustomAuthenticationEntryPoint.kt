package com.sample.auth.auth.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.sample.auth.common.exception.ErrorCode
import com.sample.auth.common.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.status = ErrorCode.AUTH_INVALID.status.value()
        response.contentType = "application/json"
        val payload = ErrorResponse(ErrorCode.AUTH_INVALID.name, ErrorCode.AUTH_INVALID.message)
        response.writer.write(objectMapper.writeValueAsString(payload))
    }
}
