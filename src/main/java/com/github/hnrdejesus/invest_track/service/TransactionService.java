package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Transaction;
import com.github.hnrdejesus.invest_track.domain.TransactionType;
import com.github.hnrdejesus.invest_track.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Transaction recording and audit trail.
 * Transactions are immutable - no update or delete operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PortfolioService portfolioService;
    private final AssetService assetService;

    /**
     * Records buy transaction.
     * Should be called by PositionService after successful buy operation.
     */
    @Transactional
    public Transaction recordBuyTransaction(
            Long portfolioId,
            Long assetId,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal fees) {

        log.info("Recording BUY transaction: {} x {} of asset {}", quantity, price, assetId);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        Asset asset = assetService.getAssetById(assetId);

        BigDecimal totalAmount = quantity.multiply(price);

        Transaction transaction = new Transaction();
        transaction.setPortfolio(portfolio);
        transaction.setAsset(asset);
        transaction.setType(TransactionType.BUY);
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setTotalAmount(totalAmount);
        transaction.setFees(fees != null ? fees : BigDecimal.ZERO);
        transaction.setTransactionDate(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    /**
     * Records sell transaction.
     * Should be called by PositionService after successful sell operation.
     */
    @Transactional
    public Transaction recordSellTransaction(
            Long portfolioId,
            Long assetId,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal fees) {

        log.info("Recording SELL transaction: {} x {} of asset {}", quantity, price, assetId);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        Asset asset = assetService.getAssetById(assetId);

        BigDecimal totalAmount = quantity.multiply(price);

        Transaction transaction = new Transaction();
        transaction.setPortfolio(portfolio);
        transaction.setAsset(asset);
        transaction.setType(TransactionType.SELL);
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setTotalAmount(totalAmount);
        transaction.setFees(fees != null ? fees : BigDecimal.ZERO);
        transaction.setTransactionDate(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    /**
     * Records cash deposit transaction.
     */
    @Transactional
    public Transaction recordDepositTransaction(Long portfolioId, BigDecimal amount, String notes) {

        log.info("Recording DEPOSIT transaction: {} to portfolio {}", amount, portfolioId);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);

        Transaction transaction = new Transaction();
        transaction.setPortfolio(portfolio);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setTotalAmount(amount);
        transaction.setFees(BigDecimal.ZERO);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setNotes(notes);

        return transactionRepository.save(transaction);
    }

    /**
     * Records cash withdrawal transaction.
     */
    @Transactional
    public Transaction recordWithdrawalTransaction(Long portfolioId, BigDecimal amount, String notes) {

        log.info("Recording WITHDRAWAL transaction: {} from portfolio {}", amount, portfolioId);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);

        Transaction transaction = new Transaction();
        transaction.setPortfolio(portfolio);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setTotalAmount(amount);
        transaction.setFees(BigDecimal.ZERO);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setNotes(notes);

        return transactionRepository.save(transaction);
    }

    /**
     * Records dividend payment transaction.
     */
    @Transactional
    public Transaction recordDividendTransaction(
            Long portfolioId,
            Long assetId,
            BigDecimal amount,
            String notes) {

        log.info("Recording DIVIDEND transaction: {} from asset {}", amount, assetId);

        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
        Asset asset = assetService.getAssetById(assetId);

        Transaction transaction = new Transaction();
        transaction.setPortfolio(portfolio);
        transaction.setAsset(asset);
        transaction.setType(TransactionType.DIVIDEND);
        transaction.setTotalAmount(amount);
        transaction.setFees(BigDecimal.ZERO);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setNotes(notes);

        return transactionRepository.save(transaction);
    }

    /**
     * Retrieves paginated transaction history for portfolio.
     */
    public Page<Transaction> getPortfolioTransactions(Long portfolioId, Pageable pageable) {
        return transactionRepository.findByPortfolioIdOrderByTransactionDateDesc(portfolioId, pageable);
    }

    /**
     * Retrieves all transactions for specific asset in portfolio.
     */
    public List<Transaction> getAssetTransactionHistory(Long portfolioId, Long assetId) {
        return transactionRepository.findByPortfolioIdAndAssetId(portfolioId, assetId);
    }

    /**
     * Retrieves transactions within date range.
     * Useful for tax reporting and period analysis.
     */
    public List<Transaction> getTransactionsByDateRange(
            Long portfolioId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return transactionRepository.findByPortfolioIdAndDateRange(portfolioId, startDate, endDate);
    }

    /**
     * Gets recent transactions across all portfolios.
     * Dashboard activity feed.
     */
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByTransactionDateDesc();
    }

    /**
     * Calculates total transaction volume for specific types.
     * Example: Total BUY + SELL volume for trading activity analysis.
     */
    public BigDecimal calculateTotalVolume(Long portfolioId, List<TransactionType> types) {
        BigDecimal total = transactionRepository.calculateTotalVolumeByTypes(portfolioId, types);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculates total fees paid in portfolio.
     */
    public BigDecimal calculateTotalFees(Long portfolioId) {
        BigDecimal total = transactionRepository.calculateTotalFees(portfolioId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Counts transactions by type.
     */
    public long countTransactionsByType(Long portfolioId, TransactionType type) {
        return transactionRepository.countByPortfolioIdAndType(portfolioId, type);
    }

    /**
     * Retrieves transaction by ID.
     */
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + id));
    }
}