package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for Portfolio business logic.
 * Handles portfolio operations, validation, and calculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    /**
     * Creates new portfolio with initial cash deposit.
     * Validates unique name before creation.
     */
    @Transactional
    public Portfolio createPortfolio(String name, String description, BigDecimal initialCash) {

        log.info("Creating portfolio: {}", name);

        if (portfolioRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Portfolio with name '" + name + "' already exists");
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setName(name);
        portfolio.setDescription(description);
        portfolio.setAvailableCash(initialCash);
        portfolio.setTotalValue(initialCash);

        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("Portfolio created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Retrieves portfolio by ID.
     * Throws exception if not found.
     */
    public Portfolio getPortfolioById(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found with ID: " + id));
    }

    /**
     * Retrieves portfolio with all positions loaded.
     * Use this when you need to access position details.
     */
    public Portfolio getPortfolioWithPositions(Long id) {
        return portfolioRepository.findByIdWithPositions(id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found with ID: " + id));
    }

    /**
     * Lists all portfolios ordered by creation date.
     */
    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Updates portfolio basic information.
     * Does not affect positions or cash balance.
     */
    @Transactional
    public Portfolio updatePortfolio(Long id, String name, String description) {

        log.info("Updating portfolio ID: {}", id);

        Portfolio portfolio = getPortfolioById(id);

        // Check name uniqueness if changed
        if (!portfolio.getName().equalsIgnoreCase(name) &&
                portfolioRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Portfolio with name '" + name + "' already exists");
        }

        portfolio.setName(name);
        portfolio.setDescription(description);

        return portfolioRepository.save(portfolio);
    }

    /**
     * Adds cash to portfolio (deposit).
     * Increases available cash and recalculates total value.
     */
    @Transactional
    public Portfolio depositCash(Long portfolioId, BigDecimal amount) {

        log.info("Depositing {} to portfolio ID: {}", amount, portfolioId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        Portfolio portfolio = getPortfolioById(portfolioId);
        portfolio.setAvailableCash(portfolio.getAvailableCash().add(amount));
        portfolio.calculateTotalValue();

        return portfolioRepository.save(portfolio);
    }

    /**
     * Withdraws cash from portfolio.
     * Validates sufficient available cash before withdrawal.
     */
    @Transactional
    public Portfolio withdrawCash(Long portfolioId, BigDecimal amount) {

        log.info("Withdrawing {} from portfolio ID: {}", amount, portfolioId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        Portfolio portfolio = getPortfolioById(portfolioId);

        if (portfolio.getAvailableCash().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available cash. Available: " +
                    portfolio.getAvailableCash() + ", Requested: " + amount);
        }

        portfolio.setAvailableCash(portfolio.getAvailableCash().subtract(amount));
        portfolio.calculateTotalValue();

        return portfolioRepository.save(portfolio);
    }

    /**
     * Recalculates portfolio total value based on positions and cash.
     * Should be called after position updates or price changes.
     */
    @Transactional
    public Portfolio recalculatePortfolioValue(Long portfolioId) {

        log.info("Recalculating value for portfolio ID: {}", portfolioId);

        Portfolio portfolio = getPortfolioWithPositions(portfolioId);
        portfolio.calculateTotalValue();

        return portfolioRepository.save(portfolio);
    }

    /**
     * Deletes portfolio.
     * Cascade delete removes all associated positions and transactions.
     */
    @Transactional
    public void deletePortfolio(Long id) {

        log.info("Deleting portfolio ID: {}", id);

        Portfolio portfolio = getPortfolioById(id);
        portfolioRepository.delete(portfolio);

        log.info("Portfolio deleted successfully");
    }

    /**
     * Finds portfolios with value above threshold.
     */
    public List<Portfolio> findPortfoliosAboveValue(BigDecimal minValue) {
        return portfolioRepository.findPortfoliosAboveValue(minValue);
    }
}