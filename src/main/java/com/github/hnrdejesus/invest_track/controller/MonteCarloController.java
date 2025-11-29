package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.dto.MonteCarloSimulationDTO;
import com.github.hnrdejesus.invest_track.service.MonteCarloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Monte Carlo simulations.
 */
@RestController
@RequestMapping("/api/portfolios/{portfolioId}/monte-carlo")
@RequiredArgsConstructor
@Tag(name = "Monte Carlo Simulation", description = "Probabilistic portfolio projections")
public class MonteCarloController {

    private final MonteCarloService monteCarloService;

    @GetMapping("/simulate")
    @Operation(
            summary = "Run Monte Carlo simulation",
            description = "Simulates thousands of possible future scenarios. Returns percentiles, " +
                    "probabilities, and confidence intervals. Default: 10,000 iterations over 252 days (1 year)."
    )
    public ResponseEntity<MonteCarloSimulationDTO> runSimulation(
            @PathVariable Long portfolioId,
            @RequestParam(required = false) Integer iterations,
            @RequestParam(required = false) Integer days) {

        MonteCarloSimulationDTO result = monteCarloService.runSimulation(portfolioId, iterations, days);
        return ResponseEntity.ok(result);
    }
}