package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.dto.MonteCarloSimulationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Monte Carlo simulation service for probabilistic portfolio projections.
 * Simulates thousands of possible future scenarios based on historical volatility.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MonteCarloService {

    private final PortfolioService portfolioService;
    private final PositionService positionService;
    private final PortfolioMetricsService metricsService;

    @Value("${app.monte-carlo.default-iterations:10000}")
    private int defaultIterations;

    private final Random random = new Random();

    /**
     * Runs Monte Carlo simulation for portfolio projections.
     *
     * @param portfolioId Portfolio to simulate
     * @param iterations Number of simulations (default 10,000)
     * @param days Days to project forward
     * @return Simulation results with percentiles and probabilities
     */
    public MonteCarloSimulationDTO runSimulation(Long portfolioId, Integer iterations, Integer days) {
        log.info("Running Monte Carlo simulation: portfolio={}, iterations={}, days={}",
                portfolioId, iterations, days);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        List<Position> positions = positionService.getPortfolioPositions(portfolioId);

        if (positions.isEmpty()) {
            throw new IllegalArgumentException("Portfolio has no positions to simulate");
        }

        iterations = iterations != null ? iterations : defaultIterations;
        days = days != null ? days : 252; // 1 trading year default

        BigDecimal initialValue = portfolio.getTotalValue();

        // Calculate historical metrics
        BigDecimal volatility = metricsService.calculateVolatility(portfolioId);
        BigDecimal averageReturn = metricsService.calculateTotalReturn(portfolioId)
                .divide(BigDecimal.valueOf(252), 6, RoundingMode.HALF_UP); // Daily return

        double dailyReturn = averageReturn.doubleValue();
        double dailyVolatility = volatility.doubleValue();

        // Run simulations
        List<BigDecimal> simulationResults = new ArrayList<>();
        NormalDistribution normalDist = new NormalDistribution(dailyReturn, dailyVolatility);

        for (int i = 0; i < iterations; i++) {
            BigDecimal simulatedValue = simulateScenario(initialValue, days, normalDist);
            simulationResults.add(simulatedValue);
        }

        // Calculate statistics
        return buildSimulationDTO(portfolioId, iterations, days, initialValue,
                simulationResults, dailyReturn, dailyVolatility);
    }

    /**
     * Simulates one possible scenario using Geometric Brownian Motion.
     */
    private BigDecimal simulateScenario(BigDecimal initialValue, int days, NormalDistribution dist) {
        BigDecimal currentValue = initialValue;

        for (int day = 0; day < days; day++) {
            double randomReturn = dist.sample();
            BigDecimal dailyChange = BigDecimal.ONE.add(BigDecimal.valueOf(randomReturn));
            currentValue = currentValue.multiply(dailyChange);
        }

        return currentValue.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Builds DTO with statistical analysis of simulation results.
     */
    private MonteCarloSimulationDTO buildSimulationDTO(
            Long portfolioId,
            int iterations,
            int days,
            BigDecimal initialValue,
            List<BigDecimal> results,
            double dailyReturn,
            double dailyVolatility) {

        Collections.sort(results);

        DescriptiveStatistics stats = new DescriptiveStatistics();
        results.forEach(r -> stats.addValue(r.doubleValue()));

        // Percentiles
        BigDecimal median = BigDecimal.valueOf(stats.getPercentile(50));
        BigDecimal percentile95 = BigDecimal.valueOf(stats.getPercentile(95));
        BigDecimal percentile5 = BigDecimal.valueOf(stats.getPercentile(5));
        BigDecimal percentile90High = BigDecimal.valueOf(stats.getPercentile(90));
        BigDecimal percentile90Low = BigDecimal.valueOf(stats.getPercentile(10));
        BigDecimal percentile75 = BigDecimal.valueOf(stats.getPercentile(75));
        BigDecimal percentile25 = BigDecimal.valueOf(stats.getPercentile(25));

        // Probabilities
        long lossCount = results.stream()
                .filter(r -> r.compareTo(initialValue) < 0)
                .count();
        double probabilityOfLoss = (double) lossCount / iterations;

        long doublingCount = results.stream()
                .filter(r -> r.compareTo(initialValue.multiply(BigDecimal.valueOf(2))) >= 0)
                .count();
        double probabilityOfDoubling = (double) doublingCount / iterations;

        return MonteCarloSimulationDTO.builder()
                .portfolioId(portfolioId)
                .iterations(iterations)
                .daysProjected(days)
                .initialValue(initialValue)
                .expectedValue(BigDecimal.valueOf(stats.getMean()).setScale(2, RoundingMode.HALF_UP))
                .medianValue(median.setScale(2, RoundingMode.HALF_UP))
                .bestCase(percentile95.setScale(2, RoundingMode.HALF_UP))
                .worstCase(percentile5.setScale(2, RoundingMode.HALF_UP))
                .percentile90High(percentile90High.setScale(2, RoundingMode.HALF_UP))
                .percentile90Low(percentile90Low.setScale(2, RoundingMode.HALF_UP))
                .percentile50High(percentile75.setScale(2, RoundingMode.HALF_UP))
                .percentile50Low(percentile25.setScale(2, RoundingMode.HALF_UP))
                .probabilityOfLoss(probabilityOfLoss)
                .probabilityOfDoubling(probabilityOfDoubling)
                .historicalReturn(dailyReturn)
                .historicalVolatility(dailyVolatility)
                .simulationResults(results.stream()
                        .limit(100) // Return only 100 samples for charting
                        .collect(Collectors.toList()))
                .calculatedAt(LocalDateTime.now().toString())
                .build();
    }
}