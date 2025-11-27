package com.github.hnrdejesus.invest_track.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response structure for API exceptions.
 * Provides consistent error format across all endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * HTTP status code (400, 404, 500, etc.)
     */
    private int status;

    /**
     * Error type/category (e.g., "VALIDATION_ERROR", "NOT_FOUND")
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * API path where error occurred
     */
    private String path;

    /**
     * Timestamp when error occurred
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * List of validation errors (for field-level validation failures)
     */
    private List<FieldError> fieldErrors;

    /**
     * Represents a single field validation error
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}