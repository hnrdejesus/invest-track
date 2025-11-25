package com.github.hnrdejesus.invest_track.repository;

import com.github.hnrdejesus.invest_track.domain.Transaction;
import com.github.hnrdejesus.invest_track.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Transaction entity with audit trail queries.
 * Transactions are immutable - no update methods provided.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds paginated transactions for portfolio ordered by date (newest first).
     * Pageable allows frontend to request specific page and size.
     *
     * Example: findByPortfolioId(portfolioId, PageRequest.of(0, 20))
     * Returns first 20 transactions.
     */
    Page<Transaction> findByPortfolioIdOrderByTransactionDateDesc(Long portfolioId, Pageable pageable);

    /**
     * Finds all transactions for specific asset across all portfolios.
     * Useful for asset history analysis.
     */
    List<Transaction> findByAssetIdOrderByTransactionDateDesc(Long assetId);

    /**
     * Finds transactions by type and portfolio.
     * Example: Get all BUY transactions for a portfolio.
     */
    List<Transaction> findByPortfolioIdAndType(Long portfolioId, TransactionType type);

    /**
     * Finds transactions within date range for portfolio.
     * Useful for monthly/yearly reports and tax calculations.
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.portfolio.id = :portfolioId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByPortfolioIdAndDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Calculates total transaction volume for portfolio.
     * Sum of all transaction amounts (useful for broker fee analysis).
     */
    @Query("SELECT SUM(t.totalAmount) FROM Transaction t " +
            "WHERE t.portfolio.id = :portfolioId " +
            "AND t.type IN :types")
    BigDecimal calculateTotalVolumeByTypes(
            @Param("portfolioId") Long portfolioId,
            @Param("types") List<TransactionType> types
    );

    /**
     * Finds recent transactions across all portfolios.
     * Useful for dashboard activity feed.
     */
    List<Transaction> findTop10ByOrderByTransactionDateDesc();

    /**
     * Calculates total fees paid in portfolio.
     * Helps track trading costs.
     */
    @Query("SELECT SUM(t.fees) FROM Transaction t WHERE t.portfolio.id = :portfolioId")
    BigDecimal calculateTotalFees(@Param("portfolioId") Long portfolioId);

    /**
     * Finds transactions for specific asset in portfolio.
     * Complete transaction history for single holding.
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.portfolio.id = :portfolioId " +
            "AND t.asset.id = :assetId " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByPortfolioIdAndAssetId(
            @Param("portfolioId") Long portfolioId,
            @Param("assetId") Long assetId
    );

    /**
     * Counts transactions by type.
     * Dashboard statistics (e.g., "15 buy operations this month").
     */
    long countByPortfolioIdAndType(Long portfolioId, TransactionType type);
}