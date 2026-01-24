package com.sample.auth.auth.controller

import com.sample.auth.auth.dto.request.LoginRequest
import com.sample.auth.auth.dto.request.RefreshRequest
import com.sample.auth.auth.dto.request.SignupRequest
import com.sample.auth.auth.dto.response.LoginResponse
import com.sample.auth.auth.dto.response.TokenResponse
import com.sample.auth.auth.service.AuthService
import com.sample.auth.common.constants.AuthConstants
import com.sample.auth.common.response.ApiResponse
import com.sample.auth.common.response.successResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    fun signup(
        @RequestHeader(AuthConstants.HEADER_DEVICE_ID) deviceId: String,
        @Valid @RequestBody request: SignupRequest,
    ): ApiResponse<Map<String, String>> {
        val userId = authService.signup(request, deviceId)
        return successResponse(mapOf("userId" to userId))
    }

    @PostMapping("/login")
    fun login(
        @RequestHeader(AuthConstants.HEADER_DEVICE_ID) deviceId: String,
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<LoginResponse> {
        val response = authService.login(request, deviceId)
        return successResponse(response)
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader(AuthConstants.HEADER_DEVICE_ID) deviceId: String,
        @Valid @RequestBody request: RefreshRequest,
    ): ApiResponse<TokenResponse> {
        val response = authService.refresh(request, deviceId)
        return successResponse(response)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader(AuthConstants.HEADER_DEVICE_ID) deviceId: String,
        principal: Principal,
    ): ApiResponse<Unit> {
        authService.logout(principal.name, deviceId)
        return successResponse(Unit, "Successfully logged out")
    }

    @PostMapping("/logout/all")
    fun logoutAll(
        principal: Principal,
    ): ApiResponse<Map<String, Int>> {
        authService.logoutAll(principal.name)
        return successResponse(mapOf("loggedOutDevices" to 0), "Successfully logged out from all devices")
    }
}
