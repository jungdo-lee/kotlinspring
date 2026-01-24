package com.sample.auth.user.entity

import com.sample.auth.user.entity.enums.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val name: String,

    val phoneNumber: String? = null,

    val profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    val status: UserStatus = UserStatus.ACTIVE,

    val marketingAgreed: Boolean = false,

    val lastLoginAt: Instant? = null,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant? = null,
)
