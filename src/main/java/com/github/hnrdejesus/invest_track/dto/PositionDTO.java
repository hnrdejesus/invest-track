package com.github.hnrdejesus.invest_track.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Position data transfer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionDTO {

    private Long id;

    private Long portfolioId;

    private AssetDTO asset;

    private BigDecimal quantity;

    private BigDecimal averagePrice;

    private BigDecimal currentValue;

    private BigDecimal costBasis;

    private BigDecimal profitLoss;

    private BigDecimal profitLossPercentage;

    /**
     * Request DTO for buy/sell operations.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRequest {

        @NotNull(message = "Asset ID is required")
        private Long assetId;

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
        private BigDecimal quantity;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal price;

        @DecimalMin(value = "0.0", inclusive = true, message = "Fees must be non-negative")
        private BigDecimal fees = BigDecimal.ZERO;
    }
}