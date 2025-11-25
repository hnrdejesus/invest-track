package com.github.hnrdejesus.invest_track.repository;

import com.github.hnrdejesus.invest_track.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Position entity with portfolio composition queries.
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    /**
     * Finds all positions in a specific portfolio.
     * Returns empty list if portfolio has no positions.
     */
    List<Position> findByPortfolioId(Long portfolioId);

    /**
     * Finds specific position by portfolio and asset.
     * Used to check if asset already exists in portfolio before adding.
     */
    Optional<Position> findByPortfolioIdAndAssetId(Long portfolioId, Long assetId);

    /**
     * Finds positions with eagerly loaded asset details.
     * Prevents N+1 queries when accessing asset information.
     * Essential for performance when displaying position lists.
     */
    @Query("SELECT p FROM Position p " +
            "JOIN FETCH p.asset " +
            "WHERE p.portfolio.id = :portfolioId")
    List<Position> findByPortfolioIdWithAssets(@Param("portfolioId") Long portfolioId);

    /**
     * Finds all positions holding a specific asset across portfolios.
     * Useful for impact analysis when asset is delisted.
     */
    List<Position> findByAssetId(Long assetId);

    /**
     * Calculates total investment across all positions in portfolio.
     * Sum of (quantity * averagePrice) for all positions.
     */
    @Query("SELECT SUM(p.quantity * p.averagePrice) FROM Position p WHERE p.portfolio.id = :portfolioId")
    BigDecimal calculateTotalInvestment(@Param("portfolioId") Long portfolioId);

    /**
     * Finds positions with quantity below threshold.
     * Identifies small/dust positions that might need cleanup.
     */
    @Query("SELECT p FROM Position p WHERE p.portfolio.id = :portfolioId AND p.quantity < :minQuantity")
    List<Position> findSmallPositions(@Param("portfolioId") Long portfolioId,
                                      @Param("minQuantity") BigDecimal minQuantity);

    /**
     * Counts number of positions in portfolio.
     * Useful for enforcing max positions limit.
     */
    long countByPortfolioId(Long portfolioId);

    /**
     * Checks if specific asset exists in portfolio.
     * More efficient than fetching entire position.
     */
    boolean existsByPortfolioIdAndAssetId(Long portfolioId, Long assetId);
}