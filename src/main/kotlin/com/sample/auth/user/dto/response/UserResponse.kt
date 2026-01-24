package com.sample.auth.user.dto.response

import com.sample.auth.user.entity.User
import java.time.Instant


data class UserResponse(
    val userId: String,
    val email: String,
    val name: String,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val marketingAgreed: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            userId = user.id.toString(),
            email = user.email,
            name = user.name,
            phoneNumber = user.phoneNumber,
            profileImageUrl = user.profileImageUrl,
            marketingAgreed = user.marketingAgreed,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    }
}
