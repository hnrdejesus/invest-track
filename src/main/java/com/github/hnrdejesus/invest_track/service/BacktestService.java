package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.dto.BacktestResultDTO;
import com.github.hnrdejesus.invest_track.dto.BacktestStrategyDTO;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for backtesting trading strategies on historical data.
 * Simulates strategy performance to evaluate effectiveness.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BacktestService {

    private final PortfolioService portfolioService;
    private final PositionService positionService;

    /**
     * Runs backtest for a given strategy on portfolio's current positions.
     * Simulates historical trades based on strategy rules.
     *
     * @param portfolioId Portfolio to backtest
     * @param strategy Strategy configuration
     * @param days Number of days to backtest (e.g., 252 = 1 year)
     * @return Backtest results with performance metrics
     */
    public BacktestResultDTO runBacktest(Long portfolioId, BacktestStrategyDTO strategy, Integer days) {
        log.info("Running backtest: portfolio={}, strategy={}, days={}",
                portfolioId, strategy.getStrategyName(), days);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        List<Position> positions = positionService.getPortfolioPositions(portfolioId);

        if (positions.isEmpty()) {
            throw new IllegalArgumentException("Portfolio has no positions to backtest");
        }

        days = days != null ? days : 252;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        // Simulate strategy
        BacktestSimulation simulation = simulateStrategy(portfolio, positions, strategy, startDate, endDate);

        // Calculate buy-and-hold comparison
        BigDecimal buyAndHoldReturn = calculateBuyAndHoldReturn(positions);

        return buildBacktestResult(portfolioId, strategy, simulation, buyAndHoldReturn, startDate, endDate, days);
    }

    /**
     * Simulates strategy execution over historical period.
     * Simplified simulation using position returns.
     */
    private BacktestSimulation simulateStrategy(
            Portfolio portfolio,
            List<Position> positions,
            BacktestStrategyDTO strategy,
            LocalDate startDate,
            LocalDate endDate) {

        BigDecimal cash = strategy.getInitialCapital();
        List<BacktestResultDTO.DailyValue> portfolioHistory = new ArrayList<>();
        List<BigDecimal> dailyReturns = new ArrayList<>();

        int totalTrades = 0;
        int winningTrades = 0;
        int losingTrades = 0;
        BigDecimal totalWins = BigDecimal.ZERO;
        BigDecimal totalLosses = BigDecimal.ZERO;

        BigDecimal peak = cash;
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        // Simulate daily trading
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            // Calculate portfolio value
            BigDecimal portfolioValue = cash;

            // Simulate trades based on strategy rules
            for (Position position : positions) {
                BigDecimal positionReturn = position.getProfitLossPercentage();

                // Check buy threshold
                if (positionReturn.compareTo(strategy.getBuyThreshold()) <= 0) {
                    BigDecimal buyAmount = cash.multiply(strategy.getMaxPositionSize());
                    if (buyAmount.compareTo(BigDecimal.ZERO) > 0) {
                        cash = cash.subtract(buyAmount);
                        portfolioValue = portfolioValue.add(buyAmount);
                        totalTrades++;
                    }
                }

                // Check sell threshold or stop loss
                if (positionReturn.compareTo(strategy.getSellThreshold()) >= 0 ||
                        positionReturn.compareTo(strategy.getStopLoss()) <= 0) {

                    BigDecimal sellValue = position.getCurrentValue();
                    BigDecimal costBasis = position.getCostBasis();
                    BigDecimal tradeReturn = sellValue.subtract(costBasis);

                    if (tradeReturn.compareTo(BigDecimal.ZERO) > 0) {
                        winningTrades++;
                        totalWins = totalWins.add(tradeReturn);
                    } else {
                        losingTrades++;
                        totalLosses = totalLosses.add(tradeReturn.abs());
                    }

                    cash = cash.add(sellValue);
                    totalTrades++;
                }
            }

            portfolioHistory.add(BacktestResultDTO.DailyValue.builder()
                    .date(date)
                    .value(portfolioValue.setScale(2, RoundingMode.HALF_UP))
                    .build());

            // Track drawdown
            if (portfolioValue.compareTo(peak) > 0) {
                peak = portfolioValue;
            }

            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = portfolioValue.subtract(peak)
                        .divide(peak, 4, RoundingMode.HALF_UP);
                if (drawdown.compareTo(maxDrawdown) < 0) {
                    maxDrawdown = drawdown;
                }
            }

            // Calculate daily return
            if (!portfolioHistory.isEmpty()) {
                int size = portfolioHistory.size();
                if (size > 1) {
                    BigDecimal prevValue = portfolioHistory.get(size - 2).getValue();
                    if (prevValue.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal dailyReturn = portfolioValue.subtract(prevValue)
                                .divide(prevValue, 6, RoundingMode.HALF_UP);
                        dailyReturns.add(dailyReturn);
                    }
                }
            }
        }

        BigDecimal finalValue = cash;

        // Calculate volatility
        DescriptiveStatistics stats = new DescriptiveStatistics();
        dailyReturns.forEach(r -> stats.addValue(r.doubleValue()));
        BigDecimal volatility = BigDecimal.valueOf(stats.getStandardDeviation())
                .setScale(4, RoundingMode.HALF_UP);

        return BacktestSimulation.builder()
                .finalCapital(finalValue)
                .maxDrawdown(maxDrawdown)
                .volatility(volatility)
                .totalTrades(totalTrades)
                .winningTrades(winningTrades)
                .losingTrades(losingTrades)
                .totalWins(totalWins)
                .totalLosses(totalLosses)
                .portfolioHistory(portfolioHistory)
                .build();
    }

    /**
     * Calculates simple buy-and-hold return for comparison.
     */
    private BigDecimal calculateBuyAndHoldReturn(List<Position> positions) {
        BigDecimal totalCost = positions.stream()
                .map(Position::getCostBasis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = positions.stream()
                .map(Position::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalValue.subtract(totalCost)
                .divide(totalCost, 4, RoundingMode.HALF_UP);
    }

    /**
     * Builds result DTO from simulation data.
     */
    private BacktestResultDTO buildBacktestResult(
            Long portfolioId,
            BacktestStrategyDTO strategy,
            BacktestSimulation simulation,
            BigDecimal buyAndHoldReturn,
            LocalDate startDate,
            LocalDate endDate,
            int days) {

        BigDecimal totalReturn = simulation.getFinalCapital().subtract(strategy.getInitialCapital());
        BigDecimal totalReturnPct = totalReturn
                .divide(strategy.getInitialCapital(), 4, RoundingMode.HALF_UP);

        BigDecimal winRate = simulation.getTotalTrades() > 0
                ? BigDecimal.valueOf(simulation.getWinningTrades())
                .divide(BigDecimal.valueOf(simulation.getTotalTrades()), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgWin = simulation.getWinningTrades() > 0
                ? simulation.getTotalWins()
                .divide(BigDecimal.valueOf(simulation.getWinningTrades()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgLoss = simulation.getLosingTrades() > 0
                ? simulation.getTotalLosses()
                .divide(BigDecimal.valueOf(simulation.getLosingTrades()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal profitFactor = simulation.getTotalLosses().compareTo(BigDecimal.ZERO) > 0
                ? simulation.getTotalWins()
                .divide(simulation.getTotalLosses(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate Sharpe Ratio
        BigDecimal riskFreeRate = new BigDecimal("0.02");
        BigDecimal sharpeRatio = simulation.getVolatility().compareTo(BigDecimal.ZERO) > 0
                ? totalReturnPct.subtract(riskFreeRate)
                .divide(simulation.getVolatility(), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal strategyVsBuyAndHold = totalReturnPct.subtract(buyAndHoldReturn);

        return BacktestResultDTO.builder()
                .strategyName(strategy.getStrategyName())
                .portfolioId(portfolioId)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(days)
                .initialCapital(strategy.getInitialCapital())
                .finalCapital(simulation.getFinalCapital())
                .totalReturn(totalReturn)
                .totalReturnPercentage(totalReturnPct)
                .sharpeRatio(sharpeRatio)
                .maxDrawdown(simulation.getMaxDrawdown())
                .volatility(simulation.getVolatility())
                .totalTrades(simulation.getTotalTrades())
                .winningTrades(simulation.getWinningTrades())
                .losingTrades(simulation.getLosingTrades())
                .winRate(winRate)
                .avgWin(avgWin)
                .avgLoss(avgLoss)
                .profitFactor(profitFactor)
                .buyAndHoldReturn(buyAndHoldReturn)
                .strategyVsBuyAndHold(strategyVsBuyAndHold)
                .portfolioHistory(simulation.getPortfolioHistory())
                .calculatedAt(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Helper class to hold simulation state.
     */
    @Data
    @Builder
    private static class BacktestSimulation {
        private BigDecimal finalCapital;
        private BigDecimal maxDrawdown;
        private BigDecimal volatility;
        private Integer totalTrades;
        private Integer winningTrades;
        private Integer losingTrades;
        private BigDecimal totalWins;
        private BigDecimal totalLosses;
        private List<BacktestResultDTO.DailyValue> portfolioHistory;
    }
}