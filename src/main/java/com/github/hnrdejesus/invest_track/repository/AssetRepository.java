package com.github.hnrdejesus.invest_track.repository;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Asset entity with market data query capabilities.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Finds asset by ticker symbol (case-insensitive).
     * Ticker is unique, so returns Optional.
     */
    Optional<Asset> findByTickerIgnoreCase(String ticker);

    /**
     * Finds all assets of specific type that are active.
     * Useful for filtering by category (stocks, ETFs, crypto).
     */
    List<Asset> findByAssetTypeAndActiveTrue(AssetType assetType);

    /**
     * Finds all active assets.
     * Used by market data update service to refresh prices.
     */
    List<Asset> findByActiveTrue();

    /**
     * Searches assets by name or ticker containing search term (case-insensitive).
     * Useful for autocomplete/search features in UI.
     *
     * OR operator allows matching either name or ticker.
     */
    @Query("SELECT a FROM Asset a WHERE " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.ticker) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Asset> searchByNameOrTicker(@Param("searchTerm") String searchTerm);

    /**
     * Finds assets without valid prices.
     * Identifies assets needing price updates.
     *
     * IS NULL checks for missing prices.
     */
    @Query("SELECT a FROM Asset a WHERE a.currentPrice IS NULL AND a.active = true")
    List<Asset> findAssetsWithoutPrice();

    /**
     * Bulk updates asset active status.
     * Soft delete pattern - marks as inactive instead of deleting.
     *
     * @Modifying indicates this query modifies data (UPDATE/DELETE).
     * Requires @Transactional in service layer.
     */
    @Modifying
    @Query("UPDATE Asset a SET a.active = :active WHERE a.id IN :ids")
    void updateActiveStatus(@Param("ids") List<Long> ids, @Param("active") Boolean active);

    /**
     * Counts assets by type.
     * Useful for dashboard statistics.
     */
    long countByAssetType(AssetType assetType);
}