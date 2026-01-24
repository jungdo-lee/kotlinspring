package com.sample.auth.user.service

import com.sample.auth.common.exception.ApiException
import com.sample.auth.common.exception.ErrorCode
import com.sample.auth.user.dto.request.ChangePasswordRequest
import com.sample.auth.user.dto.request.DeleteAccountRequest
import com.sample.auth.user.dto.request.UpdateUserRequest
import com.sample.auth.user.dto.response.UserResponse
import com.sample.auth.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserService {
    override fun getMe(userId: String): UserResponse {
        val user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        return UserResponse.from(user)
    }

    override fun updateMe(userId: String, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        val updated = user.copyForUpdate(request)
        userRepository.save(updated)
        return UserResponse.from(updated)
    }

    override fun changePassword(userId: String, request: ChangePasswordRequest) {
        val user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            throw ApiException(ErrorCode.USER_PASSWORD_MISMATCH)
        }
        if (request.currentPassword == request.newPassword) {
            throw ApiException(ErrorCode.USER_PASSWORD_SAME)
        }
        val updated = user.copy(password = passwordEncoder.encode(request.newPassword))
        userRepository.save(updated)
    }

    override fun deleteAccount(userId: String, request: DeleteAccountRequest) {
        val user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ApiException(ErrorCode.USER_PASSWORD_MISMATCH)
        }
        val deleted = user.copyForDeletion()
        userRepository.save(deleted)
    }
}

private fun com.sample.auth.user.entity.User.copyForUpdate(request: UpdateUserRequest): com.sample.auth.user.entity.User {
    return com.sample.auth.user.entity.User(
        id = this.id,
        email = this.email,
        password = this.password,
        name = request.name ?: this.name,
        phoneNumber = request.phoneNumber ?: this.phoneNumber,
        profileImageUrl = this.profileImageUrl,
        status = this.status,
        marketingAgreed = this.marketingAgreed,
        lastLoginAt = this.lastLoginAt,
        createdAt = this.createdAt,
        updatedAt = java.time.Instant.now(),
        deletedAt = this.deletedAt,
    )
}

private fun com.sample.auth.user.entity.User.copyForDeletion(): com.sample.auth.user.entity.User {
    return com.sample.auth.user.entity.User(
        id = this.id,
        email = this.email,
        password = this.password,
        name = this.name,
        phoneNumber = this.phoneNumber,
        profileImageUrl = this.profileImageUrl,
        status = com.sample.auth.user.entity.enums.UserStatus.WITHDRAWN,
        marketingAgreed = this.marketingAgreed,
        lastLoginAt = this.lastLoginAt,
        createdAt = this.createdAt,
        updatedAt = java.time.Instant.now(),
        deletedAt = java.time.Instant.now(),
    )
}
