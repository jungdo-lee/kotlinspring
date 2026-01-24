package com.sample.auth.user.service

import com.sample.auth.user.dto.request.ChangePasswordRequest
import com.sample.auth.user.dto.request.DeleteAccountRequest
import com.sample.auth.user.dto.request.UpdateUserRequest
import com.sample.auth.user.dto.response.UserResponse

interface UserService {
    fun getMe(userId: String): UserResponse
    fun updateMe(userId: String, request: UpdateUserRequest): UserResponse
    fun changePassword(userId: String, request: ChangePasswordRequest)
    fun deleteAccount(userId: String, request: DeleteAccountRequest)
}
