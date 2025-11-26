package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.dto.DTOMapper;
import com.github.hnrdejesus.invest_track.dto.PortfolioDTO;
import com.github.hnrdejesus.invest_track.service.PortfolioService;
import com.github.hnrdejesus.invest_track.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Portfolio operations.
 * Handles CRUD and cash management endpoints.
 */
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Tag(name = "Portfolios", description = "Portfolio management endpoints")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final TransactionService transactionService;
    private final DTOMapper mapper;

    /**
     * Creates new portfolio.
     * POST /api/portfolios
     */
    @PostMapping
    @Operation(summary = "Create portfolio", description = "Creates new investment portfolio")
    public ResponseEntity<PortfolioDTO> createPortfolio(
            @Valid @RequestBody PortfolioDTO.CreateRequest request) {

        Portfolio portfolio = portfolioService.createPortfolio(
                request.getName(),
                request.getDescription(),
                request.getInitialCash()
        );

        if (request.getInitialCash().compareTo(java.math.BigDecimal.ZERO) > 0) {
            transactionService.recordDepositTransaction(
                    portfolio.getId(),
                    request.getInitialCash(),
                    "Initial deposit"
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toPortfolioDTO(portfolio));
    }

    /**
     * Lists all portfolios.
     * GET /api/portfolios
     */
    @GetMapping
    @Operation(summary = "List portfolios", description = "Retrieves all portfolios ordered by creation date")
    public ResponseEntity<List<PortfolioDTO>> getAllPortfolios() {

        List<PortfolioDTO> portfolios = portfolioService.getAllPortfolios()
                .stream()
                .map(mapper::toPortfolioDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(portfolios);
    }

    /**
     * Gets portfolio by ID.
     * GET /api/portfolios/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get portfolio", description = "Retrieves portfolio by ID")
    public ResponseEntity<PortfolioDTO> getPortfolioById(@PathVariable Long id) {
        Portfolio portfolio = portfolioService.getPortfolioById(id);
        return ResponseEntity.ok(mapper.toPortfolioDTO(portfolio));
    }

    /**
     * Updates portfolio information.
     * PUT /api/portfolios/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update portfolio", description = "Updates portfolio name and description")
    public ResponseEntity<PortfolioDTO> updatePortfolio(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioDTO.UpdateRequest request) {

        Portfolio portfolio = portfolioService.updatePortfolio(
                id,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity.ok(mapper.toPortfolioDTO(portfolio));
    }

    /**
     * Deposits cash into portfolio.
     * POST /api/portfolios/{id}/deposit
     */
    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit cash", description = "Adds cash to portfolio")
    public ResponseEntity<PortfolioDTO> depositCash(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioDTO.CashOperationRequest request) {

        Portfolio portfolio = portfolioService.depositCash(id, request.getAmount());

        transactionService.recordDepositTransaction(
                id,
                request.getAmount(),
                request.getNotes()
        );

        return ResponseEntity.ok(mapper.toPortfolioDTO(portfolio));
    }

    /**
     * Withdraws cash from portfolio.
     * POST /api/portfolios/{id}/withdraw
     */
    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw cash", description = "Removes cash from portfolio")
    public ResponseEntity<PortfolioDTO> withdrawCash(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioDTO.CashOperationRequest request) {

        Portfolio portfolio = portfolioService.withdrawCash(id, request.getAmount());

        transactionService.recordWithdrawalTransaction(
                id,
                request.getAmount(),
                request.getNotes()
        );

        return ResponseEntity.ok(mapper.toPortfolioDTO(portfolio));
    }

    /**
     * Recalculates portfolio value.
     * POST /api/portfolios/{id}/recalculate
     */
    @PostMapping("/{id}/recalculate")
    @Operation(summary = "Recalculate value", description = "Recalculates portfolio total value")
    public ResponseEntity<PortfolioDTO> recalculateValue(@PathVariable Long id) {
        Portfolio portfolio = portfolioService.recalculatePortfolioValue(id);
        return ResponseEntity.ok(mapper.toPortfolioDTO(portfolio));
    }

    /**
     * Deletes portfolio.
     * DELETE /api/portfolios/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete portfolio", description = "Deletes portfolio and all associated data")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }
}