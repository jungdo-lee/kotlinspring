package com.sample.auth.auth.service

import com.sample.auth.auth.dto.request.LoginRequest
import com.sample.auth.auth.dto.request.RefreshRequest
import com.sample.auth.auth.dto.request.SignupRequest
import com.sample.auth.auth.dto.response.LoginResponse
import com.sample.auth.auth.dto.response.TokenResponse
import com.sample.auth.auth.security.jwt.JwtTokenProvider
import com.sample.auth.common.exception.ApiException
import com.sample.auth.common.exception.ErrorCode
import com.sample.auth.user.entity.User
import com.sample.auth.user.entity.enums.UserStatus
import com.sample.auth.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
) : AuthService {
    override fun signup(request: SignupRequest, deviceId: String): String {
        if (userRepository.existsByEmail(request.email)) {
            throw ApiException(ErrorCode.USER_EMAIL_EXISTS)
        }
        val user = User(
            id = UUID.randomUUID(),
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            phoneNumber = request.phoneNumber,
            marketingAgreed = request.marketingAgreed,
            status = UserStatus.ACTIVE,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        userRepository.save(user)
        return user.id.toString()
    }

    override fun login(request: LoginRequest, deviceId: String): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw ApiException(ErrorCode.AUTH_INVALID_CREDENTIALS)
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ApiException(ErrorCode.AUTH_INVALID_CREDENTIALS)
        }
        val tokenPair = jwtTokenProvider.createTokenPair(user, deviceId)
        return LoginResponse(
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken,
            tokenType = "Bearer",
            expiresIn = tokenPair.expiresIn,
            refreshExpiresIn = tokenPair.refreshExpiresIn,
            user = LoginResponse.UserSummary(
                userId = user.id.toString(),
                email = user.email,
                name = user.name,
            ),
        )
    }

    override fun refresh(request: RefreshRequest, deviceId: String): TokenResponse {
        val tokenPair = jwtTokenProvider.refreshTokenPair(request.refreshToken, deviceId)
        return TokenResponse(
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken,
            tokenType = "Bearer",
            expiresIn = tokenPair.expiresIn,
            refreshExpiresIn = tokenPair.refreshExpiresIn,
        )
    }

    override fun logout(userId: String, deviceId: String) {
        // TODO: revoke tokens and update device state in Redis
    }

    override fun logoutAll(userId: String) {
        // TODO: revoke all tokens for the user
    }
}
