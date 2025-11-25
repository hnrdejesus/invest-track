package com.github.hnrdejesus.invest_track.domain;

/**
 * Transaction types for portfolio operations.
 */
public enum TransactionType {

    /**
     * Purchase of asset - increases position quantity, decreases available cash.
     */
    BUY("Buy"),

    /**
     * Sale of asset - decreases position quantity, increases available cash.
     */
    SELL("Sell"),

    /**
     * Cash deposit into portfolio - increases available cash.
     */
    DEPOSIT("Deposit"),

    /**
     * Cash withdrawal from portfolio - decreases available cash.
     */
    WITHDRAWAL("Withdrawal"),

    /**
     * Dividend payment received - increases available cash without affecting position.
     */
    DIVIDEND("Dividend");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Determines if transaction impacts portfolio cash balance.
     */
    public boolean affectsCash() {
        return this == BUY || this == SELL || this == DEPOSIT ||
                this == WITHDRAWAL || this == DIVIDEND;
    }

    /**
     * Determines if transaction impacts position quantity.
     */
    public boolean affectsPosition() {
        return this == BUY || this == SELL;
    }
}