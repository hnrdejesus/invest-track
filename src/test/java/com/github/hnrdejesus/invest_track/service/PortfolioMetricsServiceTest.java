package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.domain.TransactionType;
import com.github.hnrdejesus.invest_track.dto.PortfolioMetricsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PortfolioMetricsService.
 * Tests financial calculations and risk metrics.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioMetricsService Tests")
class PortfolioMetricsServiceTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private PositionService positionService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private PortfolioMetricsService metricsService;

    private Portfolio testPortfolio;
    private Position profitablePosition;
    private Position losingPosition;
    private Position breakEvenPosition;

    @BeforeEach
    void setUp() {
        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setAvailableCash(new BigDecimal("5000.00"));
        testPortfolio.setTotalValue(new BigDecimal("15000.00"));

        Asset profitableAsset = new Asset();
        profitableAsset.setId(1L);
        profitableAsset.setTicker("AAPL");
        profitableAsset.setCurrentPrice(new BigDecimal("180.00"));

        Asset losingAsset = new Asset();
        losingAsset.setId(2L);
        losingAsset.setTicker("TSLA");
        losingAsset.setCurrentPrice(new BigDecimal("140.00"));

        Asset breakEvenAsset = new Asset();
        breakEvenAsset.setId(3L);
        breakEvenAsset.setTicker("MSFT");
        breakEvenAsset.setCurrentPrice(new BigDecimal("100.00"));

        profitablePosition = new Position();
        profitablePosition.setId(1L);
        profitablePosition.setPortfolio(testPortfolio);
        profitablePosition.setAsset(profitableAsset);
        profitablePosition.setQuantity(new BigDecimal("10.0"));
        profitablePosition.setAveragePrice(new BigDecimal("150.00")); // Cost: $1,500, Current: $1,800 (+20%)

        losingPosition = new Position();
        losingPosition.setId(2L);
        losingPosition.setPortfolio(testPortfolio);
        losingPosition.setAsset(losingAsset);
        losingPosition.setQuantity(new BigDecimal("10.0"));
        losingPosition.setAveragePrice(new BigDecimal("160.00")); // Cost: $1,600, Current: $1,400 (-12.5%)

        breakEvenPosition = new Position();
        breakEvenPosition.setId(3L);
        breakEvenPosition.setPortfolio(testPortfolio);
        breakEvenPosition.setAsset(breakEvenAsset);
        breakEvenPosition.setQuantity(new BigDecimal("10.0"));
        breakEvenPosition.setAveragePrice(new BigDecimal("100.00")); // Cost: $1,000, Current: $1,000 (0%)
    }

    @Test
    @DisplayName("Should calculate Sharpe ratio correctly")
    void shouldCalculateSharpeRatioCorrectly() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition, losingPosition));
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("3100.00")); // 1500 + 1600

        BigDecimal riskFreeRate = new BigDecimal("0.02"); // 2% risk-free rate
        BigDecimal sharpeRatio = metricsService.calculateSharpeRatio(1L, riskFreeRate);

        assertThat(sharpeRatio).isNotNull();
        assertThat(sharpeRatio).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should return zero Sharpe ratio when volatility is zero")
    void shouldReturnZeroSharpeRatioWhenVolatilityIsZero() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Collections.singletonList(breakEvenPosition)); // Single position, no variance
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("1000.00"));

        BigDecimal sharpeRatio = metricsService.calculateSharpeRatio(1L, new BigDecimal("0.02"));

        assertThat(sharpeRatio).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate volatility from position returns")
    void shouldCalculateVolatilityFromPositionReturns() {
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BigDecimal volatility = metricsService.calculateVolatility(1L);

        assertThat(volatility).isNotNull();
        assertThat(volatility).isGreaterThan(BigDecimal.ZERO); // Should have variance between +20% and -12.5%
    }

    @Test
    @DisplayName("Should return zero volatility when no positions")
    void shouldReturnZeroVolatilityWhenNoPositions() {
        when(positionService.getPortfolioPositions(1L)).thenReturn(Collections.emptyList());

        BigDecimal volatility = metricsService.calculateVolatility(1L);

        assertThat(volatility).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate total return correctly")
    void shouldCalculateTotalReturnCorrectly() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("10000.00"));

        BigDecimal totalReturn = metricsService.calculateTotalReturn(1L);

        assertThat(totalReturn).isNotNull();
        // Total value: $15,000, Total invested: $10,000 + $5,000 cash = $15,000
        // Return should be 0% in this case
        assertThat(totalReturn).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should return zero return when no investment")
    void shouldReturnZeroReturnWhenNoInvestment() {
        Portfolio emptyPortfolio = new Portfolio();
        emptyPortfolio.setId(1L);
        emptyPortfolio.setAvailableCash(BigDecimal.ZERO);
        emptyPortfolio.setTotalValue(BigDecimal.ZERO);

        when(portfolioService.getPortfolioById(1L)).thenReturn(emptyPortfolio);
        when(positionService.calculateTotalInvestment(1L)).thenReturn(BigDecimal.ZERO);

        BigDecimal totalReturn = metricsService.calculateTotalReturn(1L);

        assertThat(totalReturn).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate max drawdown correctly")
    void shouldCalculateMaxDrawdownCorrectly() {
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BigDecimal maxDrawdown = metricsService.calculateMaxDrawdown(1L);

        assertThat(maxDrawdown).isNotNull();
        assertThat(maxDrawdown).isLessThanOrEqualTo(BigDecimal.ZERO); // Drawdown is negative or zero
    }

    @Test
    @DisplayName("Should return zero drawdown when no positions")
    void shouldReturnZeroDrawdownWhenNoPositions() {
        when(positionService.getPortfolioPositions(1L)).thenReturn(Collections.emptyList());

        BigDecimal maxDrawdown = metricsService.calculateMaxDrawdown(1L);

        assertThat(maxDrawdown).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate turnover rate")
    void shouldCalculateTurnoverRate() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(transactionService.calculateTotalVolume(1L, List.of(TransactionType.BUY, TransactionType.SELL)))
                .thenReturn(new BigDecimal("20000.00")); // $20k in trades

        BigDecimal turnoverRate = metricsService.calculateTurnoverRate(1L);

        assertThat(turnoverRate).isNotNull();
        // Turnover = 20000 / 15000 = 1.33 (133% turnover)
        assertThat(turnoverRate).isGreaterThan(BigDecimal.ONE);
    }

    @Test
    @DisplayName("Should return zero turnover when portfolio value is zero")
    void shouldReturnZeroTurnoverWhenPortfolioValueIsZero() {
        Portfolio emptyPortfolio = new Portfolio();
        emptyPortfolio.setId(1L);
        emptyPortfolio.setTotalValue(BigDecimal.ZERO);

        when(portfolioService.getPortfolioById(1L)).thenReturn(emptyPortfolio);
        when(transactionService.calculateTotalVolume(1L, List.of(TransactionType.BUY, TransactionType.SELL)))
                .thenReturn(new BigDecimal("5000.00"));

        BigDecimal turnoverRate = metricsService.calculateTurnoverRate(1L);

        assertThat(turnoverRate).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate total profit/loss")
    void shouldCalculateTotalProfitLoss() {
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BigDecimal totalPL = metricsService.calculateTotalProfitLoss(1L);

        assertThat(totalPL).isNotNull();
        // Profitable: +$300, Losing: -$200, Total: +$100
        assertThat(totalPL).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should calculate win rate correctly")
    void shouldCalculateWinRateCorrectly() {
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition, losingPosition, breakEvenPosition));

        BigDecimal winRate = metricsService.calculateWinRate(1L);

        assertThat(winRate).isNotNull();
        // 1 profitable out of 3 positions = 33.33%
        assertThat(winRate).isGreaterThan(BigDecimal.ZERO);
        assertThat(winRate).isLessThanOrEqualTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("Should return zero win rate when no positions")
    void shouldReturnZeroWinRateWhenNoPositions() {
        when(positionService.getPortfolioPositions(1L)).thenReturn(Collections.emptyList());

        BigDecimal winRate = metricsService.calculateWinRate(1L);

        assertThat(winRate).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate comprehensive metrics")
    void shouldCalculateComprehensiveMetrics() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition, losingPosition, breakEvenPosition));
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("4100.00")); // 1500 + 1600 + 1000

        PortfolioMetricsDTO metrics = metricsService.calculateMetrics(1L);

        assertThat(metrics).isNotNull();
        assertThat(metrics.getPortfolioId()).isEqualTo(1L);
        assertThat(metrics.getPortfolioName()).isEqualTo("Test Portfolio");
        assertThat(metrics.getTotalValue()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(metrics.getTotalPositions()).isEqualTo(3);
        assertThat(metrics.getProfitablePositions()).isEqualTo(1); // Only profitablePosition has profit
        assertThat(metrics.getLosingPositions()).isEqualTo(2); // losingPosition + breakEvenPosition
    }

    @Test
    @DisplayName("Should identify best and worst performers")
    void shouldIdentifyBestAndWorstPerformers() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition, losingPosition, breakEvenPosition));
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("4100.00"));

        PortfolioMetricsDTO metrics = metricsService.calculateMetrics(1L);

        assertThat(metrics.getBestPerformer()).isNotNull();
        assertThat(metrics.getWorstPerformer()).isNotNull();
        assertThat(metrics.getBestPerformer()).isGreaterThanOrEqualTo(metrics.getWorstPerformer());
    }

    @Test
    @DisplayName("Should include all risk metrics in comprehensive calculation")
    void shouldIncludeAllRiskMetricsInComprehensiveCalculation() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition, losingPosition));
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("3100.00"));

        PortfolioMetricsDTO metrics = metricsService.calculateMetrics(1L);

        assertThat(metrics.getSharpeRatio()).isNotNull();
        assertThat(metrics.getVolatility()).isNotNull();
        assertThat(metrics.getMaxDrawdown()).isNotNull();
        assertThat(metrics.getWinRate()).isNotNull();
        assertThat(metrics.getTotalReturn()).isNotNull();
        assertThat(metrics.getTotalProfitLoss()).isNotNull();
    }

    @Test
    @DisplayName("Should include calculation timestamp")
    void shouldIncludeCalculationTimestamp() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Arrays.asList(profitablePosition));
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("1500.00"));

        PortfolioMetricsDTO metrics = metricsService.calculateMetrics(1L);

        assertThat(metrics.getCalculatedAt()).isNotNull();
        assertThat(metrics.getCalculatedAt()).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle portfolio with only profitable positions")
    void shouldHandlePortfolioWithOnlyProfitablePositions() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Collections.singletonList(profitablePosition));
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("1500.00"));

        PortfolioMetricsDTO metrics = metricsService.calculateMetrics(1L);

        assertThat(metrics.getProfitablePositions()).isEqualTo(1);
        assertThat(metrics.getLosingPositions()).isEqualTo(0);
        assertThat(metrics.getWinRate()).isEqualByComparingTo(BigDecimal.ONE); // 100% win rate
    }

    @Test
    @DisplayName("Should handle portfolio with only losing positions")
    void shouldHandlePortfolioWithOnlyLosingPositions() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L))
                .thenReturn(Collections.singletonList(losingPosition));
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("1600.00"));

        PortfolioMetricsDTO metrics = metricsService.calculateMetrics(1L);

        assertThat(metrics.getProfitablePositions()).isEqualTo(0);
        assertThat(metrics.getLosingPositions()).isEqualTo(1);
        assertThat(metrics.getWinRate()).isEqualByComparingTo(BigDecimal.ZERO); // 0% win rate
    }
}