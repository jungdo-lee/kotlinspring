package com.sample.auth.auth.service

import com.sample.auth.auth.dto.request.LoginRequest
import com.sample.auth.auth.dto.request.RefreshRequest
import com.sample.auth.auth.dto.request.SignupRequest
import com.sample.auth.auth.dto.response.LoginResponse
import com.sample.auth.auth.dto.response.TokenResponse

interface AuthService {
    fun signup(request: SignupRequest, deviceId: String): String
    fun login(request: LoginRequest, deviceId: String): LoginResponse
    fun refresh(request: RefreshRequest, deviceId: String): TokenResponse
    fun logout(userId: String, deviceId: String)
    fun logoutAll(userId: String)
}
