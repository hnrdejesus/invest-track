package com.github.hnrdejesus.invest_track.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Portfolio data transfer.
 * Separates API contract from domain model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDTO {

    private Long id;

    @NotBlank(message = "Portfolio name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private BigDecimal totalValue;

    private BigDecimal availableCash;

    private Integer positionCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Request DTO for creating new portfolio.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "Portfolio name is required")
        @Size(min = 3, max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        @DecimalMin(value = "0.0", inclusive = true, message = "Initial cash must be positive")
        private BigDecimal initialCash = BigDecimal.ZERO;
    }

    /**
     * Request DTO for updating portfolio.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @NotBlank(message = "Portfolio name is required")
        @Size(min = 3, max = 100)
        private String name;

        @Size(max = 500)
        private String description;
    }

    /**
     * Request DTO for cash operations.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashOperationRequest {

        @DecimalMin(value = "0.01", message = "Amount must be positive")
        private BigDecimal amount;

        @Size(max = 500)
        private String notes;
    }
}