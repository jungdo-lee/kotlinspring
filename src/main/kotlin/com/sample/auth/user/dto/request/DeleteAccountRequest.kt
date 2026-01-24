package com.sample.auth.user.dto.request

import jakarta.validation.constraints.NotBlank


data class DeleteAccountRequest(
    @field:NotBlank val password: String,
    val reason: String? = null,
)
