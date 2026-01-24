package com.sample.auth.user.controller

import com.sample.auth.common.constants.AuthConstants
import com.sample.auth.common.response.ApiResponse
import com.sample.auth.common.response.successResponse
import com.sample.auth.user.dto.response.DeviceResponse
import com.sample.auth.user.service.DeviceService
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/v1/users/me/devices")
class DeviceController(
    private val deviceService: DeviceService,
) {
    @GetMapping
    fun listDevices(
        principal: Principal,
        @RequestHeader(AuthConstants.HEADER_DEVICE_ID) currentDeviceId: String,
    ): ApiResponse<List<DeviceResponse>> {
        val devices = deviceService.listDevices(principal.name, currentDeviceId)
        return successResponse(devices)
    }

    @DeleteMapping("/{deviceId}")
    fun logoutDevice(
        principal: Principal,
        @PathVariable deviceId: String,
    ): ApiResponse<Unit> {
        deviceService.logoutDevice(principal.name, deviceId)
        return successResponse(Unit, "Device logged out successfully")
    }
}
