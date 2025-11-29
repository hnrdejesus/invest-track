package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.dto.BacktestResultDTO;
import com.github.hnrdejesus.invest_track.dto.BacktestStrategyDTO;
import com.github.hnrdejesus.invest_track.service.BacktestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST endpoints for backtesting trading strategies.
 */
@RestController
@RequestMapping("/api/portfolios/{portfolioId}/backtest")
@RequiredArgsConstructor
@Tag(name = "Backtesting", description = "Test trading strategies on historical data")
public class BacktestController {

    private final BacktestService backtestService;

    @PostMapping
    @Operation(
            summary = "Run backtest for a strategy",
            description = "Simulates a trading strategy over historical period. " +
                    "Compares strategy performance vs buy-and-hold. " +
                    "Default: 252 days (1 trading year)."
    )
    public ResponseEntity<BacktestResultDTO> runBacktest(
            @PathVariable Long portfolioId,
            @Valid @RequestBody BacktestStrategyDTO strategy,
            @RequestParam(required = false) Integer days) {

        BacktestResultDTO result = backtestService.runBacktest(portfolioId, strategy, days);
        return ResponseEntity.ok(result);
    }
}