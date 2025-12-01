package com.github.hnrdejesus.invest_track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.dto.DTOMapper;
import com.github.hnrdejesus.invest_track.dto.PortfolioDTO;
import com.github.hnrdejesus.invest_track.exception.ResourceNotFoundException;
import com.github.hnrdejesus.invest_track.service.PortfolioService;
import com.github.hnrdejesus.invest_track.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PortfolioController.
 * Tests REST endpoints with MockMvc.
 */
@WebMvcTest(PortfolioController.class)
@DisplayName("PortfolioController Tests")
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private DTOMapper dtoMapper;

    private Portfolio testPortfolio;
    private PortfolioDTO testPortfolioDTO;

    @BeforeEach
    void setUp() {
        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Tech Portfolio");
        testPortfolio.setDescription("Technology stocks");
        testPortfolio.setAvailableCash(new BigDecimal("10000.00"));

        testPortfolioDTO = new PortfolioDTO();
        testPortfolioDTO.setId(1L);
        testPortfolioDTO.setName("Tech Portfolio");
        testPortfolioDTO.setDescription("Technology stocks");
        testPortfolioDTO.setAvailableCash(new BigDecimal("10000.00"));
        testPortfolioDTO.setTotalValue(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("POST /api/portfolios - Should create portfolio")
    void shouldCreatePortfolio() throws Exception {
        PortfolioDTO.CreateRequest request = new PortfolioDTO.CreateRequest();
        request.setName("New Portfolio");
        request.setDescription("Test portfolio");
        request.setInitialCash(new BigDecimal("5000.00"));

        when(portfolioService.createPortfolio(anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(testPortfolio);
        when(dtoMapper.toPortfolioDTO(any(Portfolio.class))).thenReturn(testPortfolioDTO);

        mockMvc.perform(post("/api/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Tech Portfolio")))
                .andExpect(jsonPath("$.availableCash", is(10000.00)));

        verify(portfolioService).createPortfolio(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("POST /api/portfolios - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        PortfolioDTO.CreateRequest request = new PortfolioDTO.CreateRequest();
        request.setName("AB"); // Too short (min 3)
        request.setDescription("Test");
        request.setInitialCash(new BigDecimal("-100.00")); // Negative

        mockMvc.perform(post("/api/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/portfolios - Should return all portfolios")
    void shouldReturnAllPortfolios() throws Exception {
        Portfolio portfolio2 = new Portfolio();
        portfolio2.setId(2L);
        portfolio2.setName("Value Portfolio");

        PortfolioDTO dto2 = new PortfolioDTO();
        dto2.setId(2L);
        dto2.setName("Value Portfolio");

        when(portfolioService.getAllPortfolios()).thenReturn(Arrays.asList(testPortfolio, portfolio2));
        when(dtoMapper.toPortfolioDTO(testPortfolio)).thenReturn(testPortfolioDTO);
        when(dtoMapper.toPortfolioDTO(portfolio2)).thenReturn(dto2);

        mockMvc.perform(get("/api/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Tech Portfolio")))
                .andExpect(jsonPath("$[1].name", is("Value Portfolio")));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id} - Should return portfolio by ID")
    void shouldReturnPortfolioById() throws Exception {
        when(portfolioService.getPortfolioById(1L)).thenReturn(testPortfolio);
        when(dtoMapper.toPortfolioDTO(testPortfolio)).thenReturn(testPortfolioDTO);

        mockMvc.perform(get("/api/portfolios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Tech Portfolio")));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id} - Should return 404 when not found")
    void shouldReturn404WhenPortfolioNotFound() throws Exception {
        when(portfolioService.getPortfolioById(999L))
                .thenThrow(new ResourceNotFoundException("Portfolio", 999L));

        mockMvc.perform(get("/api/portfolios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PUT /api/portfolios/{id} - Should update portfolio")
    void shouldUpdatePortfolio() throws Exception {
        PortfolioDTO.UpdateRequest request = new PortfolioDTO.UpdateRequest();
        request.setName("Updated Portfolio");
        request.setDescription("Updated description");

        when(portfolioService.updatePortfolio(anyLong(), anyString(), anyString()))
                .thenReturn(testPortfolio);
        when(dtoMapper.toPortfolioDTO(testPortfolio)).thenReturn(testPortfolioDTO);

        mockMvc.perform(put("/api/portfolios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(portfolioService).updatePortfolio(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/portfolios/{id}/deposit - Should deposit cash")
    void shouldDepositCash() throws Exception {
        PortfolioDTO.CashOperationRequest request = new PortfolioDTO.CashOperationRequest();
        request.setAmount(new BigDecimal("1000.00"));

        Portfolio updatedPortfolio = new Portfolio();
        updatedPortfolio.setId(1L);
        updatedPortfolio.setAvailableCash(new BigDecimal("11000.00"));

        PortfolioDTO updatedDTO = new PortfolioDTO();
        updatedDTO.setId(1L);
        updatedDTO.setAvailableCash(new BigDecimal("11000.00"));

        when(portfolioService.depositCash(anyLong(), any(BigDecimal.class)))
                .thenReturn(updatedPortfolio);
        when(dtoMapper.toPortfolioDTO(updatedPortfolio)).thenReturn(updatedDTO);

        mockMvc.perform(post("/api/portfolios/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCash", is(11000.00)));
    }

    @Test
    @DisplayName("POST /api/portfolios/{id}/withdraw - Should withdraw cash")
    void shouldWithdrawCash() throws Exception {
        PortfolioDTO.CashOperationRequest request = new PortfolioDTO.CashOperationRequest();
        request.setAmount(new BigDecimal("500.00"));

        Portfolio updatedPortfolio = new Portfolio();
        updatedPortfolio.setId(1L);
        updatedPortfolio.setAvailableCash(new BigDecimal("9500.00"));

        PortfolioDTO updatedDTO = new PortfolioDTO();
        updatedDTO.setId(1L);
        updatedDTO.setAvailableCash(new BigDecimal("9500.00"));

        when(portfolioService.withdrawCash(anyLong(), any(BigDecimal.class)))
                .thenReturn(updatedPortfolio);
        when(dtoMapper.toPortfolioDTO(updatedPortfolio)).thenReturn(updatedDTO);

        mockMvc.perform(post("/api/portfolios/1/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCash", is(9500.00)));
    }

    @Test
    @DisplayName("DELETE /api/portfolios/{id} - Should delete portfolio")
    void shouldDeletePortfolio() throws Exception {
        mockMvc.perform(delete("/api/portfolios/1"))
                .andExpect(status().isNoContent());

        verify(portfolioService).deletePortfolio(1L);
    }
}