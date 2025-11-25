package com.github.hnrdejesus.invest_track.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a financial asset (stock, ETF, crypto, etc.).
 *
 * Assets are catalog entries that can be referenced by multiple positions
 * across different portfolios. This avoids data duplication.
 *
 * Example: AAPL stock is one Asset, but can be in many portfolios.
 *
 * @author Henrique de Jesus
 */
@Entity
@Table(
        name = "assets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assets_ticker", columnNames = "ticker")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Asset extends BaseEntity {

    /**
     * Asset ticker symbol (e.g., AAPL, BTC-USD, SPY).
     * Must be unique across all assets.
     */
    @NotBlank(message = "Ticker symbol is required")
    @Size(min = 1, max = 20, message = "Ticker must be between 1 and 20 characters")
    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    /**
     * Full name of the asset (e.g., "Apple Inc.", "Bitcoin").
     */
    @NotBlank(message = "Asset name is required")
    @Size(min = 2, max = 200, message = "Asset name must be between 2 and 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Type of asset (STOCK, ETF, CRYPTO, etc.).
     * Stored as String in database for flexibility.
     */
    @NotNull(message = "Asset type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetType assetType;

    /**
     * Current market price of the asset.
     * Updated periodically from external APIs.
     *
     * Null if price is not available yet.
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal currentPrice;

    /**
     * Currency of the asset (USD, BRL, EUR, etc.).
     * Default is USD for most international assets.
     */
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    /**
     * Stock exchange where the asset is traded (e.g., NASDAQ, NYSE, BINANCE).
     * Optional field for additional context.
     */
    @Size(max = 50, message = "Exchange name cannot exceed 50 characters")
    @Column(length = 50)
    private String exchange;

    /**
     * Indicates if the asset is actively tracked.
     * Inactive assets won't be updated from external APIs.
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Updates the current price of the asset.
     * Should be called by the market data service.
     *
     * @param newPrice the new market price
     */
    public void updatePrice(BigDecimal newPrice) {
        this.currentPrice = newPrice;
    }

    /**
     * Checks if the asset has a valid price.
     *
     * @return true if currentPrice is not null and greater than zero
     */
    public boolean hasValidPrice() {
        return currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0;
    }
}