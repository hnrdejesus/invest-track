package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.dto.AssetDTO;
import com.github.hnrdejesus.invest_track.dto.DTOMapper;
import com.github.hnrdejesus.invest_track.dto.PriceResponseDTO;
import com.github.hnrdejesus.invest_track.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST Controller for market data operations.
 * Handles price updates from external APIs.
 */
@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Real-time market data and price updates")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final DTOMapper mapper;

    /**
     * Fetches current price for ticker symbol.
     * GET /api/market-data/price/{ticker}
     */
    @GetMapping("/price/{ticker}")
    @Operation(summary = "Get current price", description = "Fetches real-time price from Alpha Vantage")
    public ResponseEntity<PriceResponseDTO> getCurrentPrice(@PathVariable String ticker) {
        BigDecimal price = marketDataService.fetchAssetPrice(ticker);
        return ResponseEntity.ok(new PriceResponseDTO(ticker, price));
    }

    /**
     * Updates price for specific asset.
     * POST /api/market-data/assets/{assetId}/update-price
     */
    @PostMapping("/assets/{assetId}/update-price")
    @Operation(summary = "Update asset price", description = "Fetches and saves latest price for asset")
    public ResponseEntity<AssetDTO> updateAssetPrice(@PathVariable Long assetId) {
        Asset asset = marketDataService.updateAssetPrice(assetId);
        return ResponseEntity.ok(mapper.toAssetDTO(asset));
    }

    /**
     * Triggers batch update for all active assets.
     * POST /api/market-data/update-all
     *
     * WARNING: This respects API rate limits (5 calls/min).
     * Will take time for many assets.
     */
    @PostMapping("/update-all")
    @Operation(summary = "Update all prices",
            description = "Updates prices for all active assets (respects rate limits)")
    public ResponseEntity<Map<String, String>> updateAllPrices() {
        // Run async to avoid timeout
        new Thread(() -> marketDataService.updateAllAssetPrices()).start();

        return ResponseEntity.accepted()
                .body(Map.of("status", "Price update started in background"));
    }

    /**
     * Checks if market data API is available.
     * GET /api/market-data/health
     */
    @GetMapping("/health")
    @Operation(summary = "API health check", description = "Verifies market data API availability")
    public ResponseEntity<Map<String, Object>> checkApiHealth() {
        boolean available = marketDataService.isApiAvailable();

        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "Market data API is operational" : "Market data API is unavailable"
        ));
    }
}