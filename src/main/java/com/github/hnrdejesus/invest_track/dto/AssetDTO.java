package com.github.hnrdejesus.invest_track.dto;

import com.github.hnrdejesus.invest_track.domain.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Asset data transfer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {

    private Long id;

    private String ticker;

    private String name;

    private AssetType assetType;

    private BigDecimal currentPrice;

    private String currency;

    private String exchange;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Request DTO for creating asset.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "Ticker is required")
        @Size(min = 1, max = 20)
        private String ticker;

        @NotBlank(message = "Asset name is required")
        @Size(min = 2, max = 200)
        private String name;

        @NotNull(message = "Asset type is required")
        private AssetType assetType;

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
        private String currency = "USD";

        @Size(max = 50)
        private String exchange;
    }

    /**
     * Request DTO for updating asset info.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @NotBlank(message = "Asset name is required")
        @Size(min = 2, max = 200)
        private String name;

        @Size(max = 50)
        private String exchange;
    }
}