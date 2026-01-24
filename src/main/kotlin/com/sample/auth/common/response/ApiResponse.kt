package com.sample.auth.common.response

import java.time.Instant


data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Instant = Instant.now(),
    val traceId: String? = null,
)

fun <T> successResponse(data: T, message: String? = null): ApiResponse<T> =
    ApiResponse(success = true, data = data, message = message)

fun <T> failureResponse(message: String): ApiResponse<T> =
    ApiResponse(success = false, message = message)
