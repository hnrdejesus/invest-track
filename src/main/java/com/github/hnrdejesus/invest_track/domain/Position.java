package com.github.hnrdejesus.invest_track.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Position represents asset ownership within a portfolio.
 * Tracks quantity, average purchase price, and calculates current value.
 */
@Entity
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Position extends BaseEntity {

    /**
     * FetchType.LAZY defers loading until accessed - critical for performance.
     * Prevents N+1 query problem when loading multiple positions.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @NotNull(message = "Portfolio is required")
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @NotNull(message = "Asset is required")
    private Asset asset;

    /**
     * Number of shares/units owned.
     * Scale 8 supports fractional shares (e.g., 0.00000001 BTC).
     */
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    /**
     * Average cost per unit (cost basis).
     * Used for profit/loss calculations: (currentPrice - averagePrice) * quantity
     */
    @NotNull(message = "Average price is required")
    @DecimalMin(value = "0.01", message = "Average price must be positive")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal averagePrice;

    /**
     * Calculates current market value: quantity * asset's current price.
     * Returns zero if asset has no valid price.
     */
    public BigDecimal getCurrentValue() {
        if (asset == null || !asset.hasValidPrice()) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(asset.getCurrentPrice())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates total cost basis: quantity * average purchase price.
     */
    public BigDecimal getCostBasis() {
        return quantity.multiply(averagePrice)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates unrealized profit/loss: current value - cost basis.
     * Positive = profit, Negative = loss.
     */
    public BigDecimal getUnrealizedProfitLoss() {
        return getCurrentValue().subtract(getCostBasis());
    }

    /**
     * Calculates profit/loss percentage: ((current - cost) / cost) * 100.
     * Returns zero if cost basis is zero (edge case protection).
     */
    public BigDecimal getProfitLossPercentage() {
        BigDecimal costBasis = getCostBasis();
        if (costBasis.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return getUnrealizedProfitLoss()
                .divide(costBasis, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Updates position after a buy transaction.
     * Recalculates average price using weighted average formula.
     *
     * Formula: newAvg = (oldQty * oldPrice + buyQty * buyPrice) / (oldQty + buyQty)
     */
    public void addQuantity(BigDecimal additionalQuantity, BigDecimal purchasePrice) {
        BigDecimal currentCost = quantity.multiply(averagePrice);
        BigDecimal additionalCost = additionalQuantity.multiply(purchasePrice);
        BigDecimal totalCost = currentCost.add(additionalCost);

        this.quantity = quantity.add(additionalQuantity);
        this.averagePrice = totalCost.divide(quantity, 2, RoundingMode.HALF_UP);
    }

    /**
     * Updates position after a sell transaction.
     * Average price remains unchanged (preserves cost basis for remaining shares).
     */
    public void removeQuantity(BigDecimal soldQuantity) {
        if (soldQuantity.compareTo(quantity) > 0) {
            throw new IllegalArgumentException("Cannot sell more than owned quantity");
        }
        this.quantity = quantity.subtract(soldQuantity);
    }
}