package com.github.hnrdejesus.invest_track.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Results from backtesting a trading strategy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResultDTO {

    private String strategyName;
    private Long portfolioId;

    // Time period
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;

    // Capital
    private BigDecimal initialCapital;
    private BigDecimal finalCapital;
    private BigDecimal totalReturn;
    private BigDecimal totalReturnPercentage;

    // Performance metrics
    private BigDecimal sharpeRatio;
    private BigDecimal maxDrawdown;
    private BigDecimal volatility;

    // Trading statistics
    private Integer totalTrades;
    private Integer winningTrades;
    private Integer losingTrades;
    private BigDecimal winRate;
    private BigDecimal avgWin;
    private BigDecimal avgLoss;
    private BigDecimal profitFactor;  // Total wins / Total losses

    // Comparison with buy-and-hold
    private BigDecimal buyAndHoldReturn;
    private BigDecimal strategyVsBuyAndHold;  // Strategy return - Buy&Hold return

    // Daily portfolio values for charting
    private List<DailyValue> portfolioHistory;

    private String calculatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyValue {
        private LocalDate date;
        private BigDecimal value;
    }
}