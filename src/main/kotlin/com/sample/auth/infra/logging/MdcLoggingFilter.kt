package com.sample.auth.infra.logging

import com.sample.auth.common.constants.AuthConstants
import com.sample.auth.common.util.IpUtils
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class MdcLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            MDC.put("traceId", request.getHeader(AuthConstants.HEADER_REQUEST_ID) ?: UUID.randomUUID().toString())
            MDC.put("deviceId", request.getHeader(AuthConstants.HEADER_DEVICE_ID) ?: "unknown")
            MDC.put("clientIp", IpUtils.extractClientIp(request))
            MDC.put("userAgent", request.getHeader("User-Agent") ?: "unknown")
            MDC.put("appVersion", request.getHeader(AuthConstants.HEADER_APP_VERSION) ?: "unknown")
            MDC.put("osType", request.getHeader(AuthConstants.HEADER_OS_TYPE) ?: "unknown")
            MDC.put("requestMethod", request.method)
            MDC.put("requestUri", request.requestURI)
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
