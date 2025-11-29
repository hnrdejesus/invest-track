package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.dto.PortfolioMetricsDTO;
import com.github.hnrdejesus.invest_track.service.PortfolioMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * REST endpoints for portfolio financial metrics and performance analysis.
 */
@RestController
@RequestMapping("/api/portfolios/{portfolioId}/metrics")
@RequiredArgsConstructor
@Tag(name = "Portfolio Metrics", description = "Financial metrics and performance analysis")
public class PortfolioMetricsController {

    private final PortfolioMetricsService metricsService;

    @GetMapping
    @Operation(summary = "Get all portfolio metrics",
            description = "Returns comprehensive financial metrics including Sharpe ratio, volatility, and returns")
    public ResponseEntity<PortfolioMetricsDTO> getPortfolioMetrics(@PathVariable Long portfolioId) {
        PortfolioMetricsDTO metrics = metricsService.calculateMetrics(portfolioId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/sharpe-ratio")
    @Operation(summary = "Get Sharpe ratio",
            description = "Risk-adjusted return metric. Values > 1.0 are good, > 2.0 are excellent")
    public ResponseEntity<BigDecimal> getSharpeRatio(
            @PathVariable Long portfolioId,
            @RequestParam(defaultValue = "0.02") BigDecimal riskFreeRate) {
        BigDecimal sharpe = metricsService.calculateSharpeRatio(portfolioId, riskFreeRate);
        return ResponseEntity.ok(sharpe);
    }

    @GetMapping("/volatility")
    @Operation(summary = "Get portfolio volatility",
            description = "Standard deviation of returns. Higher values indicate higher risk")
    public ResponseEntity<BigDecimal> getVolatility(@PathVariable Long portfolioId) {
        BigDecimal volatility = metricsService.calculateVolatility(portfolioId);
        return ResponseEntity.ok(volatility);
    }

    @GetMapping("/max-drawdown")
    @Operation(summary = "Get maximum drawdown",
            description = "Largest peak-to-trough decline. Example: -0.25 means 25% loss from peak")
    public ResponseEntity<BigDecimal> getMaxDrawdown(@PathVariable Long portfolioId) {
        BigDecimal maxDrawdown = metricsService.calculateMaxDrawdown(portfolioId);
        return ResponseEntity.ok(maxDrawdown);
    }

    @GetMapping("/total-return")
    @Operation(summary = "Get total return",
            description = "Overall portfolio return percentage")
    public ResponseEntity<BigDecimal> getTotalReturn(@PathVariable Long portfolioId) {
        BigDecimal totalReturn = metricsService.calculateTotalReturn(portfolioId);
        return ResponseEntity.ok(totalReturn);
    }
}