package com.github.hnrdejesus.invest_track.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable record of portfolio transactions.
 * Maintains complete audit trail of all buy/sell/deposit/withdrawal operations.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @NotNull(message = "Portfolio is required")
    private Portfolio portfolio;

    /**
     * Asset is null for DEPOSIT/WITHDRAWAL transactions.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    /**
     * Number of shares transacted.
     * Null for DEPOSIT/WITHDRAWAL/DIVIDEND.
     */
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    @Column(precision = 20, scale = 8)
    private BigDecimal quantity;

    /**
     * Price per unit at transaction time.
     * Captures historical price for accurate P&L tracking.
     */
    @DecimalMin(value = "0.01", message = "Price must be positive")
    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    /**
     * Total transaction amount (quantity * price + fees).
     * For DEPOSIT/WITHDRAWAL, represents cash amount.
     */
    @NotNull(message = "Total amount is required")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Brokerage fees, commissions, or transaction costs.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;

    /**
     * When the transaction occurred (may differ from createdAt for historical imports).
     */
    @NotNull(message = "Transaction date is required")
    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(length = 500)
    private String notes;

    /**
     * Calculates net amount after fees.
     * BUY: totalAmount includes fees (you pay more)
     * SELL: totalAmount minus fees (you receive less)
     */
    public BigDecimal getNetAmount() {
        if (type == TransactionType.BUY) {
            return totalAmount.add(fees);
        }
        return totalAmount.subtract(fees);
    }
}