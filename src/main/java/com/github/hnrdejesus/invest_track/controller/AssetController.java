package com.github.hnrdejesus.invest_track.controller;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.dto.AssetDTO;
import com.github.hnrdejesus.invest_track.dto.DTOMapper;
import com.github.hnrdejesus.invest_track.service.AssetService;
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
 * REST Controller for Asset catalog operations.
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Asset catalog management endpoints")
public class AssetController {

    private final AssetService assetService;
    private final DTOMapper mapper;

    /**
     * Creates new asset.
     * POST /api/assets
     */
    @PostMapping
    @Operation(summary = "Create asset", description = "Adds new asset to catalog")
    public ResponseEntity<AssetDTO> createAsset(@Valid @RequestBody AssetDTO.CreateRequest request) {

        Asset asset = assetService.createAsset(
                request.getTicker(),
                request.getName(),
                request.getAssetType(),
                request.getCurrency(),
                request.getExchange()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toAssetDTO(asset));
    }

    /**
     * Lists all assets.
     * GET /api/assets
     */
    @GetMapping
    @Operation(summary = "List all assets", description = "Retrieves complete asset catalog")
    public ResponseEntity<List<AssetDTO>> getAllAssets() {

        List<AssetDTO> assets = assetService.getAllAssets()
                .stream()
                .map(mapper::toAssetDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(assets);
    }

    /**
     * Lists active assets only.
     * GET /api/assets/active
     */
    @GetMapping("/active")
    @Operation(summary = "List active assets", description = "Retrieves only active assets")
    public ResponseEntity<List<AssetDTO>> getActiveAssets() {

        List<AssetDTO> assets = assetService.getActiveAssets()
                .stream()
                .map(mapper::toAssetDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(assets);
    }

    /**
     * Gets asset by ID.
     * GET /api/assets/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get asset", description = "Retrieves asset by ID")
    public ResponseEntity<AssetDTO> getAssetById(@PathVariable Long id) {
        Asset asset = assetService.getAssetById(id);
        return ResponseEntity.ok(mapper.toAssetDTO(asset));
    }

    /**
     * Gets asset by ticker.
     * GET /api/assets/ticker/{ticker}
     */
    @GetMapping("/ticker/{ticker}")
    @Operation(summary = "Get asset by ticker", description = "Retrieves asset by ticker symbol")
    public ResponseEntity<AssetDTO> getAssetByTicker(@PathVariable String ticker) {
        Asset asset = assetService.getAssetByTicker(ticker);
        return ResponseEntity.ok(mapper.toAssetDTO(asset));
    }

    /**
     * Searches assets by name or ticker.
     * GET /api/assets/search?q=AAPL
     */
    @GetMapping("/search")
    @Operation(summary = "Search assets", description = "Searches assets by name or ticker")
    public ResponseEntity<List<AssetDTO>> searchAssets(@RequestParam String q) {

        List<AssetDTO> assets = assetService.searchAssets(q)
                .stream()
                .map(mapper::toAssetDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(assets);
    }

    /**
     * Filters assets by type.
     * GET /api/assets/type/STOCK
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Filter by type", description = "Retrieves assets of specific type")
    public ResponseEntity<List<AssetDTO>> getAssetsByType(@PathVariable AssetType type) {

        List<AssetDTO> assets = assetService.getAssetsByType(type)
                .stream()
                .map(mapper::toAssetDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(assets);
    }

    /**
     * Updates asset information.
     * PUT /api/assets/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update asset", description = "Updates asset name and exchange")
    public ResponseEntity<AssetDTO> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetDTO.UpdateRequest request) {

        Asset asset = assetService.updateAssetInfo(id, request.getName(), request.getExchange());
        return ResponseEntity.ok(mapper.toAssetDTO(asset));
    }

    /**
     * Deactivates asset.
     * POST /api/assets/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate asset", description = "Marks asset as inactive")
    public ResponseEntity<Void> deactivateAsset(@PathVariable Long id) {
        assetService.deactivateAsset(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates asset.
     * POST /api/assets/{id}/reactivate
     */
    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate asset", description = "Marks asset as active")
    public ResponseEntity<Void> reactivateAsset(@PathVariable Long id) {
        assetService.reactivateAsset(id);
        return ResponseEntity.noContent().build();
    }
}