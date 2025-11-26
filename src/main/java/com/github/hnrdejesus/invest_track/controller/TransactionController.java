package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.domain.Transaction;
import com.github.hnrdejesus.invest_track.domain.TransactionType;
import com.github.hnrdejesus.invest_track.dto.DTOMapper;
import com.github.hnrdejesus.invest_track.dto.TransactionDTO;
import com.github.hnrdejesus.invest_track.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Transaction history and reporting.
 * Read-only operations - transactions are created by other controllers.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction history and reporting endpoints")
public class TransactionController {

    private final TransactionService transactionService;
    private final DTOMapper mapper;

    /**
     * Gets paginated transaction history for portfolio.
     * GET /api/portfolios/{portfolioId}/transactions?page=0&size=20
     */
    @GetMapping("/portfolios/{portfolioId}/transactions")
    @Operation(summary = "Portfolio transactions", description = "Retrieves paginated transaction history")
    public ResponseEntity<Page<TransactionDTO>> getPortfolioTransactions(
            @PathVariable Long portfolioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getPortfolioTransactions(portfolioId, pageable);

        Page<TransactionDTO> transactionDTOs = transactions.map(mapper::toTransactionDTO);

        return ResponseEntity.ok(transactionDTOs);
    }

    /**
     * Gets transaction history for specific asset in portfolio.
     * GET /api/portfolios/{portfolioId}/assets/{assetId}/transactions
     */
    @GetMapping("/portfolios/{portfolioId}/assets/{assetId}/transactions")
    @Operation(summary = "Asset transactions", description = "Retrieves all transactions for specific asset")
    public ResponseEntity<List<TransactionDTO>> getAssetTransactions(
            @PathVariable Long portfolioId,
            @PathVariable Long assetId) {

        List<TransactionDTO> transactions = transactionService
                .getAssetTransactionHistory(portfolioId, assetId)
                .stream()
                .map(mapper::toTransactionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactions);
    }

    /**
     * Gets transactions within date range.
     * GET /api/portfolios/{portfolioId}/transactions/range?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59
     */
    @GetMapping("/portfolios/{portfolioId}/transactions/range")
    @Operation(summary = "Transactions by date range", description = "Retrieves transactions within date range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @PathVariable Long portfolioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<TransactionDTO> transactions = transactionService
                .getTransactionsByDateRange(portfolioId, start, end)
                .stream()
                .map(mapper::toTransactionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactions);
    }

    /**
     * Gets recent transactions across all portfolios.
     * GET /api/transactions/recent
     */
    @GetMapping("/transactions/recent")
    @Operation(summary = "Recent transactions", description = "Retrieves 10 most recent transactions")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactions() {

        List<TransactionDTO> transactions = transactionService.getRecentTransactions()
                .stream()
                .map(mapper::toTransactionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactions);
    }

    /**
     * Gets transaction by ID.
     * GET /api/transactions/{id}
     */
    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get transaction", description = "Retrieves transaction by ID")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(mapper.toTransactionDTO(transaction));
    }

    /**
     * Calculates total transaction volume by types.
     * GET /api/portfolios/{portfolioId}/transactions/volume?types=BUY,SELL
     */
    @GetMapping("/portfolios/{portfolioId}/transactions/volume")
    @Operation(summary = "Transaction volume", description = "Calculates total volume by transaction types")
    public ResponseEntity<BigDecimal> calculateTotalVolume(
            @PathVariable Long portfolioId,
            @RequestParam List<TransactionType> types) {

        BigDecimal volume = transactionService.calculateTotalVolume(portfolioId, types);
        return ResponseEntity.ok(volume);
    }

    /**
     * Calculates total fees paid.
     * GET /api/portfolios/{portfolioId}/transactions/fees
     */
    @GetMapping("/portfolios/{portfolioId}/transactions/fees")
    @Operation(summary = "Total fees", description = "Calculates total fees paid in portfolio")
    public ResponseEntity<BigDecimal> getTotalFees(@PathVariable Long portfolioId) {
        BigDecimal fees = transactionService.calculateTotalFees(portfolioId);
        return ResponseEntity.ok(fees);
    }

    /**
     * Counts transactions by type.
     * GET /api/portfolios/{portfolioId}/transactions/count?type=BUY
     */
    @GetMapping("/portfolios/{portfolioId}/transactions/count")
    @Operation(summary = "Count by type", description = "Counts transactions by type")
    public ResponseEntity<Long> countTransactionsByType(
            @PathVariable Long portfolioId,
            @RequestParam TransactionType type) {

        long count = transactionService.countTransactionsByType(portfolioId, type);
        return ResponseEntity.ok(count);
    }
}