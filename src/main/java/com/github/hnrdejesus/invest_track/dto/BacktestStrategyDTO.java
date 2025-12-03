package com.github.hnrdejesus.invest_track.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Strategy configuration for backtesting.
 * All fields are validated to ensure valid strategy parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestStrategyDTO {

    @NotBlank(message = "Strategy name is required")
    @Size(min = 3, max = 100, message = "Strategy name must be between 3 and 100 characters")
    private String strategyName;

    @NotNull(message = "Initial capital is required")
    @DecimalMin(value = "0.01", message = "Initial capital must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Initial capital must have at most 10 integer digits and 2 decimal places")
    private BigDecimal initialCapital;

    // Buy/Sell thresholds
    @NotNull(message = "Buy threshold is required")
    @DecimalMin(value = "-1.0", message = "Buy threshold must be greater than or equal to -100%")
    @DecimalMax(value = "0.0", message = "Buy threshold must be less than or equal to 0% (negative value)")
    private BigDecimal buyThreshold;    // Buy when price drops X% (e.g., -0.05 = -5%)

    @NotNull(message = "Sell threshold is required")
    @DecimalMin(value = "0.0", message = "Sell threshold must be greater than or equal to 0%")
    @DecimalMax(value = "10.0", message = "Sell threshold must be less than or equal to 1000%")
    private BigDecimal sellThreshold;   // Sell when price rises X% (e.g., 0.10 = 10%)

    // Position sizing
    @NotNull(message = "Max position size is required")
    @DecimalMin(value = "0.01", message = "Max position size must be at least 1%")
    @DecimalMax(value = "1.0", message = "Max position size cannot exceed 100%")
    private BigDecimal maxPositionSize;  // Max % of portfolio per position

    // Risk management
    @DecimalMin(value = "-1.0", message = "Stop loss must be greater than or equal to -100%")
    @DecimalMax(value = "0.0", message = "Stop loss must be less than or equal to 0% (negative value)")
    private BigDecimal stopLoss;         // Stop loss percentage (e.g., -0.15 = -15%)

    @DecimalMin(value = "0.0", message = "Take profit must be greater than or equal to 0%")
    @DecimalMax(value = "10.0", message = "Take profit must be less than or equal to 1000%")
    private BigDecimal takeProfit;       // Take profit percentage (e.g., 0.25 = 25%)

    // Rebalancing
    @Min(value = 0, message = "Rebalance days must be non-negative")
    @Max(value = 365, message = "Rebalance days cannot exceed 365")
    private Integer rebalanceDays;       // Rebalance every N days (0 = no rebalancing)
}