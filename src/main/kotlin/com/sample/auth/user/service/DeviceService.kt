package com.sample.auth.user.service

import com.sample.auth.user.dto.response.DeviceResponse

interface DeviceService {
    fun listDevices(userId: String, currentDeviceId: String): List<DeviceResponse>
    fun logoutDevice(userId: String, deviceId: String)
}
