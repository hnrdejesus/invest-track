package com.github.hnrdejesus.invest_track.repository;

import com.github.hnrdejesus.invest_track.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Portfolio entity providing database access methods.
 * Spring Data JPA automatically implements basic CRUD operations.
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * Finds portfolio by name (case-insensitive).
     * Method name follows Spring Data JPA naming convention for automatic implementation.
     */
    Optional<Portfolio> findByNameIgnoreCase(String name);

    /**
     * Checks if portfolio with given name exists.
     * Useful for validation before creating new portfolios.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Finds all portfolios ordered by creation date (newest first).
     * Useful for listing portfolios in UI.
     */
    List<Portfolio> findAllByOrderByCreatedAtDesc();

    /**
     * Fetches portfolio with all positions eagerly loaded in single query.
     * Solves N+1 problem when accessing position list.
     *
     * JOIN FETCH loads associated entities in same query.
     * LEFT JOIN includes portfolio even if it has no positions.
     */
    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.positions WHERE p.id = :id")
    Optional<Portfolio> findByIdWithPositions(@Param("id") Long id);

    /**
     * Finds portfolios with total value above specified amount.
     * Demonstrates JPQL query with comparison operators.
     */
    @Query("SELECT p FROM Portfolio p WHERE p.totalValue > :minValue ORDER BY p.totalValue DESC")
    List<Portfolio> findPortfoliosAboveValue(@Param("minValue") java.math.BigDecimal minValue);

    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.positions ORDER BY p.createdAt DESC")
    List<Portfolio> findAllWithPositionsOrderByCreatedAtDesc();
}