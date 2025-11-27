package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.exception.ResourceNotFoundException;
import com.github.hnrdejesus.invest_track.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for Position management and portfolio composition.
 * Handles buy/sell operations and position calculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;
    private final PortfolioService portfolioService;
    private final AssetService assetService;

    @Value("${app.portfolio.max-assets-per-portfolio:100}")
    private int maxAssetsPerPortfolio;

    /**
     * Retrieves all positions in portfolio with asset details loaded.
     * Use this to display portfolio composition.
     */
    public List<Position> getPortfolioPositions(Long portfolioId) {
        return positionRepository.findByPortfolioIdWithAssets(portfolioId);
    }

    /**
     * Finds specific position by portfolio and asset.
     */
    public Position getPosition(Long portfolioId, Long assetId) {
        return positionRepository.findByPortfolioIdAndAssetId(portfolioId, assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Position not found for portfolio %d and asset %d", portfolioId, assetId)));
    }

    /**
     * Adds new position or increases existing position (buy operation).
     *
     * If position exists: updates quantity using weighted average price.
     * If new position: creates position and links to portfolio.
     *
     * Validates sufficient cash and enforces max positions limit.
     */
    @Transactional
    public Position buyAsset(Long portfolioId, Long assetId, BigDecimal quantity, BigDecimal price) {
        log.info("Buying {} units of asset {} for portfolio {}", quantity, assetId, portfolioId);

        validatePositiveAmount(quantity, "Quantity");
        validatePositiveAmount(price, "Price");

        Portfolio portfolio = portfolioService.getPortfolioWithPositions(portfolioId);
        Asset asset = assetService.getAssetById(assetId);

        if (!asset.getActive()) {
            throw new IllegalArgumentException("Cannot buy inactive asset: " + asset.getTicker());
        }

        BigDecimal totalCost = quantity.multiply(price);

        if (portfolio.getAvailableCash().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient cash. Required: %s, Available: %s",
                            totalCost, portfolio.getAvailableCash()));
        }

        Position position = positionRepository.findByPortfolioIdAndAssetId(portfolioId, assetId)
                .orElse(null);

        if (position == null) {
            // Create new position
            if (positionRepository.countByPortfolioId(portfolioId) >= maxAssetsPerPortfolio) {
                throw new IllegalArgumentException(
                        "Maximum number of positions reached: " + maxAssetsPerPortfolio);
            }

            position = new Position();
            position.setPortfolio(portfolio);
            position.setAsset(asset);
            position.setQuantity(quantity);
            position.setAveragePrice(price);

            portfolio.addPosition(position);
            log.info("Created new position for asset {}", asset.getTicker());
        } else {
            // Update existing position with weighted average
            position.addQuantity(quantity, price);
            log.info("Updated position for asset {}, new quantity: {}",
                    asset.getTicker(), position.getQuantity());
        }

        // Deduct cash from portfolio
        portfolio.setAvailableCash(portfolio.getAvailableCash().subtract(totalCost));
        portfolio.calculateTotalValue();

        Position saved = positionRepository.save(position);
        portfolioService.recalculatePortfolioValue(portfolioId);

        return saved;
    }

    /**
     * Reduces or closes position (sell operation).
     *
     * Validates sufficient quantity before selling.
     * If quantity reaches zero, position is automatically removed (orphanRemoval).
     * Cash is credited to portfolio available balance.
     */
    @Transactional
    public Position sellAsset(Long portfolioId, Long assetId, BigDecimal quantity, BigDecimal price) {
        log.info("Selling {} units of asset {} from portfolio {}", quantity, assetId, portfolioId);

        validatePositiveAmount(quantity, "Quantity");
        validatePositiveAmount(price, "Price");

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        Position position = getPosition(portfolioId, assetId);

        if (position.getQuantity().compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient quantity. Owned: %s, Requested: %s",
                            position.getQuantity(), quantity));
        }

        BigDecimal saleProceeds = quantity.multiply(price);

        // Update position
        position.removeQuantity(quantity);

        // Credit cash to portfolio
        portfolio.setAvailableCash(portfolio.getAvailableCash().add(saleProceeds));

        Position saved;
        if (position.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            // Remove position if quantity is zero
            portfolio.removePosition(position);
            positionRepository.delete(position);
            saved = null;
            log.info("Position closed for asset {}", position.getAsset().getTicker());
        } else {
            saved = positionRepository.save(position);
            log.info("Position reduced for asset {}, remaining: {}",
                    position.getAsset().getTicker(), position.getQuantity());
        }

        portfolioService.recalculatePortfolioValue(portfolioId);

        return saved;
    }

    /**
     * Closes entire position (sell all).
     * Convenience method for selling complete holding.
     */
    @Transactional
    public void closePosition(Long portfolioId, Long assetId, BigDecimal sellPrice) {
        Position position = getPosition(portfolioId, assetId);
        sellAsset(portfolioId, assetId, position.getQuantity(), sellPrice);
    }

    /**
     * Calculates total investment (cost basis) across portfolio.
     */
    public BigDecimal calculateTotalInvestment(Long portfolioId) {
        BigDecimal total = positionRepository.calculateTotalInvestment(portfolioId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Finds small positions below threshold.
     * Useful for portfolio cleanup.
     */
    public List<Position> findSmallPositions(Long portfolioId, BigDecimal minQuantity) {
        return positionRepository.findSmallPositions(portfolioId, minQuantity);
    }

    /**
     * Checks if asset already exists in portfolio.
     */
    public boolean hasPosition(Long portfolioId, Long assetId) {
        return positionRepository.existsByPortfolioIdAndAssetId(portfolioId, assetId);
    }

    /**
     * Counts positions in portfolio.
     */
    public long countPositions(Long portfolioId) {
        return positionRepository.countByPortfolioId(portfolioId);
    }

    private void validatePositiveAmount(BigDecimal amount, String fieldName) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}