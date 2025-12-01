package com.github.hnrdejesus.invest_track.service;

import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.exception.ResourceNotFoundException;
import com.github.hnrdejesus.invest_track.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortfolioService.
 * Tests business logic and validation rules.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService Tests")
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private Portfolio testPortfolio;

    @BeforeEach
    void setUp() {
        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Tech Portfolio");
        testPortfolio.setDescription("Technology stocks");
        testPortfolio.setAvailableCash(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("Should create portfolio successfully")
    void shouldCreatePortfolio() {
        String name = "New Portfolio";
        String description = "Test portfolio";
        BigDecimal initialCash = new BigDecimal("5000.00");

        when(portfolioRepository.existsByNameIgnoreCase(name)).thenReturn(false);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        Portfolio created = portfolioService.createPortfolio(name, description, initialCash);

        assertThat(created).isNotNull();
        verify(portfolioRepository).existsByNameIgnoreCase(name);
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("Should throw exception when creating portfolio with duplicate name")
    void shouldThrowExceptionWhenCreatingDuplicatePortfolio() {
        String name = "Existing Portfolio";
        when(portfolioRepository.existsByNameIgnoreCase(name)).thenReturn(true);

        assertThatThrownBy(() -> portfolioService.createPortfolio(name, "desc", BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating portfolio with negative cash")
    void shouldThrowExceptionWhenCreatingPortfolioWithNegativeCash() {
        BigDecimal negativeCash = new BigDecimal("-100.00");

        assertThatThrownBy(() -> portfolioService.createPortfolio("Test", "desc", negativeCash))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        // Verify that save was never called (validation happens first)
        verify(portfolioRepository, never()).save(any());
        verify(portfolioRepository, never()).existsByNameIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Should get portfolio by ID")
    void shouldGetPortfolioById() {
        Long portfolioId = 1L;
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(testPortfolio));

        Portfolio found = portfolioService.getPortfolioById(portfolioId);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(portfolioId);
        assertThat(found.getName()).isEqualTo("Tech Portfolio");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when portfolio not found")
    void shouldThrowExceptionWhenPortfolioNotFound() {
        Long portfolioId = 999L;
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.getPortfolioById(portfolioId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Portfolio")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should get all portfolios")
    void shouldGetAllPortfolios() {
        Portfolio portfolio2 = new Portfolio();
        portfolio2.setId(2L);
        portfolio2.setName("Value Portfolio");
        portfolio2.setAvailableCash(new BigDecimal("5000.00"));

        when(portfolioRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(testPortfolio, portfolio2));

        List<Portfolio> result = portfolioService.getAllPortfolios();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Tech Portfolio");
        assertThat(result.get(1).getName()).isEqualTo("Value Portfolio");
        verify(portfolioRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should update portfolio successfully")
    void shouldUpdatePortfolio() {
        Long portfolioId = 1L;
        String newName = "Updated Portfolio";
        String newDescription = "Updated description";

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(testPortfolio));
        when(portfolioRepository.existsByNameIgnoreCase(newName)).thenReturn(false);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        Portfolio updated = portfolioService.updatePortfolio(portfolioId, newName, newDescription);

        assertThat(updated).isNotNull();
        verify(portfolioRepository).save(testPortfolio);
    }

    @Test
    @DisplayName("Should deposit cash successfully")
    void shouldDepositCash() {
        Long portfolioId = 1L;
        BigDecimal depositAmount = new BigDecimal("1000.00");
        BigDecimal initialCash = testPortfolio.getAvailableCash();

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(testPortfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        Portfolio updated = portfolioService.depositCash(portfolioId, depositAmount);

        assertThat(updated.getAvailableCash())
                .isEqualByComparingTo(initialCash.add(depositAmount));
    }

    @Test
    @DisplayName("Should throw exception when depositing negative amount")
    void shouldThrowExceptionWhenDepositingNegativeAmount() {
        Long portfolioId = 1L;
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        assertThatThrownBy(() -> portfolioService.depositCash(portfolioId, negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should withdraw cash successfully")
    void shouldWithdrawCash() {
        Long portfolioId = 1L;
        BigDecimal withdrawAmount = new BigDecimal("500.00");
        BigDecimal initialCash = testPortfolio.getAvailableCash();

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(testPortfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        Portfolio updated = portfolioService.withdrawCash(portfolioId, withdrawAmount);

        assertThat(updated.getAvailableCash())
                .isEqualByComparingTo(initialCash.subtract(withdrawAmount));
    }

    @Test
    @DisplayName("Should throw exception when withdrawing more than available cash")
    void shouldThrowExceptionWhenWithdrawingTooMuch() {
        Long portfolioId = 1L;
        BigDecimal excessiveAmount = new BigDecimal("50000.00");

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(testPortfolio));

        assertThatThrownBy(() -> portfolioService.withdrawCash(portfolioId, excessiveAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient available cash");

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete portfolio successfully")
    void shouldDeletePortfolio() {
        Long portfolioId = 1L;
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(testPortfolio));
        doNothing().when(portfolioRepository).delete(testPortfolio);

        portfolioService.deletePortfolio(portfolioId);

        verify(portfolioRepository).delete(testPortfolio);
    }
}