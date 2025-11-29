package com.github.hnrdejesus.invest_track.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hnrdejesus.invest_track.domain.Asset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Service for fetching market data from external APIs.
 * Integrates with Alpha Vantage for real-time stock prices.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final WebClient.Builder webClientBuilder;
    private final AssetService assetService;
    private final ObjectMapper objectMapper;

    @Value("${external-api.alpha-vantage.base-url}")
    private String alphaVantageBaseUrl;

    @Value("${external-api.alpha-vantage.api-key}")
    private String alphaVantageApiKey;

    @Value("${external-api.alpha-vantage.timeout}")
    private int timeout;

    /**
     * Fetches current price for a single asset from Alpha Vantage.
     * API: GLOBAL_QUOTE endpoint
     *
     * Example response:
     * {
     *   "Global Quote": {
     *     "01. symbol": "AAPL",
     *     "05. price": "150.50",
     *     ...
     *   }
     * }
     */
    public BigDecimal fetchAssetPrice(String ticker) {
        log.info("Fetching price for ticker: {}", ticker);

        try {
            WebClient webClient = webClientBuilder.baseUrl(alphaVantageBaseUrl).build();

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", ticker)
                            .queryParam("apikey", alphaVantageApiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            return parsePriceFromResponse(response);

        } catch (Exception e) {
            log.error("Failed to fetch price for ticker: {}", ticker, e);
            throw new RuntimeException("Failed to fetch market data for " + ticker, e);
        }
    }

    /**
     * Updates price for a single asset and persists to database.
     */
    public Asset updateAssetPrice(Long assetId) {
        Asset asset = assetService.getAssetById(assetId);
        BigDecimal newPrice = fetchAssetPrice(asset.getTicker());
        return assetService.updatePrice(assetId, newPrice);
    }

    /**
     * Updates prices for all active assets.
     * Use with caution: Alpha Vantage has rate limits (5 calls/min for free tier).
     */
    public void updateAllAssetPrices() {
        log.info("Starting batch price update for all active assets");

        List<Asset> activeAssets = assetService.getActiveAssets();
        int successCount = 0;
        int failCount = 0;

        for (Asset asset : activeAssets) {
            try {
                BigDecimal newPrice = fetchAssetPrice(asset.getTicker());
                assetService.updatePrice(asset.getId(), newPrice);
                successCount++;

                // Rate limiting: wait 12 seconds between calls (5 calls/min)
                Thread.sleep(12000);

            } catch (Exception e) {
                log.error("Failed to update price for asset: {}", asset.getTicker(), e);
                failCount++;
            }
        }

        log.info("Batch update completed. Success: {}, Failed: {}", successCount, failCount);
    }

    /**
     * Parses price from Alpha Vantage JSON response.
     */
    private BigDecimal parsePriceFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode globalQuote = root.get("Global Quote");

            if (globalQuote == null || globalQuote.isEmpty()) {
                log.warn("Empty response from Alpha Vantage. API limit reached or invalid ticker?");
                throw new RuntimeException("No data returned from market data API");
            }

            String priceStr = globalQuote.get("05. price").asText();
            return new BigDecimal(priceStr);

        } catch (Exception e) {
            log.error("Failed to parse price from response: {}", jsonResponse, e);
            throw new RuntimeException("Failed to parse market data response", e);
        }
    }

    /**
     * Checks if API is available and responding.
     * Useful for health checks.
     */
    public boolean isApiAvailable() {
        try {
            fetchAssetPrice("AAPL"); // Test with Apple stock
            return true;
        } catch (Exception e) {
            log.error("Market data API is unavailable", e);
            return false;
        }
    }
}