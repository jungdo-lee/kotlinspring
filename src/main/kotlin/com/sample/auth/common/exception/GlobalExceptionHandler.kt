package com.sample.auth.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            code = ex.errorCode.name,
            message = ex.message,
        )
        return ResponseEntity.status(ex.errorCode.status).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.allErrors.firstOrNull()?.defaultMessage
            ?: ErrorCode.VALIDATION_FAILED.message
        val response = ErrorResponse(
            code = ErrorCode.VALIDATION_FAILED.name,
            message = message,
        )
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.status).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception", ex)
        val response = ErrorResponse(
            code = ErrorCode.SYSTEM_ERROR.name,
            message = ErrorCode.SYSTEM_ERROR.message,
        )
        return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.status).body(response)
    }
}
