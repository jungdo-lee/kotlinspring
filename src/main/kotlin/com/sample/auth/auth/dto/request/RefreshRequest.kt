package com.sample.auth.auth.dto.request

import jakarta.validation.constraints.NotBlank


data class RefreshRequest(
    @field:NotBlank val refreshToken: String,
)
