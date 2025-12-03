package com.github.hnrdejesus.invest_track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.dto.DTOMapper;
import com.github.hnrdejesus.invest_track.dto.PositionDTO;
import com.github.hnrdejesus.invest_track.exception.ResourceNotFoundException;
import com.github.hnrdejesus.invest_track.service.PositionService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PositionController.
 * Tests buy/sell operations and portfolio composition endpoints.
 */
@WebMvcTest(PositionController.class)
@DisplayName("PositionController Tests")
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PositionService positionService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private DTOMapper dtoMapper;

    private Position testPosition;
    private PositionDTO testPositionDTO;
    private Portfolio testPortfolio;
    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Test Portfolio");

        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setTicker("AAPL");
        testAsset.setName("Apple Inc.");
        testAsset.setAssetType(AssetType.STOCK);

        testPosition = new Position();
        testPosition.setId(1L);
        testPosition.setPortfolio(testPortfolio);
        testPosition.setAsset(testAsset);
        testPosition.setQuantity(new BigDecimal("10.0"));
        testPosition.setAveragePrice(new BigDecimal("150.00"));

        testPositionDTO = new PositionDTO();
        testPositionDTO.setId(1L);
        testPositionDTO.setPortfolioId(1L);
        testPositionDTO.setQuantity(new BigDecimal("10.0"));
        testPositionDTO.setAveragePrice(new BigDecimal("150.00"));
        testPositionDTO.setCurrentValue(new BigDecimal("1500.00"));
        testPositionDTO.setCostBasis(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id}/positions - Should return all positions")
    void shouldReturnAllPositions() throws Exception {
        when(positionService.getPortfolioPositions(1L)).thenReturn(Arrays.asList(testPosition));
        when(dtoMapper.toPositionDTO(testPosition)).thenReturn(testPositionDTO);

        mockMvc.perform(get("/api/portfolios/1/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quantity", is(10.0)))
                .andExpect(jsonPath("$[0].averagePrice", is(150.00)));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id}/positions/asset/{assetId} - Should return specific position")
    void shouldReturnSpecificPosition() throws Exception {
        when(positionService.getPosition(1L, 1L)).thenReturn(testPosition);
        when(dtoMapper.toPositionDTO(testPosition)).thenReturn(testPositionDTO);

        mockMvc.perform(get("/api/portfolios/1/positions/asset/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.quantity", is(10.0)));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id}/positions/asset/{assetId} - Should return 404 when not found")
    void shouldReturn404WhenPositionNotFound() throws Exception {
        when(positionService.getPosition(1L, 999L))
                .thenThrow(new ResourceNotFoundException("Position not found"));

        mockMvc.perform(get("/api/portfolios/1/positions/asset/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/portfolios/{id}/positions/buy - Should buy asset successfully")
    void shouldBuyAssetSuccessfully() throws Exception {
        PositionDTO.TradeRequest request = new PositionDTO.TradeRequest();
        request.setAssetId(1L);
        request.setQuantity(new BigDecimal("5.0"));
        request.setPrice(new BigDecimal("150.00"));
        request.setFees(new BigDecimal("5.00"));

        when(positionService.buyAsset(anyLong(), anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(testPosition);
        when(dtoMapper.toPositionDTO(testPosition)).thenReturn(testPositionDTO);

        mockMvc.perform(post("/api/portfolios/1/positions/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.quantity", is(10.0)));

        // Verify both position update and transaction recording
        verify(positionService).buyAsset(eq(1L), eq(1L), any(BigDecimal.class), any(BigDecimal.class));
        verify(transactionService).recordBuyTransaction(eq(1L), eq(1L), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    @DisplayName("POST /api/portfolios/{id}/positions/buy - Should return 400 for invalid request")
    void shouldReturn400ForInvalidBuyRequest() throws Exception {
        PositionDTO.TradeRequest request = new PositionDTO.TradeRequest();
        // Missing required fields (assetId, quantity, price)

        mockMvc.perform(post("/api/portfolios/1/positions/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/portfolios/{id}/positions/sell - Should sell asset and reduce position")
    void shouldSellAssetAndReducePosition() throws Exception {
        PositionDTO.TradeRequest request = new PositionDTO.TradeRequest();
        request.setAssetId(1L);
        request.setQuantity(new BigDecimal("5.0"));  // Selling half
        request.setPrice(new BigDecimal("160.00"));
        request.setFees(new BigDecimal("5.00"));

        Position reducedPosition = new Position();
        reducedPosition.setQuantity(new BigDecimal("5.0"));  // Remaining: 10 - 5 = 5

        PositionDTO reducedDTO = new PositionDTO();
        reducedDTO.setQuantity(new BigDecimal("5.0"));

        when(positionService.sellAsset(anyLong(), anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(reducedPosition);
        when(dtoMapper.toPositionDTO(reducedPosition)).thenReturn(reducedDTO);

        mockMvc.perform(post("/api/portfolios/1/positions/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5.0)));

        verify(positionService).sellAsset(eq(1L), eq(1L), any(BigDecimal.class), any(BigDecimal.class));
        verify(transactionService).recordSellTransaction(eq(1L), eq(1L), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    @DisplayName("POST /api/portfolios/{id}/positions/sell - Should return 204 when position closed")
    void shouldReturn204WhenPositionClosed() throws Exception {
        PositionDTO.TradeRequest request = new PositionDTO.TradeRequest();
        request.setAssetId(1L);
        request.setQuantity(new BigDecimal("10.0"));  // Selling all shares
        request.setPrice(new BigDecimal("160.00"));
        request.setFees(BigDecimal.ZERO);

        // Position is null when completely sold
        when(positionService.sellAsset(anyLong(), anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/portfolios/1/positions/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(transactionService).recordSellTransaction(eq(1L), eq(1L), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    @DisplayName("DELETE /api/portfolios/{id}/positions/asset/{assetId} - Should close position")
    void shouldClosePosition() throws Exception {
        when(positionService.getPosition(1L, 1L)).thenReturn(testPosition);

        mockMvc.perform(delete("/api/portfolios/1/positions/asset/1")
                        .param("sellPrice", "160.00"))
                .andExpect(status().isNoContent());

        // Verify position closed and transaction recorded with full quantity
        verify(positionService).closePosition(eq(1L), eq(1L), any(BigDecimal.class));
        verify(transactionService).recordSellTransaction(eq(1L), eq(1L),
                eq(testPosition.getQuantity()), any(BigDecimal.class), eq(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id}/positions/total-investment - Should return total investment")
    void shouldReturnTotalInvestment() throws Exception {
        when(positionService.calculateTotalInvestment(1L))
                .thenReturn(new BigDecimal("15000.00"));

        mockMvc.perform(get("/api/portfolios/1/positions/total-investment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(15000.00)));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id}/positions/count - Should return position count")
    void shouldReturnPositionCount() throws Exception {
        when(positionService.countPositions(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/portfolios/1/positions/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(5)));
    }

    @Test
    @DisplayName("POST /api/portfolios/{id}/positions/sell - Should handle insufficient quantity")
    void shouldHandleInsufficientQuantity() throws Exception {
        PositionDTO.TradeRequest request = new PositionDTO.TradeRequest();
        request.setAssetId(1L);
        request.setQuantity(new BigDecimal("100.0"));  // Trying to sell more than owned
        request.setPrice(new BigDecimal("160.00"));

        when(positionService.sellAsset(anyLong(), anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Insufficient quantity"));

        mockMvc.perform(post("/api/portfolios/1/positions/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}