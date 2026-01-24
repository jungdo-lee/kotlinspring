package com.sample.auth.user.repository

import com.sample.auth.user.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository

interface UserDeviceRepository : JpaRepository<UserDevice, Long> {
    fun findAllByUserId(userId: String): List<UserDevice>
    fun findByUserIdAndDeviceId(userId: String, deviceId: String): UserDevice?
}
