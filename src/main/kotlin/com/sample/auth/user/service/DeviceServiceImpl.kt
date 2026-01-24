package com.sample.auth.user.service

import com.sample.auth.user.dto.response.DeviceResponse
import com.sample.auth.user.repository.UserDeviceRepository
import org.springframework.stereotype.Service

@Service
class DeviceServiceImpl(
    private val userDeviceRepository: UserDeviceRepository,
) : DeviceService {
    override fun listDevices(userId: String, currentDeviceId: String): List<DeviceResponse> {
        return userDeviceRepository.findAllByUserId(userId).map {
            DeviceResponse.from(it, it.deviceId == currentDeviceId)
        }
    }

    override fun logoutDevice(userId: String, deviceId: String) {
        val device = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
        if (device != null) {
            userDeviceRepository.delete(device)
        }
    }
}
