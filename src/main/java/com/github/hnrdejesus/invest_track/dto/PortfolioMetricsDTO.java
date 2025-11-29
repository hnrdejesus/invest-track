package com.github.hnrdejesus.invest_track.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Financial metrics for portfolio performance analysis.
 * Includes risk-adjusted returns and statistical measures.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioMetricsDTO {

    private Long portfolioId;
    private String portfolioName;

    // Value metrics
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalReturn;

    // Risk metrics
    private BigDecimal sharpeRatio;
    private BigDecimal volatility;
    private BigDecimal maxDrawdown;

    // Performance metrics
    private BigDecimal winRate;
    private Integer totalPositions;
    private Integer profitablePositions;
    private Integer losingPositions;

    // Individual position metrics
    private BigDecimal bestPerformer;
    private BigDecimal worstPerformer;

    // Additional stats
    private String calculatedAt;
}