package com.github.hnrdejesus.invest_track.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for price response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponseDTO {
    private String ticker;
    private BigDecimal price;
}