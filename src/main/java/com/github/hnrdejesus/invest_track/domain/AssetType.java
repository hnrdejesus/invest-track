package com.github.hnrdejesus.invest_track.domain;

/**
 * Enumeration of asset types supported by the system.
 *
 * Asset types determine how the asset behaves in calculations
 * and which data sources are used for pricing.
 *
 * @author Henrique de Jesus
 */
public enum AssetType {

    /**
     * Individual company stock (e.g., AAPL, GOOGL).
     * Traded on stock exchanges.
     */
    STOCK("Stock"),

    /**
     * Exchange-Traded Fund - basket of stocks/bonds.
     * Provides diversification in a single asset.
     */
    ETF("ETF"),

    /**
     * Real Estate Investment Trust.
     * Invests in real estate properties or mortgages.
     */
    REIT("REIT"),

    /**
     * Cryptocurrency (e.g., BTC, ETH).
     * Digital assets with high volatility.
     */
    CRYPTO("Cryptocurrency"),

    /**
     * Fixed income security issued by governments or corporations.
     * Generally lower risk than stocks.
     */
    BOND("Bond"),

    /**
     * Mutual Fund - professionally managed investment pool.
     * Similar to ETF but not traded on exchanges.
     */
    MUTUAL_FUND("Mutual Fund"),

    /**
     * Raw materials or agricultural products (e.g., gold, oil, wheat).
     * Often used as inflation hedge.
     */
    COMMODITY("Commodity"),

    /**
     * Other asset types not covered by specific categories.
     */
    OTHER("Other");

    private final String displayName;

    /**
     * Constructor for enum values.
     *
     * @param displayName user-friendly name for the asset type
     */
    AssetType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the user-friendly display name.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }
}