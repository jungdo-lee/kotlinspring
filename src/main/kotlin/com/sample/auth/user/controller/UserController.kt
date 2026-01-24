package com.sample.auth.user.controller

import com.sample.auth.common.response.ApiResponse
import com.sample.auth.common.response.successResponse
import com.sample.auth.user.dto.request.ChangePasswordRequest
import com.sample.auth.user.dto.request.DeleteAccountRequest
import com.sample.auth.user.dto.request.UpdateUserRequest
import com.sample.auth.user.dto.response.UserResponse
import com.sample.auth.user.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/v1/users/me")
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    fun me(principal: Principal): ApiResponse<UserResponse> {
        val response = userService.getMe(principal.name)
        return successResponse(response)
    }

    @PatchMapping
    fun update(
        principal: Principal,
        @RequestBody request: UpdateUserRequest,
    ): ApiResponse<UserResponse> {
        val response = userService.updateMe(principal.name, request)
        return successResponse(response)
    }

    @PutMapping("/password")
    fun changePassword(
        principal: Principal,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ApiResponse<Unit> {
        userService.changePassword(principal.name, request)
        return successResponse(Unit, "Password changed successfully. Please login again.")
    }

    @DeleteMapping
    fun deleteAccount(
        principal: Principal,
        @Valid @RequestBody request: DeleteAccountRequest,
    ): ApiResponse<Unit> {
        userService.deleteAccount(principal.name, request)
        return successResponse(Unit, "Account deleted successfully")
    }
}
