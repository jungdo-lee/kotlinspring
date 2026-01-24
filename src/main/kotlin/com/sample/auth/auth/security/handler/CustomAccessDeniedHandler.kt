package com.sample.auth.auth.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.sample.auth.common.exception.ErrorCode
import com.sample.auth.common.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        response.status = ErrorCode.AUTH_FORBIDDEN.status.value()
        response.contentType = "application/json"
        val payload = ErrorResponse(ErrorCode.AUTH_FORBIDDEN.name, ErrorCode.AUTH_FORBIDDEN.message)
        response.writer.write(objectMapper.writeValueAsString(payload))
    }
}
