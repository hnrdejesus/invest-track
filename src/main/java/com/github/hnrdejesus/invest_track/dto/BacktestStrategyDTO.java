package com.github.hnrdejesus.invest_track.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Strategy configuration for backtesting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestStrategyDTO {

    private String strategyName;
    private BigDecimal initialCapital;

    // Buy/Sell thresholds
    private BigDecimal buyThreshold;    // Buy when price drops X% (e.g., -0.05 = -5%)
    private BigDecimal sellThreshold;   // Sell when price rises X% (e.g., 0.10 = 10%)

    // Position sizing
    private BigDecimal maxPositionSize;  // Max % of portfolio per position

    // Risk management
    private BigDecimal stopLoss;         // Stop loss percentage (e.g., -0.15 = -15%)
    private BigDecimal takeProfit;       // Take profit percentage (e.g., 0.25 = 25%)

    // Rebalancing
    private Integer rebalanceDays;       // Rebalance every N days (0 = no rebalancing)
}