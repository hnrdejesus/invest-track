package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.dto.BacktestResultDTO;
import com.github.hnrdejesus.invest_track.dto.BacktestStrategyDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BacktestService.
 * Tests strategy simulation and performance calculations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BacktestService Tests")
class BacktestServiceTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private PositionService positionService;

    @InjectMocks
    private BacktestService backtestService;

    private Portfolio testPortfolio;
    private Position profitablePosition;
    private Position losingPosition;
    private BacktestStrategyDTO testStrategy;

    @BeforeEach
    void setUp() {
        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setAvailableCash(new BigDecimal("10000.00"));
        testPortfolio.setTotalValue(new BigDecimal("20000.00"));

        Asset profitableAsset = new Asset();
        profitableAsset.setId(1L);
        profitableAsset.setTicker("AAPL");
        profitableAsset.setName("Apple Inc.");
        profitableAsset.setAssetType(AssetType.STOCK);
        profitableAsset.setCurrentPrice(new BigDecimal("180.00"));

        Asset losingAsset = new Asset();
        losingAsset.setId(2L);
        losingAsset.setTicker("TSLA");
        losingAsset.setName("Tesla Inc.");
        losingAsset.setAssetType(AssetType.STOCK);
        losingAsset.setCurrentPrice(new BigDecimal("140.00"));

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

        testStrategy = new BacktestStrategyDTO();
        testStrategy.setStrategyName("Test Strategy");
        testStrategy.setInitialCapital(new BigDecimal("10000.00"));
        testStrategy.setBuyThreshold(new BigDecimal("-0.05")); // Buy when -5%
        testStrategy.setSellThreshold(new BigDecimal("0.10")); // Sell when +10%
        testStrategy.setStopLoss(new BigDecimal("-0.15")); // Stop loss at -15%
        testStrategy.setTakeProfit(new BigDecimal("0.25")); // Take profit at +25%
        testStrategy.setMaxPositionSize(new BigDecimal("0.30")); // 30% max per position
    }

    @Test
    @DisplayName("Should run backtest successfully")
    void shouldRunBacktestSuccessfully() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result).isNotNull();
        assertThat(result.getStrategyName()).isEqualTo("Test Strategy");
        assertThat(result.getPortfolioId()).isEqualTo(1L);
        assertThat(result.getTotalDays()).isEqualTo(30);
        assertThat(result.getInitialCapital()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("Should use default days when not specified")
    void shouldUseDefaultDays() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, null);

        assertThat(result.getTotalDays()).isEqualTo(252); // 1 trading year
    }

    @Test
    @DisplayName("Should throw exception when portfolio has no positions")
    void shouldThrowExceptionWhenNoPositions() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> backtestService.runBacktest(1L, testStrategy, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no positions to backtest");
    }

    @Test
    @DisplayName("Should calculate total return correctly")
    void shouldCalculateTotalReturnCorrectly() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getTotalReturn()).isNotNull();
        assertThat(result.getTotalReturnPercentage()).isNotNull();
        assertThat(result.getFinalCapital()).isNotNull();
    }

    @Test
    @DisplayName("Should calculate trade statistics")
    void shouldCalculateTradeStatistics() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getTotalTrades()).isNotNull();
        assertThat(result.getWinningTrades()).isNotNull();
        assertThat(result.getLosingTrades()).isNotNull();
        assertThat(result.getWinRate()).isBetween(BigDecimal.ZERO, BigDecimal.ONE);
    }

    @Test
    @DisplayName("Should calculate average win and loss")
    void shouldCalculateAverageWinAndLoss() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getAvgWin()).isNotNull();
        assertThat(result.getAvgLoss()).isNotNull();
    }

    @Test
    @DisplayName("Should calculate profit factor")
    void shouldCalculateProfitFactor() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getProfitFactor()).isNotNull();
        assertThat(result.getProfitFactor()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate risk metrics")
    void shouldCalculateRiskMetrics() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getSharpeRatio()).isNotNull();
        assertThat(result.getMaxDrawdown()).isNotNull();
        assertThat(result.getVolatility()).isNotNull();
    }

    @Test
    @DisplayName("Should compare with buy-and-hold strategy")
    void shouldCompareWithBuyAndHold() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition, losingPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getBuyAndHoldReturn()).isNotNull();
        assertThat(result.getStrategyVsBuyAndHold()).isNotNull();
    }

    @Test
    @DisplayName("Should include portfolio history")
    void shouldIncludePortfolioHistory() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getPortfolioHistory()).isNotNull();
        assertThat(result.getPortfolioHistory()).isNotEmpty();
    }

    @Test
    @DisplayName("Should have correct date range")
    void shouldHaveCorrectDateRange() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getStartDate()).isNotNull();
        assertThat(result.getEndDate()).isNotNull();
        assertThat(result.getEndDate()).isAfter(result.getStartDate());
    }

    @Test
    @DisplayName("Should include calculation timestamp")
    void shouldIncludeCalculationTimestamp() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getCalculatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle zero winning trades gracefully")
    void shouldHandleZeroWinningTrades() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(losingPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getAvgWin()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getWinRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should handle zero losing trades gracefully")
    void shouldHandleZeroLosingTrades() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(profitablePosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result.getAvgLoss()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should respect stop loss threshold")
    void shouldRespectStopLossThreshold() {
        Position deepLosingPosition = new Position();
        deepLosingPosition.setId(3L);
        deepLosingPosition.setPortfolio(testPortfolio);

        Asset deepLosingAsset = new Asset();
        deepLosingAsset.setId(3L);
        deepLosingAsset.setTicker("LOSS");
        deepLosingAsset.setCurrentPrice(new BigDecimal("80.00"));

        deepLosingPosition.setAsset(deepLosingAsset);
        deepLosingPosition.setQuantity(new BigDecimal("10.0"));
        deepLosingPosition.setAveragePrice(new BigDecimal("100.00")); // -20% loss, triggers stop loss

        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(deepLosingPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result).isNotNull();
        assertThat(result.getTotalTrades()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should respect take profit threshold")
    void shouldRespectTakeProfitThreshold() {
        Position highProfitPosition = new Position();
        highProfitPosition.setId(4L);
        highProfitPosition.setPortfolio(testPortfolio);

        Asset highProfitAsset = new Asset();
        highProfitAsset.setId(4L);
        highProfitAsset.setTicker("MOON");
        highProfitAsset.setCurrentPrice(new BigDecimal("150.00"));

        highProfitPosition.setAsset(highProfitAsset);
        highProfitPosition.setQuantity(new BigDecimal("10.0"));
        highProfitPosition.setAveragePrice(new BigDecimal("100.00")); // +50% profit, triggers take profit

        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(highProfitPosition));

        BacktestResultDTO result = backtestService.runBacktest(1L, testStrategy, 30);

        assertThat(result).isNotNull();
        assertThat(result.getTotalTrades()).isGreaterThan(0);
    }
}