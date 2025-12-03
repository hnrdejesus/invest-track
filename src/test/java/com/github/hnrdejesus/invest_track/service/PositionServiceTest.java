package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.exception.ResourceNotFoundException;
import com.github.hnrdejesus.invest_track.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PositionService.
 * Tests buy/sell operations, weighted average calculations, and validations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PositionService Tests")
class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private PositionService positionService;

    private Portfolio testPortfolio;
    private Asset testAsset;
    private Position testPosition;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(positionService, "maxAssetsPerPortfolio", 100);

        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setAvailableCash(new BigDecimal("10000.00"));

        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setTicker("AAPL");
        testAsset.setName("Apple Inc.");
        testAsset.setAssetType(AssetType.STOCK);
        testAsset.setActive(true);

        testPosition = new Position();
        testPosition.setId(1L);
        testPosition.setPortfolio(testPortfolio);
        testPosition.setAsset(testAsset);
        testPosition.setQuantity(new BigDecimal("10.0"));
        testPosition.setAveragePrice(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should get portfolio positions with assets loaded")
    void shouldGetPortfolioPositions() {
        when(positionRepository.findByPortfolioIdWithAssets(1L))
                .thenReturn(Arrays.asList(testPosition));

        List<Position> positions = positionService.getPortfolioPositions(1L);

        assertThat(positions).hasSize(1);
        assertThat(positions.get(0).getAsset().getTicker()).isEqualTo("AAPL");
        verify(positionRepository).findByPortfolioIdWithAssets(1L);
    }

    @Test
    @DisplayName("Should get specific position by portfolio and asset")
    void shouldGetPosition() {
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Optional.of(testPosition));

        Position found = positionService.getPosition(1L, 1L);

        assertThat(found).isNotNull();
        assertThat(found.getAsset().getTicker()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should throw exception when position not found")
    void shouldThrowExceptionWhenPositionNotFound() {
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> positionService.getPosition(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Position not found");
    }

    @Test
    @DisplayName("Should buy asset and create new position")
    void shouldBuyAssetAndCreateNewPosition() {
        BigDecimal quantity = new BigDecimal("5.0");
        BigDecimal price = new BigDecimal("150.00");

        when(portfolioService.getPortfolioWithPositions(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Optional.empty());
        when(positionRepository.countByPortfolioId(1L)).thenReturn(10L);
        when(positionRepository.save(any(Position.class))).thenReturn(testPosition);

        // First purchase: creates new position with 5 shares @ $150
        Position result = positionService.buyAsset(1L, 1L, quantity, price);

        assertThat(result).isNotNull();
        verify(positionRepository).save(any(Position.class));
    }

    @Test
    @DisplayName("Should buy asset and update existing position with weighted average")
    void shouldBuyAssetAndUpdateExistingPosition() {
        BigDecimal newQuantity = new BigDecimal("5.0");
        BigDecimal newPrice = new BigDecimal("160.00");

        when(portfolioService.getPortfolioWithPositions(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Optional.of(testPosition));
        when(positionRepository.save(any(Position.class))).thenReturn(testPosition);

        // Existing position: 10 shares @ $150 = $1,500
        // New purchase: 5 shares @ $160 = $800
        // Expected result: 15 shares @ $153.33 (weighted average)
        Position result = positionService.buyAsset(1L, 1L, newQuantity, newPrice);

        assertThat(result).isNotNull();
        verify(positionRepository).save(testPosition);
    }

    @Test
    @DisplayName("Should throw exception when buying with insufficient cash")
    void shouldThrowExceptionWhenInsufficientCash() {
        testPortfolio.setAvailableCash(new BigDecimal("100.00"));
        BigDecimal quantity = new BigDecimal("100.0");
        BigDecimal price = new BigDecimal("150.00");

        when(portfolioService.getPortfolioWithPositions(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);

        // Total cost: $15,000 but only $100 available
        assertThatThrownBy(() -> positionService.buyAsset(1L, 1L, quantity, price))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient cash");

        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when buying inactive asset")
    void shouldThrowExceptionWhenBuyingInactiveAsset() {
        testAsset.setActive(false);

        when(portfolioService.getPortfolioWithPositions(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);

        assertThatThrownBy(() -> positionService.buyAsset(1L, 1L, BigDecimal.TEN, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive asset");

        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when max positions limit reached")
    void shouldThrowExceptionWhenMaxPositionsReached() {
        ReflectionTestUtils.setField(positionService, "maxAssetsPerPortfolio", 10);

        when(portfolioService.getPortfolioWithPositions(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Optional.empty());
        when(positionRepository.countByPortfolioId(1L)).thenReturn(10L);

        // Portfolio already has 10 positions (limit reached)
        // Attempting to add new position (should fail)
        assertThatThrownBy(() -> positionService.buyAsset(1L, 1L, BigDecimal.TEN, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum number of positions");

        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when buying with negative quantity")
    void shouldThrowExceptionWhenBuyingWithNegativeQuantity() {
        assertThatThrownBy(() -> positionService.buyAsset(1L, 1L, new BigDecimal("-5"), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    @DisplayName("Should throw exception when buying with negative price")
    void shouldThrowExceptionWhenBuyingWithNegativePrice() {
        assertThatThrownBy(() -> positionService.buyAsset(1L, 1L, BigDecimal.TEN, new BigDecimal("-10")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be positive");
    }

    @Test
    @DisplayName("Should sell asset and reduce position")
    void shouldSellAssetAndReducePosition() {
        BigDecimal sellQuantity = new BigDecimal("5.0");
        BigDecimal sellPrice = new BigDecimal("160.00");

        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Optional.of(testPosition));
        when(positionRepository.save(any(Position.class))).thenReturn(testPosition);

        // Selling 5 out of 10 shares (50% of position)
        // Average price remains $150, only quantity changes
        Position result = positionService.sellAsset(1L, 1L, sellQuantity, sellPrice);

        assertThat(result).isNotNull();
        verify(positionRepository).save(testPosition);
    }

    @Test
    @DisplayName("Should sell all and close position when quantity reaches zero")
    void shouldClosePositionWhenSellingAll() {
        BigDecimal sellQuantity = new BigDecimal("10.0");
        BigDecimal sellPrice = new BigDecimal("160.00");

        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Optional.of(testPosition));

        // Selling all 10 shares: position should be deleted
        Position result = positionService.sellAsset(1L, 1L, sellQuantity, sellPrice);

        assertThat(result).isNull();
        verify(positionRepository).delete(testPosition);
    }

    @Test
    @DisplayName("Should throw exception when selling more than owned")
    void shouldThrowExceptionWhenSellingMoreThanOwned() {
        BigDecimal excessiveQuantity = new BigDecimal("100.0");

        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Optional.of(testPosition));

        // Attempting to sell 100 shares but only 10 are owned
        assertThatThrownBy(() -> positionService.sellAsset(1L, 1L, excessiveQuantity, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient quantity");

        verify(positionRepository, never()).save(any());
        verify(positionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should close entire position")
    void shouldCloseEntirePosition() {
        when(positionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Optional.of(testPosition));
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);

        positionService.closePosition(1L, 1L, new BigDecimal("160.00"));

        verify(positionRepository).delete(testPosition);
    }

    @Test
    @DisplayName("Should calculate total investment")
    void shouldCalculateTotalInvestment() {
        when(positionRepository.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("1500.00"));

        BigDecimal total = positionService.calculateTotalInvestment(1L);

        assertThat(total).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Should return zero when no positions exist")
    void shouldReturnZeroWhenNoPositionsExist() {
        when(positionRepository.calculateTotalInvestment(1L)).thenReturn(null);

        BigDecimal total = positionService.calculateTotalInvestment(1L);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should find small positions below threshold")
    void shouldFindSmallPositions() {
        BigDecimal minQuantity = new BigDecimal("1.0");
        when(positionRepository.findSmallPositions(1L, minQuantity))
                .thenReturn(Arrays.asList(testPosition));

        List<Position> smallPositions = positionService.findSmallPositions(1L, minQuantity);

        assertThat(smallPositions).hasSize(1);
        verify(positionRepository).findSmallPositions(1L, minQuantity);
    }

    @Test
    @DisplayName("Should check if position exists")
    void shouldCheckIfPositionExists() {
        when(positionRepository.existsByPortfolioIdAndAssetId(1L, 1L)).thenReturn(true);

        boolean exists = positionService.hasPosition(1L, 1L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should count positions in portfolio")
    void shouldCountPositions() {
        when(positionRepository.countByPortfolioId(1L)).thenReturn(5L);

        long count = positionService.countPositions(1L);

        assertThat(count).isEqualTo(5L);
    }
}