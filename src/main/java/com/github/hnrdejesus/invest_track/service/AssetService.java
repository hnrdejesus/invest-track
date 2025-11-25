package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for Asset catalog management and price updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;

    /**
     * Creates new asset in catalog.
     * Validates unique ticker symbol.
     */
    @Transactional
    public Asset createAsset(String ticker, String name, AssetType type, String currency, String exchange) {

        log.info("Creating asset: {} ({})", ticker, type);

        if (assetRepository.findByTickerIgnoreCase(ticker).isPresent()) {
            throw new IllegalArgumentException("Asset with ticker '" + ticker + "' already exists");
        }

        Asset asset = new Asset();
        asset.setTicker(ticker.toUpperCase());
        asset.setName(name);
        asset.setAssetType(type);
        asset.setCurrency(currency);
        asset.setExchange(exchange);
        asset.setActive(true);

        Asset saved = assetRepository.save(asset);
        log.info("Asset created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Retrieves asset by ID.
     */
    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ID: " + id));
    }

    /**
     * Finds asset by ticker symbol.
     */
    public Asset getAssetByTicker(String ticker) {
        return assetRepository.findByTickerIgnoreCase(ticker)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with ticker: " + ticker));
    }

    /**
     * Lists all assets.
     */
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    /**
     * Lists active assets only.
     */
    public List<Asset> getActiveAssets() {
        return assetRepository.findByActiveTrue();
    }

    /**
     * Finds assets by type.
     */
    public List<Asset> getAssetsByType(AssetType type) {
        return assetRepository.findByAssetTypeAndActiveTrue(type);
    }

    /**
     * Searches assets by name or ticker.
     * Used for autocomplete in UI.
     */
    public List<Asset> searchAssets(String searchTerm) {
        return assetRepository.searchByNameOrTicker(searchTerm);
    }

    /**
     * Updates asset price.
     * Called by market data service when fetching latest prices.
     */
    @Transactional
    public Asset updatePrice(Long assetId, BigDecimal newPrice) {

        log.debug("Updating price for asset ID {}: {}", assetId, newPrice);

        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        Asset asset = getAssetById(assetId);
        asset.updatePrice(newPrice);

        return assetRepository.save(asset);
    }

    /**
     * Updates asset basic information.
     * Does not modify price or active status.
     */
    @Transactional
    public Asset updateAssetInfo(Long id, String name, String exchange) {

        log.info("Updating asset ID: {}", id);

        Asset asset = getAssetById(id);
        asset.setName(name);
        asset.setExchange(exchange);

        return assetRepository.save(asset);
    }

    /**
     * Deactivates asset (soft delete).
     * Prevents use in new positions while preserving historical data.
     */
    @Transactional
    public void deactivateAsset(Long id) {

        log.info("Deactivating asset ID: {}", id);

        Asset asset = getAssetById(id);
        asset.setActive(false);
        assetRepository.save(asset);
    }

    /**
     * Reactivates previously deactivated asset.
     */
    @Transactional
    public void reactivateAsset(Long id) {

        log.info("Reactivating asset ID: {}", id);

        Asset asset = getAssetById(id);
        asset.setActive(true);
        assetRepository.save(asset);
    }

    /**
     * Bulk deactivate multiple assets.
     * Useful for removing delisted securities.
     */
    @Transactional
    public void bulkDeactivate(List<Long> assetIds) {

        log.info("Bulk deactivating {} assets", assetIds.size());
        assetRepository.updateActiveStatus(assetIds, false);
    }

    /**
     * Finds assets needing price updates.
     * Returns active assets without current price.
     */
    public List<Asset> getAssetsNeedingPriceUpdate() {
        return assetRepository.findAssetsWithoutPrice();
    }

    /**
     * Counts assets by type.
     * Dashboard statistics.
     */
    public long countAssetsByType(AssetType type) {
        return assetRepository.countByAssetType(type);
    }
}