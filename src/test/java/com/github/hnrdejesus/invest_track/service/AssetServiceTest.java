package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.exception.ResourceNotFoundException;
import com.github.hnrdejesus.invest_track.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AssetService.
 * Tests asset management and validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssetService Tests")
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setTicker("AAPL");
        testAsset.setName("Apple Inc.");
        testAsset.setAssetType(AssetType.STOCK);
        testAsset.setCurrency("USD");
        testAsset.setExchange("NASDAQ");
        testAsset.setCurrentPrice(new BigDecimal("150.50"));
        testAsset.setActive(true);
    }

    @Test
    @DisplayName("Should create asset successfully")
    void shouldCreateAsset() {
        String ticker = "GOOGL";
        String name = "Alphabet Inc.";
        AssetType type = AssetType.STOCK;

        when(assetRepository.findByTickerIgnoreCase(ticker)).thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        Asset created = assetService.createAsset(ticker, name, type, "USD", "NASDAQ");

        assertThat(created).isNotNull();
        verify(assetRepository).findByTickerIgnoreCase(ticker);
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    @DisplayName("Should throw exception when creating asset with duplicate ticker")
    void shouldThrowExceptionWhenCreatingDuplicateAsset() {
        String ticker = "AAPL";
        when(assetRepository.findByTickerIgnoreCase(ticker)).thenReturn(Optional.of(testAsset));

        assertThatThrownBy(() -> assetService.createAsset(ticker, "Apple", AssetType.STOCK, "USD", "NASDAQ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get asset by ID")
    void shouldGetAssetById() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));

        Asset found = assetService.getAssetById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getTicker()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset not found")
    void shouldThrowExceptionWhenAssetNotFound() {
        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.getAssetById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Asset")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should get asset by ticker")
    void shouldGetAssetByTicker() {
        when(assetRepository.findByTickerIgnoreCase("AAPL")).thenReturn(Optional.of(testAsset));

        Asset found = assetService.getAssetByTicker("AAPL");

        assertThat(found).isNotNull();
        assertThat(found.getTicker()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should get all active assets")
    void shouldGetAllActiveAssets() {
        Asset asset2 = new Asset();
        asset2.setId(2L);
        asset2.setTicker("MSFT");
        asset2.setActive(true);

        when(assetRepository.findByActiveTrue()).thenReturn(Arrays.asList(testAsset, asset2));

        List<Asset> assets = assetService.getActiveAssets();

        assertThat(assets).hasSize(2);
        assertThat(assets).allMatch(Asset::getActive);
    }

    @Test
    @DisplayName("Should get assets by type")
    void shouldGetAssetsByType() {
        Asset asset2 = new Asset();
        asset2.setId(2L);
        asset2.setTicker("MSFT");
        asset2.setAssetType(AssetType.STOCK);

        when(assetRepository.findByAssetTypeAndActiveTrue(AssetType.STOCK))
                .thenReturn(Arrays.asList(testAsset, asset2));

        List<Asset> stocks = assetService.getAssetsByType(AssetType.STOCK);

        assertThat(stocks).hasSize(2);
        assertThat(stocks).allMatch(a -> a.getAssetType() == AssetType.STOCK);
    }

    @Test
    @DisplayName("Should search assets by name or ticker")
    void shouldSearchAssets() {
        when(assetRepository.searchByNameOrTicker("app"))
                .thenReturn(Arrays.asList(testAsset));

        List<Asset> results = assetService.searchAssets("app");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTicker()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should update asset price")
    void shouldUpdateAssetPrice() {
        BigDecimal newPrice = new BigDecimal("160.00");
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        Asset updated = assetService.updatePrice(1L, newPrice);

        assertThat(updated).isNotNull();
        verify(assetRepository).save(testAsset);
    }

    @Test
    @DisplayName("Should throw exception when updating with negative price")
    void shouldThrowExceptionWhenUpdatingWithNegativePrice() {
        BigDecimal negativePrice = new BigDecimal("-10.00");

        assertThatThrownBy(() -> assetService.updatePrice(1L, negativePrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating with zero price")
    void shouldThrowExceptionWhenUpdatingWithZeroPrice() {
        assertThatThrownBy(() -> assetService.updatePrice(1L, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deactivate asset")
    void shouldDeactivateAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        assetService.deactivateAsset(1L);

        verify(assetRepository).save(testAsset);
    }

    @Test
    @DisplayName("Should reactivate asset")
    void shouldReactivateAsset() {
        testAsset.setActive(false);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        assetService.reactivateAsset(1L);

        verify(assetRepository).save(testAsset);
    }

    @Test
    @DisplayName("Should bulk deactivate assets")
    void shouldBulkDeactivateAssets() {
        List<Long> assetIds = Arrays.asList(1L, 2L, 3L);

        assetService.bulkDeactivate(assetIds);

        verify(assetRepository).updateActiveStatus(assetIds, false);
    }

    @Test
    @DisplayName("Should update asset info")
    void shouldUpdateAssetInfo() {
        String newName = "Apple Inc. (Updated)";
        String newExchange = "NASDAQ-GS";

        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        Asset updated = assetService.updateAssetInfo(1L, newName, newExchange);

        assertThat(updated).isNotNull();
        verify(assetRepository).save(testAsset);
    }

    @Test
    @DisplayName("Should get assets needing price update")
    void shouldGetAssetsNeedingPriceUpdate() {
        Asset assetWithoutPrice = new Asset();
        assetWithoutPrice.setId(2L);
        assetWithoutPrice.setTicker("TSLA");
        assetWithoutPrice.setCurrentPrice(null);

        when(assetRepository.findAssetsWithoutPrice())
                .thenReturn(Arrays.asList(assetWithoutPrice));

        List<Asset> assets = assetService.getAssetsNeedingPriceUpdate();

        assertThat(assets).hasSize(1);
        verify(assetRepository).findAssetsWithoutPrice();
    }

    @Test
    @DisplayName("Should count assets by type")
    void shouldCountAssetsByType() {
        when(assetRepository.countByAssetType(AssetType.STOCK)).thenReturn(5L);

        long count = assetService.countAssetsByType(AssetType.STOCK);

        assertThat(count).isEqualTo(5L);
        verify(assetRepository).countByAssetType(AssetType.STOCK);
    }
}