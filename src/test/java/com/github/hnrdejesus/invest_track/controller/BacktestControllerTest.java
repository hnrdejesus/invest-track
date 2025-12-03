package com.github.hnrdejesus.invest_track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hnrdejesus.invest_track.dto.BacktestResultDTO;
import com.github.hnrdejesus.invest_track.dto.BacktestStrategyDTO;
import com.github.hnrdejesus.invest_track.service.BacktestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for BacktestController.
 * Tests strategy backtesting endpoints.
 */
@WebMvcTest(BacktestController.class)
@DisplayName("BacktestController Tests")
class BacktestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BacktestService backtestService;

    private BacktestStrategyDTO testStrategy;
    private BacktestResultDTO backtestResult;

    @BeforeEach
    void setUp() {
        testStrategy = new BacktestStrategyDTO();
        testStrategy.setStrategyName("Momentum Strategy");
        testStrategy.setInitialCapital(new BigDecimal("10000.00"));
        testStrategy.setBuyThreshold(new BigDecimal("-0.05"));  // Buy when -5%
        testStrategy.setSellThreshold(new BigDecimal("0.10"));  // Sell when +10%
        testStrategy.setStopLoss(new BigDecimal("-0.15"));  // Stop loss at -15%
        testStrategy.setTakeProfit(new BigDecimal("0.25"));  // Take profit at +25%
        testStrategy.setMaxPositionSize(new BigDecimal("0.30"));  // 30% max per position
        testStrategy.setRebalanceDays(7);  // Rebalance weekly

        backtestResult = BacktestResultDTO.builder()
                .strategyName("Momentum Strategy")
                .portfolioId(1L)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .totalDays(30)
                .initialCapital(new BigDecimal("10000.00"))
                .finalCapital(new BigDecimal("11500.00"))
                .totalReturn(new BigDecimal("1500.00"))
                .totalReturnPercentage(new BigDecimal("0.15"))  // 15% return
                .sharpeRatio(new BigDecimal("1.25"))
                .maxDrawdown(new BigDecimal("-0.08"))  // -8% max drawdown
                .volatility(new BigDecimal("0.12"))  // 12% volatility
                .totalTrades(20)
                .winningTrades(12)
                .losingTrades(8)
                .winRate(new BigDecimal("0.60"))  // 60% win rate
                .avgWin(new BigDecimal("150.00"))
                .avgLoss(new BigDecimal("75.00"))
                .profitFactor(new BigDecimal("2.40"))  // Wins/Losses ratio
                .buyAndHoldReturn(new BigDecimal("0.10"))  // 10% buy-and-hold
                .strategyVsBuyAndHold(new BigDecimal("0.05"))  // 5% outperformance
                .portfolioHistory(Arrays.asList(
                        BacktestResultDTO.DailyValue.builder()
                                .date(LocalDate.now().minusDays(30))
                                .value(new BigDecimal("10000.00"))
                                .build(),
                        BacktestResultDTO.DailyValue.builder()
                                .date(LocalDate.now())
                                .value(new BigDecimal("11500.00"))
                                .build()
                ))
                .calculatedAt(LocalDateTime.now().toString())
                .build();
    }

    @Test
    @DisplayName("POST /api/portfolios/{id}/backtest - Should run backtest successfully")
    void shouldRunBacktestSuccessfully() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategyName", is("Momentum Strategy")))
                .andExpect(jsonPath("$.portfolioId", is(1)))
                .andExpect(jsonPath("$.totalDays", is(30)));
    }

    @Test
    @DisplayName("Should return financial metrics")
    void shouldReturnFinancialMetrics() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialCapital", is(10000.00)))
                .andExpect(jsonPath("$.finalCapital", is(11500.00)))
                .andExpect(jsonPath("$.totalReturn", is(1500.00)))
                .andExpect(jsonPath("$.totalReturnPercentage", is(0.15)));
    }

    @Test
    @DisplayName("Should return risk metrics")
    void shouldReturnRiskMetrics() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sharpeRatio", is(1.25)))
                .andExpect(jsonPath("$.maxDrawdown", is(-0.08)))
                .andExpect(jsonPath("$.volatility", is(0.12)));
    }

    @Test
    @DisplayName("Should return trade statistics")
    void shouldReturnTradeStatistics() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrades", is(20)))
                .andExpect(jsonPath("$.winningTrades", is(12)))
                .andExpect(jsonPath("$.losingTrades", is(8)))
                .andExpect(jsonPath("$.winRate", is(0.60)))
                .andExpect(jsonPath("$.avgWin", is(150.00)))
                .andExpect(jsonPath("$.avgLoss", is(75.00)))
                .andExpect(jsonPath("$.profitFactor", is(2.40)));
    }

    @Test
    @DisplayName("Should return buy-and-hold comparison")
    void shouldReturnBuyAndHoldComparison() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyAndHoldReturn", is(0.10)))
                .andExpect(jsonPath("$.strategyVsBuyAndHold", is(0.05)));  // 5% outperformance
    }

    @Test
    @DisplayName("Should return portfolio history")
    void shouldReturnPortfolioHistory() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioHistory", hasSize(2)))
                .andExpect(jsonPath("$.portfolioHistory[0].value", is(10000.00)))
                .andExpect(jsonPath("$.portfolioHistory[1].value", is(11500.00)));
    }

    @Test
    @DisplayName("Should use default days when not provided")
    void shouldUseDefaultDaysWhenNotProvided() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), eq(null)))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should validate strategy parameters - missing required fields")
    void shouldValidateStrategyParameters() throws Exception {
        BacktestStrategyDTO invalidStrategy = new BacktestStrategyDTO();
        // Missing all required fields

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStrategy))
                        .param("days", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should validate strategy name length")
    void shouldValidateStrategyNameLength() throws Exception {
        testStrategy.setStrategyName("AB");  // Too short (min 3)

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'strategyName')].message",
                        hasItem(containsString("between 3 and 100"))));
    }

    @Test
    @DisplayName("Should validate initial capital is positive")
    void shouldValidateInitialCapitalIsPositive() throws Exception {
        testStrategy.setInitialCapital(BigDecimal.ZERO);  // Must be > 0

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'initialCapital')].message",
                        hasItem(containsString("greater than zero"))));
    }

    @Test
    @DisplayName("Should validate buy threshold is negative")
    void shouldValidateBuyThresholdIsNegative() throws Exception {
        testStrategy.setBuyThreshold(new BigDecimal("0.10"));  // Must be <= 0

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'buyThreshold')].message",
                        hasItem(containsString("less than or equal to 0%"))));
    }

    @Test
    @DisplayName("Should validate max position size range")
    void shouldValidateMaxPositionSizeRange() throws Exception {
        testStrategy.setMaxPositionSize(new BigDecimal("1.5"));  // Must be <= 1.0 (100%)

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'maxPositionSize')].message",
                        hasItem(containsString("cannot exceed 100%"))));
    }

    @Test
    @DisplayName("Should handle portfolio not found")
    void shouldHandlePortfolioNotFound() throws Exception {
        when(backtestService.runBacktest(eq(999L), any(BacktestStrategyDTO.class), anyInt()))
                .thenThrow(new RuntimeException("Portfolio not found"));

        mockMvc.perform(post("/api/portfolios/999/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should handle portfolio with no positions")
    void shouldHandlePortfolioWithNoPositions() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenThrow(new IllegalArgumentException("Portfolio has no positions to backtest"));

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should include date range in results")
    void shouldIncludeDateRangeInResults() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists());
    }

    @Test
    @DisplayName("Should include calculation timestamp")
    void shouldIncludeCalculationTimestamp() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), anyInt()))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatedAt").exists())
                .andExpect(jsonPath("$.calculatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should accept custom backtest period")
    void shouldAcceptCustomBacktestPeriod() throws Exception {
        when(backtestService.runBacktest(eq(1L), any(BacktestStrategyDTO.class), eq(60)))
                .thenReturn(backtestResult);

        mockMvc.perform(post("/api/portfolios/1/backtest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStrategy))
                        .param("days", "60"))
                .andExpect(status().isOk());
    }
}