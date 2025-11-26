package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.dto.DTOMapper;
import com.github.hnrdejesus.invest_track.dto.PositionDTO;
import com.github.hnrdejesus.invest_track.service.PositionService;
import com.github.hnrdejesus.invest_track.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Position operations.
 * Handles buy/sell transactions and portfolio composition.
 */
@RestController
@RequestMapping("/api/portfolios/{portfolioId}/positions")
@RequiredArgsConstructor
@Tag(name = "Positions", description = "Portfolio position management endpoints")
public class PositionController {

    private final PositionService positionService;
    private final TransactionService transactionService;
    private final DTOMapper mapper;

    /**
     * Lists all positions in portfolio.
     * GET /api/portfolios/{portfolioId}/positions
     */
    @GetMapping
    @Operation(summary = "List positions", description = "Retrieves all positions in portfolio")
    public ResponseEntity<List<PositionDTO>> getPortfolioPositions(@PathVariable Long portfolioId) {

        List<PositionDTO> positions = positionService.getPortfolioPositions(portfolioId)
                .stream()
                .map(mapper::toPositionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(positions);
    }

    /**
     * Gets specific position by asset.
     * GET /api/portfolios/{portfolioId}/positions/asset/{assetId}
     */
    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get position", description = "Retrieves specific position by asset")
    public ResponseEntity<PositionDTO> getPosition(
            @PathVariable Long portfolioId,
            @PathVariable Long assetId) {

        Position position = positionService.getPosition(portfolioId, assetId);
        return ResponseEntity.ok(mapper.toPositionDTO(position));
    }

    /**
     * Buys asset (creates or increases position).
     * POST /api/portfolios/{portfolioId}/positions/buy
     */
    @PostMapping("/buy")
    @Operation(summary = "Buy asset", description = "Purchases asset and creates/updates position")
    public ResponseEntity<PositionDTO> buyAsset(
            @PathVariable Long portfolioId,
            @Valid @RequestBody PositionDTO.TradeRequest request) {

        Position position = positionService.buyAsset(
                portfolioId,
                request.getAssetId(),
                request.getQuantity(),
                request.getPrice()
        );

        transactionService.recordBuyTransaction(
                portfolioId,
                request.getAssetId(),
                request.getQuantity(),
                request.getPrice(),
                request.getFees()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toPositionDTO(position));
    }

    /**
     * Sells asset (reduces or closes position).
     * POST /api/portfolios/{portfolioId}/positions/sell
     */
    @PostMapping("/sell")
    @Operation(summary = "Sell asset", description = "Sells asset and reduces/closes position")
    public ResponseEntity<PositionDTO> sellAsset(
            @PathVariable Long portfolioId,
            @Valid @RequestBody PositionDTO.TradeRequest request) {

        Position position = positionService.sellAsset(
                portfolioId,
                request.getAssetId(),
                request.getQuantity(),
                request.getPrice()
        );

        transactionService.recordSellTransaction(
                portfolioId,
                request.getAssetId(),
                request.getQuantity(),
                request.getPrice(),
                request.getFees()
        );

        if (position == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(mapper.toPositionDTO(position));
    }

    /**
     * Closes entire position (sell all).
     * DELETE /api/portfolios/{portfolioId}/positions/asset/{assetId}
     */
    @DeleteMapping("/asset/{assetId}")
    @Operation(summary = "Close position", description = "Sells entire position")
    public ResponseEntity<Void> closePosition(
            @PathVariable Long portfolioId,
            @PathVariable Long assetId,
            @RequestParam BigDecimal sellPrice) {

        Position position = positionService.getPosition(portfolioId, assetId);

        positionService.closePosition(portfolioId, assetId, sellPrice);

        transactionService.recordSellTransaction(
                portfolioId,
                assetId,
                position.getQuantity(),
                sellPrice,
                BigDecimal.ZERO
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Calculates total investment in portfolio.
     * GET /api/portfolios/{portfolioId}/positions/total-investment
     */
    @GetMapping("/total-investment")
    @Operation(summary = "Total investment", description = "Calculates total cost basis")
    public ResponseEntity<BigDecimal> getTotalInvestment(@PathVariable Long portfolioId) {
        BigDecimal total = positionService.calculateTotalInvestment(portfolioId);
        return ResponseEntity.ok(total);
    }

    /**
     * Counts positions in portfolio.
     * GET /api/portfolios/{portfolioId}/positions/count
     */
    @GetMapping("/count")
    @Operation(summary = "Count positions", description = "Returns number of positions")
    public ResponseEntity<Long> countPositions(@PathVariable Long portfolioId) {
        long count = positionService.countPositions(portfolioId);
        return ResponseEntity.ok(count);
    }
}