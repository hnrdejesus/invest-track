package com.github.hnrdejesus.invest_track.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Results from Monte Carlo simulation.
 * Contains probabilistic projections of portfolio value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonteCarloSimulationDTO {

    private Long portfolioId;
    private Integer iterations;
    private Integer daysProjected;

    // Current state
    private BigDecimal initialValue;

    // Statistical projections
    private BigDecimal expectedValue;
    private BigDecimal medianValue;
    private BigDecimal bestCase;       // 95th percentile
    private BigDecimal worstCase;      // 5th percentile

    // Confidence intervals
    private BigDecimal percentile90High;  // 90% confidence upper bound
    private BigDecimal percentile90Low;   // 90% confidence lower bound
    private BigDecimal percentile50High;  // 50% confidence upper bound
    private BigDecimal percentile50Low;   // 50% confidence lower bound

    // Risk metrics
    private Double probabilityOfLoss;      // Chance of losing money
    private Double probabilityOfDoubling;  // Chance of 2x returns

    // Historical data used
    private Double historicalReturn;       // Average daily return
    private Double historicalVolatility;   // Standard deviation

    // Distribution data for charting
    private List<BigDecimal> simulationResults;  // All iteration results

    private String calculatedAt;
}