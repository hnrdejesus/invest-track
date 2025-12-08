package com.github.hnrdejesus.invest_track.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Investment portfolio aggregating multiple asset positions.
 * Tracks total value, available cash, and maintains consistency
 * through cascading operations to positions.
 */
@Entity
@Table(name = "portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio extends BaseEntity {

    @NotBlank(message = "Portfolio name is required")
    @Size(min = 3, max = 100, message = "Portfolio name must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    /**
     * Sum of all position values plus available cash.
     * BigDecimal prevents floating-point precision errors in financial calculations.
     * Precision 15,2 supports portfolios up to 999 trillion USD.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    /**
     * Liquid cash available for new positions.
     * Decreases on buy orders, increases on sell orders.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal availableCash = BigDecimal.ZERO;

    /**
     * Position ownership uses CascadeType.ALL and orphanRemoval for lifecycle management.
     * Deleting portfolio cascades to positions; removing from list triggers deletion.
     * mappedBy indicates Position owns the foreign key (avoids duplicate FK columns).
     */
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Position> positions = new ArrayList<>();

    /**
     * Transaction history uses CascadeType.ALL and orphanRemoval for lifecycle management.
     * Deleting portfolio cascades to transactions; removing from list triggers deletion.
     * mappedBy indicates Transaction owns the foreign key (avoids duplicate FK columns).
     */
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Maintains bidirectional sync when adding positions.
     * Essential for JPA consistency in @OneToMany relationships.
     */
    public void addPosition(Position position) {
        positions.add(position);
        position.setPortfolio(this);
    }

    public void removePosition(Position position) {
        positions.remove(position);
        position.setPortfolio(null);
    }

    /**
     * Recalculates total value from current position values plus cash.
     * Should be called after position updates or transactions.
     */
    public void calculateTotalValue() {
        BigDecimal positionsValue = positions.stream()
                .map(Position::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalValue = positionsValue.add(availableCash);
    }
}