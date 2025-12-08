package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.dto.MonteCarloSimulationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MonteCarloService.
 * Tests probabilistic simulations and statistical calculations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MonteCarloService Tests")
class MonteCarloServiceTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private PositionService positionService;

    @Mock
    private PortfolioMetricsService metricsService;

    @InjectMocks
    private MonteCarloService monteCarloService;

    private Portfolio testPortfolio;
    private Position testPosition;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(monteCarloService, "defaultIterations", 1000);
        ReflectionTestUtils.setField(monteCarloService, "defaultVolatility", 0.01);
        ReflectionTestUtils.setField(monteCarloService, "defaultReturn", 0.0003);

        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setAvailableCash(new BigDecimal("5000.00"));
        testPortfolio.setTotalValue(new BigDecimal("15000.00"));

        Asset testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setTicker("AAPL");
        testAsset.setName("Apple Inc.");
        testAsset.setAssetType(AssetType.STOCK);

        testPosition = new Position();
        testPosition.setId(1L);
        testPosition.setPortfolio(testPortfolio);
        testPosition.setAsset(testAsset);
        testPosition.setQuantity(new BigDecimal("10.0"));
        testPosition.setAveragePrice(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should run Monte Carlo simulation successfully")
    void shouldRunSimulationSuccessfully() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15")); // 15% volatility
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08")); // 8% annual return

        // Run simulation with small iteration count for test speed
        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        assertThat(result).isNotNull();
        assertThat(result.getPortfolioId()).isEqualTo(1L);
        assertThat(result.getIterations()).isEqualTo(100);
        assertThat(result.getDaysProjected()).isEqualTo(30);
        assertThat(result.getInitialValue()).isEqualByComparingTo(new BigDecimal("15000.00"));
    }

    @Test
    @DisplayName("Should use default iterations when not specified")
    void shouldUseDefaultIterations() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        // Pass null for iterations to use default
        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, null, 30);

        assertThat(result.getIterations()).isEqualTo(1000); // Default value
    }

    @Test
    @DisplayName("Should use default days when not specified")
    void shouldUseDefaultDays() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        // Pass null for days to use default (252 trading days = 1 year)
        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, null);

        assertThat(result.getDaysProjected()).isEqualTo(252);
    }

    @Test
    @DisplayName("Should throw exception when portfolio has no positions")
    void shouldThrowExceptionWhenNoPositions() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> monteCarloService.runSimulation(1L, 100, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no positions to simulate");
    }

    @Test
    @DisplayName("Should use default volatility when historical data is zero")
    void shouldUseDefaultVolatilityWhenZero() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(BigDecimal.ZERO);
        when(metricsService.calculateTotalReturn(1L)).thenReturn(BigDecimal.ZERO);

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        assertThat(result).isNotNull();
        // Fallback to configured default when historical data unavailable
        assertThat(result.getHistoricalVolatility()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Should use default return when historical data is zero")
    void shouldUseDefaultReturnWhenZero() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(BigDecimal.ZERO);

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        assertThat(result).isNotNull();
        // Fallback to configured default when historical data unavailable
        assertThat(result.getHistoricalReturn()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Should use historical data when available")
    void shouldUseHistoricalDataWhenAvailable() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));

        BigDecimal historicalVol = new BigDecimal("0.15");
        BigDecimal historicalReturn = new BigDecimal("0.08");

        when(metricsService.calculateVolatility(1L)).thenReturn(historicalVol);
        when(metricsService.calculateTotalReturn(1L)).thenReturn(historicalReturn);

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        // Annual return converted to daily (252 trading days)
        double dailyReturn = historicalReturn.doubleValue() / 252;
        assertThat(result.getHistoricalReturn()).isCloseTo(dailyReturn, within(0.0001));
        assertThat(result.getHistoricalVolatility()).isEqualTo(historicalVol.doubleValue());
    }

    @Test
    @DisplayName("Should calculate statistical measures correctly")
    void shouldCalculateStatisticalMeasures() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        // Verify all statistical measures are present
        assertThat(result.getExpectedValue()).isNotNull();
        assertThat(result.getMedianValue()).isNotNull();
        assertThat(result.getBestCase()).isNotNull(); // 95th percentile
        assertThat(result.getWorstCase()).isNotNull(); // 5th percentile
        assertThat(result.getPercentile90High()).isNotNull();
        assertThat(result.getPercentile90Low()).isNotNull();
        assertThat(result.getPercentile50High()).isNotNull(); // 75th percentile
        assertThat(result.getPercentile50Low()).isNotNull();  // 25th percentile
    }

    @Test
    @DisplayName("Should calculate probabilities correctly")
    void shouldCalculateProbabilities() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        // Probabilities should be between 0 and 1
        assertThat(result.getProbabilityOfLoss()).isBetween(0.0, 1.0);
        assertThat(result.getProbabilityOfDoubling()).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Should include historical metrics in results")
    void shouldIncludeHistoricalMetrics() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        // Historical metrics used for simulation
        assertThat(result.getHistoricalReturn()).isNotNull();
        assertThat(result.getHistoricalVolatility()).isNotNull();
    }

    @Test
    @DisplayName("Should limit simulation results for charting")
    void shouldLimitSimulationResultsForCharting() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        // Run with 1000 iterations but expect only 100 results for charting
        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 1000, 30);

        assertThat(result.getSimulationResults()).hasSize(100); // Limited to 100 for charting
    }

    @Test
    @DisplayName("Should have best case greater than worst case")
    void shouldHaveBestCaseGreaterThanWorstCase() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        // 95th percentile should be greater than 5th percentile
        assertThat(result.getBestCase()).isGreaterThan(result.getWorstCase());
    }

    @Test
    @DisplayName("Should have median between best and worst case")
    void shouldHaveMedianBetweenBestAndWorst() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        // Median should be between 5th and 95th percentile
        assertThat(result.getMedianValue())
                .isGreaterThanOrEqualTo(result.getWorstCase())
                .isLessThanOrEqualTo(result.getBestCase());
    }

    @Test
    @DisplayName("Should include calculation timestamp")
    void shouldIncludeCalculationTimestamp() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(metricsService.calculateVolatility(1L)).thenReturn(new BigDecimal("0.15"));
        when(metricsService.calculateTotalReturn(1L)).thenReturn(new BigDecimal("0.08"));

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(1L, 100, 30);

        assertThat(result.getCalculatedAt()).isNotNull();
    }
}