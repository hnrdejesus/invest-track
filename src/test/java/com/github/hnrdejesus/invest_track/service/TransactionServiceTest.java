package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Transaction;
import com.github.hnrdejesus.invest_track.domain.TransactionType;
import com.github.hnrdejesus.invest_track.exception.ResourceNotFoundException;
import com.github.hnrdejesus.invest_track.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 * Tests immutable audit trail recording and reporting.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private TransactionService transactionService;

    private Portfolio testPortfolio;
    private Asset testAsset;
    private Transaction buyTransaction;
    private Transaction sellTransaction;

    @BeforeEach
    void setUp() {
        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setAvailableCash(new BigDecimal("10000.00"));

        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setTicker("AAPL");
        testAsset.setName("Apple Inc.");
        testAsset.setAssetType(AssetType.STOCK);
        testAsset.setCurrentPrice(new BigDecimal("150.00"));

        buyTransaction = new Transaction();
        buyTransaction.setId(1L);
        buyTransaction.setPortfolio(testPortfolio);
        buyTransaction.setAsset(testAsset);
        buyTransaction.setType(TransactionType.BUY);
        buyTransaction.setQuantity(new BigDecimal("10.0"));
        buyTransaction.setPrice(new BigDecimal("150.00"));
        buyTransaction.setTotalAmount(new BigDecimal("1500.00"));
        buyTransaction.setFees(new BigDecimal("5.00"));

        sellTransaction = new Transaction();
        sellTransaction.setId(2L);
        sellTransaction.setPortfolio(testPortfolio);
        sellTransaction.setAsset(testAsset);
        sellTransaction.setType(TransactionType.SELL);
        sellTransaction.setQuantity(new BigDecimal("5.0"));
        sellTransaction.setPrice(new BigDecimal("160.00"));
        sellTransaction.setTotalAmount(new BigDecimal("800.00"));
        sellTransaction.setFees(new BigDecimal("3.00"));
    }

    @Test
    @DisplayName("Should record buy transaction successfully")
    void shouldRecordBuyTransactionSuccessfully() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(buyTransaction);

        Transaction result = transactionService.recordBuyTransaction(
                1L, 1L,
                new BigDecimal("10.0"),
                new BigDecimal("150.00"),
                new BigDecimal("5.00")
        );

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.BUY);
        assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("10.0"));
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(result.getFees()).isEqualByComparingTo(new BigDecimal("5.00"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should calculate total amount correctly on buy")
    void shouldCalculateTotalAmountCorrectlyOnBuy() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        transactionService.recordBuyTransaction(
                1L, 1L,
                new BigDecimal("10.0"),  // quantity
                new BigDecimal("150.00"),  // price
                new BigDecimal("5.00")
        );

        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1500.00")); // 10 * 150
    }

    @Test
    @DisplayName("Should default fees to zero when null")
    void shouldDefaultFeesToZeroWhenNull() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        transactionService.recordBuyTransaction(
                1L, 1L,
                new BigDecimal("10.0"),
                new BigDecimal("150.00"),
                null  // null fees
        );

        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertThat(saved.getFees()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should record sell transaction successfully")
    void shouldRecordSellTransactionSuccessfully() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sellTransaction);

        Transaction result = transactionService.recordSellTransaction(
                1L, 1L,
                new BigDecimal("5.0"),
                new BigDecimal("160.00"),
                new BigDecimal("3.00")
        );

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.SELL);
        assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("160.00"));
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("Should record deposit transaction without asset")
    void shouldRecordDepositTransactionWithoutAsset() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        transactionService.recordDepositTransaction(
                1L,
                new BigDecimal("5000.00"),
                "Initial deposit"
        );

        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(saved.getAsset()).isNull();  // Deposits don't have assets
        assertThat(saved.getFees()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getNotes()).isEqualTo("Initial deposit");
    }

    @Test
    @DisplayName("Should record withdrawal transaction without asset")
    void shouldRecordWithdrawalTransactionWithoutAsset() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        transactionService.recordWithdrawalTransaction(
                1L,
                new BigDecimal("2000.00"),
                "Emergency withdrawal"
        );

        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(saved.getAsset()).isNull();
        assertThat(saved.getNotes()).isEqualTo("Emergency withdrawal");
    }

    @Test
    @DisplayName("Should record dividend transaction with asset")
    void shouldRecordDividendTransactionWithAsset() {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        transactionService.recordDividendTransaction(
                1L, 1L,
                new BigDecimal("50.00"),
                "Quarterly dividend"
        );

        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(TransactionType.DIVIDEND);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(saved.getAsset()).isNotNull();
        assertThat(saved.getAsset().getTicker()).isEqualTo("AAPL");
        assertThat(saved.getNotes()).isEqualTo("Quarterly dividend");
    }

    @Test
    @DisplayName("Should retrieve paginated transactions")
    void shouldRetrievePaginatedTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(
                Arrays.asList(buyTransaction, sellTransaction),
                pageable,
                2
        );

        when(transactionRepository.findByPortfolioIdOrderByTransactionDateDesc(1L, pageable))
                .thenReturn(transactionPage);

        Page<Transaction> result = transactionService.getPortfolioTransactions(1L, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should retrieve asset transaction history")
    void shouldRetrieveAssetTransactionHistory() {
        when(transactionRepository.findByPortfolioIdAndAssetId(1L, 1L))
                .thenReturn(Arrays.asList(buyTransaction, sellTransaction));

        List<Transaction> result = transactionService.getAssetTransactionHistory(1L, 1L);

        assertThat(result).hasSize(2);
        assertThat(result).contains(buyTransaction, sellTransaction);
    }

    @Test
    @DisplayName("Should retrieve transactions by date range")
    void shouldRetrieveTransactionsByDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        when(transactionRepository.findByPortfolioIdAndDateRange(1L, start, end))
                .thenReturn(Arrays.asList(buyTransaction, sellTransaction));

        List<Transaction> result = transactionService.getTransactionsByDateRange(1L, start, end);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should retrieve recent transactions with assets eagerly loaded")
    void shouldRetrieveRecentTransactionsWithAssets() {
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepository.findRecentTransactionsWithAssets(pageable))
                .thenReturn(Arrays.asList(buyTransaction, sellTransaction));

        List<Transaction> result = transactionService.getRecentTransactions();

        assertThat(result).hasSize(2);
        assertThat(result).contains(buyTransaction, sellTransaction);
        // Eager loading prevents N+1 queries when accessing asset details
        verify(transactionRepository).findRecentTransactionsWithAssets(any(Pageable.class));
    }

    @Test
    @DisplayName("Should calculate total volume for transaction types")
    void shouldCalculateTotalVolumeForTransactionTypes() {
        List<TransactionType> types = Arrays.asList(TransactionType.BUY, TransactionType.SELL);
        when(transactionRepository.calculateTotalVolumeByTypes(1L, types))
                .thenReturn(new BigDecimal("2300.00")); // 1500 + 800

        BigDecimal result = transactionService.calculateTotalVolume(1L, types);

        assertThat(result).isEqualByComparingTo(new BigDecimal("2300.00"));
    }

    @Test
    @DisplayName("Should return zero when volume calculation returns null")
    void shouldReturnZeroWhenVolumeCalculationReturnsNull() {
        List<TransactionType> types = Arrays.asList(TransactionType.BUY, TransactionType.SELL);
        when(transactionRepository.calculateTotalVolumeByTypes(1L, types)).thenReturn(null);

        BigDecimal result = transactionService.calculateTotalVolume(1L, types);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate total fees")
    void shouldCalculateTotalFees() {
        when(transactionRepository.calculateTotalFees(1L))
                .thenReturn(new BigDecimal("8.00")); // 5.00 + 3.00

        BigDecimal result = transactionService.calculateTotalFees(1L);

        assertThat(result).isEqualByComparingTo(new BigDecimal("8.00"));
    }

    @Test
    @DisplayName("Should return zero when fees calculation returns null")
    void shouldReturnZeroWhenFeesCalculationReturnsNull() {
        when(transactionRepository.calculateTotalFees(1L)).thenReturn(null);

        BigDecimal result = transactionService.calculateTotalFees(1L);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should count transactions by type")
    void shouldCountTransactionsByType() {
        when(transactionRepository.countByPortfolioIdAndType(1L, TransactionType.BUY))
                .thenReturn(5L);

        long result = transactionService.countTransactionsByType(1L, TransactionType.BUY);

        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should retrieve transaction by ID")
    void shouldRetrieveTransactionById() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(buyTransaction));

        Transaction result = transactionService.getTransactionById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(TransactionType.BUY);
    }

    @Test
    @DisplayName("Should throw exception when transaction not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction");
    }
}