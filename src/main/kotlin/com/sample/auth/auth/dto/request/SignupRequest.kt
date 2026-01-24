package com.sample.auth.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank


data class SignupRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val name: String,
    val phoneNumber: String? = null,
    val marketingAgreed: Boolean = false,
)
