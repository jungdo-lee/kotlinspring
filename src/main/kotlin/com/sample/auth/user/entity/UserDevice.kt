package com.sample.auth.user.entity

import com.sample.auth.user.entity.enums.OsType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "user_devices")
class UserDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val deviceId: String,

    val deviceName: String? = null,

    @Enumerated(EnumType.STRING)
    val osType: OsType? = null,

    val osVersion: String? = null,

    val appVersion: String? = null,

    val pushToken: String? = null,

    val lastLoginAt: Instant? = null,

    val lastLoginIp: String? = null,

    val lastAccessAt: Instant? = null,

    val isActive: Boolean = true,

    val createdAt: Instant = Instant.now(),

    val updatedAt: Instant = Instant.now(),
)
