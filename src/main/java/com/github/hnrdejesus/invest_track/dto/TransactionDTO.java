package com.github.hnrdejesus.invest_track.dto;

import com.github.hnrdejesus.invest_track.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Transaction data transfer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;

    private Long portfolioId;

    private Long assetId;

    private String assetTicker;

    private TransactionType type;

    private BigDecimal quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private BigDecimal fees;

    private LocalDateTime transactionDate;

    private String notes;

    private LocalDateTime createdAt;
}