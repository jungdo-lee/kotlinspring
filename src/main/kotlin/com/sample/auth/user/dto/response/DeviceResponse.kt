package com.sample.auth.user.dto.response

import com.sample.auth.user.entity.UserDevice
import java.time.Instant


data class DeviceResponse(
    val deviceId: String,
    val deviceName: String?,
    val osType: String?,
    val osVersion: String?,
    val appVersion: String?,
    val lastLoginAt: Instant?,
    val lastAccessAt: Instant?,
    val ipAddress: String?,
    val isCurrent: Boolean,
) {
    companion object {
        fun from(device: UserDevice, isCurrent: Boolean): DeviceResponse = DeviceResponse(
            deviceId = device.deviceId,
            deviceName = device.deviceName,
            osType = device.osType?.name,
            osVersion = device.osVersion,
            appVersion = device.appVersion,
            lastLoginAt = device.lastLoginAt,
            lastAccessAt = device.lastAccessAt,
            ipAddress = device.lastLoginIp,
            isCurrent = isCurrent,
        )
    }
}
