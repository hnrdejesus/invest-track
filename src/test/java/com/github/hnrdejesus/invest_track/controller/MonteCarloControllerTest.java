package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.dto.MonteCarloSimulationDTO;
import com.github.hnrdejesus.invest_track.service.MonteCarloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for MonteCarloController.
 * Tests Monte Carlo simulation endpoints.
 */
@WebMvcTest(MonteCarloController.class)
@DisplayName("MonteCarloController Tests")
class MonteCarloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MonteCarloService monteCarloService;

    private MonteCarloSimulationDTO simulationResult;

    @BeforeEach
    void setUp() {
        simulationResult = MonteCarloSimulationDTO.builder()
                .portfolioId(1L)
                .iterations(1000)
                .daysProjected(30)
                .initialValue(new BigDecimal("10000.00"))
                .expectedValue(new BigDecimal("10500.00"))
                .medianValue(new BigDecimal("10450.00"))
                .bestCase(new BigDecimal("12000.00"))  // 95th percentile
                .worstCase(new BigDecimal("9000.00"))  // 5th percentile
                .percentile90High(new BigDecimal("11500.00"))
                .percentile90Low(new BigDecimal("9500.00"))
                .percentile50High(new BigDecimal("10800.00"))  // 75th percentile
                .percentile50Low(new BigDecimal("10200.00"))   // 25th percentile
                .probabilityOfLoss(0.20)  // 20% chance of loss
                .probabilityOfDoubling(0.05)  // 5% chance of doubling
                .historicalReturn(0.0003)  // Daily return
                .historicalVolatility(0.015)  // Daily volatility
                .simulationResults(Arrays.asList(
                        new BigDecimal("10100.00"),
                        new BigDecimal("10200.00"),
                        new BigDecimal("10300.00")
                ))
                .calculatedAt(LocalDateTime.now().toString())
                .build();
    }

    @Test
    @DisplayName("GET /api/portfolios/{id}/monte-carlo/simulate - Should run simulation successfully")
    void shouldRunSimulationSuccessfully() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), anyInt(), anyInt()))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId", is(1)))
                .andExpect(jsonPath("$.iterations", is(1000)))
                .andExpect(jsonPath("$.daysProjected", is(30)))
                .andExpect(jsonPath("$.initialValue", is(10000.00)))
                .andExpect(jsonPath("$.expectedValue", is(10500.00)));
    }

    @Test
    @DisplayName("Should return statistical percentiles")
    void shouldReturnStatisticalPercentiles() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), anyInt(), anyInt()))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestCase", is(12000.00)))  // 95th percentile
                .andExpect(jsonPath("$.worstCase", is(9000.00)))  // 5th percentile
                .andExpect(jsonPath("$.medianValue", is(10450.00)))
                .andExpect(jsonPath("$.percentile90High", is(11500.00)))
                .andExpect(jsonPath("$.percentile90Low", is(9500.00)))
                .andExpect(jsonPath("$.percentile50High", is(10800.00)))
                .andExpect(jsonPath("$.percentile50Low", is(10200.00)));
    }

    @Test
    @DisplayName("Should return probability calculations")
    void shouldReturnProbabilityCalculations() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), anyInt(), anyInt()))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.probabilityOfLoss", is(0.20)))
                .andExpect(jsonPath("$.probabilityOfDoubling", is(0.05)));
    }

    @Test
    @DisplayName("Should return historical metrics used for simulation")
    void shouldReturnHistoricalMetrics() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), anyInt(), anyInt()))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historicalReturn", is(0.0003)))
                .andExpect(jsonPath("$.historicalVolatility", is(0.015)));
    }

    @Test
    @DisplayName("Should return simulation results for charting")
    void shouldReturnSimulationResultsForCharting() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), anyInt(), anyInt()))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulationResults", hasSize(3)))
                .andExpect(jsonPath("$.simulationResults[0]", is(10100.00)));
    }

    @Test
    @DisplayName("Should use default parameters when not provided")
    void shouldUseDefaultParametersWhenNotProvided() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), eq(null), eq(null)))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId", is(1)));
    }

    @Test
    @DisplayName("Should include calculation timestamp")
    void shouldIncludeCalculationTimestamp() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), anyInt(), anyInt()))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatedAt").exists())
                .andExpect(jsonPath("$.calculatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should handle portfolio not found")
    void shouldHandlePortfolioNotFound() throws Exception {
        when(monteCarloService.runSimulation(eq(999L), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Portfolio not found"));

        mockMvc.perform(get("/api/portfolios/999/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "30"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should handle portfolio with no positions")
    void shouldHandlePortfolioWithNoPositions() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Portfolio has no positions to simulate"));

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should accept custom iteration count")
    void shouldAcceptCustomIterationCount() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), eq(5000), anyInt()))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "5000")
                        .param("days", "30"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept custom projection days")
    void shouldAcceptCustomProjectionDays() throws Exception {
        when(monteCarloService.runSimulation(eq(1L), anyInt(), eq(60)))
                .thenReturn(simulationResult);

        mockMvc.perform(get("/api/portfolios/1/monte-carlo/simulate")
                        .param("iterations", "1000")
                        .param("days", "60"))
                .andExpect(status().isOk());
    }
}