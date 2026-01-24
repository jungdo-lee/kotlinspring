package com.sample.auth.user.entity

import com.sample.auth.user.entity.enums.LoginType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "login_histories")
class LoginHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val deviceId: String,

    val ipAddress: String? = null,

    val userAgent: String? = null,

    val osType: String? = null,

    val appVersion: String? = null,

    val loginAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    val loginType: LoginType = LoginType.EMAIL,

    val success: Boolean = true,

    val failureReason: String? = null,
)
