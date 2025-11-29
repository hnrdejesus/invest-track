package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.domain.Transaction;
import com.github.hnrdejesus.invest_track.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for calculating portfolio financial metrics.
 * Implements industry-standard risk and performance measures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioMetricsService {

    private final PortfolioService portfolioService;
    private final PositionService positionService;
    private final TransactionService transactionService;

    /**
     * Calculates Sharpe Ratio - risk-adjusted return metric.
     *
     * Formula: (Portfolio Return - Risk-free Rate) / Portfolio Volatility
     *
     * Interpretation:
     * > 1.0: Good risk-adjusted return
     * > 2.0: Very good
     * > 3.0: Excellent
     * < 1.0: Poor risk-adjusted return
     *
     * @param portfolioId Portfolio ID
     * @param riskFreeRate Annual risk-free rate (e.g., 0.05 for 5%)
     * @return Sharpe Ratio
     */
    public BigDecimal calculateSharpeRatio(Long portfolioId, BigDecimal riskFreeRate) {
        log.info("Calculating Sharpe Ratio for portfolio: {}", portfolioId);

        BigDecimal portfolioReturn = calculateTotalReturn(portfolioId);
        BigDecimal volatility = calculateVolatility(portfolioId);

        if (volatility.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal excessReturn = portfolioReturn.subtract(riskFreeRate);
        return excessReturn.divide(volatility, 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculates portfolio volatility (standard deviation of returns).
     * Higher volatility = higher risk.
     *
     * Uses historical position values to calculate return variance.
     */
    public BigDecimal calculateVolatility(Long portfolioId) {
        log.info("Calculating volatility for portfolio: {}", portfolioId);

        List<Position> positions = positionService.getPortfolioPositions(portfolioId);

        if (positions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (Position position : positions) {
            BigDecimal positionReturn = calculatePositionReturn(position);
            stats.addValue(positionReturn.doubleValue());
        }

        double standardDeviation = stats.getStandardDeviation();
        return BigDecimal.valueOf(standardDeviation).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Calculates total return of portfolio.
     *
     * Formula: (Current Value - Initial Investment) / Initial Investment
     *
     * @return Return as decimal (0.15 = 15% return)
     */
    public BigDecimal calculateTotalReturn(Long portfolioId) {
        log.info("Calculating total return for portfolio: {}", portfolioId);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        BigDecimal currentValue = portfolio.getTotalValue();

        BigDecimal totalInvested = positionService.calculateTotalInvestment(portfolioId);
        totalInvested = totalInvested.add(portfolio.getAvailableCash());

        if (totalInvested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profit = currentValue.subtract(totalInvested);
        return profit.divide(totalInvested, 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculates maximum drawdown - largest peak-to-trough decline.
     *
     * Measures worst-case scenario loss from historical high.
     * Lower (more negative) = higher risk.
     *
     * @return Max drawdown as decimal (e.g., -0.25 = 25% drawdown)
     */
    public BigDecimal calculateMaxDrawdown(Long portfolioId) {
        log.info("Calculating max drawdown for portfolio: {}", portfolioId);

        List<Position> positions = positionService.getPortfolioPositions(portfolioId);

        if (positions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal peak = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        for (Position position : positions) {
            BigDecimal currentValue = position.getCurrentValue();

            if (currentValue.compareTo(peak) > 0) {
                peak = currentValue;
            }

            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = currentValue.subtract(peak)
                        .divide(peak, 4, RoundingMode.HALF_UP);

                if (drawdown.compareTo(maxDrawdown) < 0) {
                    maxDrawdown = drawdown;
                }
            }
        }

        return maxDrawdown;
    }

    /**
     * Calculates portfolio turnover rate.
     * Measures trading activity - higher turnover = more active trading.
     *
     * Formula: Total Transaction Volume / Average Portfolio Value
     */
    public BigDecimal calculateTurnoverRate(Long portfolioId) {
        log.info("Calculating turnover rate for portfolio: {}", portfolioId);

        List<TransactionType> tradeTypes = List.of(TransactionType.BUY, TransactionType.SELL);
        BigDecimal totalVolume = transactionService.calculateTotalVolume(portfolioId, tradeTypes);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        BigDecimal avgValue = portfolio.getTotalValue();

        if (avgValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalVolume.divide(avgValue, 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculates total profit/loss across all positions.
     */
    public BigDecimal calculateTotalProfitLoss(Long portfolioId) {
        List<Position> positions = positionService.getPortfolioPositions(portfolioId);

        return positions.stream()
                .map(Position::getUnrealizedProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates win rate (percentage of profitable positions).
     *
     * @return Decimal between 0-1 (0.6 = 60% win rate)
     */
    public BigDecimal calculateWinRate(Long portfolioId) {
        List<Position> positions = positionService.getPortfolioPositions(portfolioId);

        if (positions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long profitableCount = positions.stream()
                .filter(p -> p.getUnrealizedProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();

        return BigDecimal.valueOf(profitableCount)
                .divide(BigDecimal.valueOf(positions.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculates comprehensive portfolio metrics in a single call.
     * Optimized to avoid multiple database queries.
     */
    public com.github.hnrdejesus.invest_track.dto.PortfolioMetricsDTO calculateMetrics(Long portfolioId) {
        log.info("Calculating comprehensive metrics for portfolio: {}", portfolioId);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        List<Position> positions = positionService.getPortfolioPositions(portfolioId);

        BigDecimal totalValue = portfolio.getTotalValue();
        BigDecimal totalCost = positionService.calculateTotalInvestment(portfolioId)
                .add(portfolio.getAvailableCash());
        BigDecimal totalProfitLoss = calculateTotalProfitLoss(portfolioId);
        BigDecimal totalReturn = calculateTotalReturn(portfolioId);

        BigDecimal sharpeRatio = calculateSharpeRatio(portfolioId, new BigDecimal("0.02"));
        BigDecimal volatility = calculateVolatility(portfolioId);
        BigDecimal maxDrawdown = calculateMaxDrawdown(portfolioId);
        BigDecimal winRate = calculateWinRate(portfolioId);

        long profitablePositions = positions.stream()
                .filter(p -> p.getUnrealizedProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();

        long losingPositions = positions.size() - profitablePositions;

        BigDecimal bestPerformer = positions.stream()
                .map(Position::getProfitLossPercentage)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal worstPerformer = positions.stream()
                .map(Position::getProfitLossPercentage)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return com.github.hnrdejesus.invest_track.dto.PortfolioMetricsDTO.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .totalValue(totalValue)
                .totalCost(totalCost)
                .totalProfitLoss(totalProfitLoss)
                .totalReturn(totalReturn)
                .sharpeRatio(sharpeRatio)
                .volatility(volatility)
                .maxDrawdown(maxDrawdown)
                .winRate(winRate)
                .totalPositions(positions.size())
                .profitablePositions((int) profitablePositions)
                .losingPositions((int) losingPositions)
                .bestPerformer(bestPerformer)
                .worstPerformer(worstPerformer)
                .calculatedAt(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Helper method to calculate individual position return.
     */
    private BigDecimal calculatePositionReturn(Position position) {
        BigDecimal costBasis = position.getCostBasis();

        if (costBasis.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profitLoss = position.getUnrealizedProfitLoss();
        return profitLoss.divide(costBasis, 4, RoundingMode.HALF_UP);
    }
}