package com.github.hnrdejesus.invest_track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.AssetType;
import com.github.hnrdejesus.invest_track.dto.AssetDTO;
import com.github.hnrdejesus.invest_track.dto.DTOMapper;
import com.github.hnrdejesus.invest_track.exception.ResourceNotFoundException;
import com.github.hnrdejesus.invest_track.service.AssetService;
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
 * Integration tests for AssetController.
 * Tests REST endpoints with MockMvc and validates HTTP responses.
 */
@WebMvcTest(AssetController.class)
@DisplayName("AssetController Tests")
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssetService assetService;

    @MockitoBean
    private DTOMapper dtoMapper;

    private Asset testAsset;
    private AssetDTO testAssetDTO;

    @BeforeEach
    void setUp() {
        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setTicker("AAPL");
        testAsset.setName("Apple Inc.");
        testAsset.setAssetType(AssetType.STOCK);
        testAsset.setCurrency("USD");
        testAsset.setExchange("NASDAQ");
        testAsset.setCurrentPrice(new BigDecimal("150.50"));
        testAsset.setActive(true);

        testAssetDTO = new AssetDTO();
        testAssetDTO.setId(1L);
        testAssetDTO.setTicker("AAPL");
        testAssetDTO.setName("Apple Inc.");
        testAssetDTO.setAssetType(AssetType.STOCK);
        testAssetDTO.setCurrency("USD");
        testAssetDTO.setExchange("NASDAQ");
        testAssetDTO.setCurrentPrice(new BigDecimal("150.50"));
        testAssetDTO.setActive(true);
    }

    @Test
    @DisplayName("POST /api/assets - Should create asset")
    void shouldCreateAsset() throws Exception {
        AssetDTO.CreateRequest request = new AssetDTO.CreateRequest();
        request.setTicker("GOOGL");
        request.setName("Alphabet Inc.");
        request.setAssetType(AssetType.STOCK);
        request.setCurrency("USD");
        request.setExchange("NASDAQ");

        when(assetService.createAsset(anyString(), anyString(), any(AssetType.class), anyString(), anyString()))
                .thenReturn(testAsset);
        when(dtoMapper.toAssetDTO(any(Asset.class))).thenReturn(testAssetDTO);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ticker", is("AAPL")))
                .andExpect(jsonPath("$.name", is("Apple Inc.")));

        verify(assetService).createAsset(anyString(), anyString(), any(AssetType.class), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/assets - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        AssetDTO.CreateRequest request = new AssetDTO.CreateRequest();
        request.setTicker("A");  // Too short
        request.setName("");     // Empty
        request.setAssetType(null);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/assets - Should return all assets")
    void shouldReturnAllAssets() throws Exception {
        Asset asset2 = new Asset();
        asset2.setId(2L);
        asset2.setTicker("MSFT");
        asset2.setName("Microsoft Corp.");
        asset2.setAssetType(AssetType.STOCK);

        AssetDTO dto2 = new AssetDTO();
        dto2.setId(2L);
        dto2.setTicker("MSFT");
        dto2.setName("Microsoft Corp.");

        when(assetService.getAllAssets()).thenReturn(Arrays.asList(testAsset, asset2));
        when(dtoMapper.toAssetDTO(testAsset)).thenReturn(testAssetDTO);
        when(dtoMapper.toAssetDTO(asset2)).thenReturn(dto2);

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ticker", is("AAPL")))
                .andExpect(jsonPath("$[1].ticker", is("MSFT")));
    }

    @Test
    @DisplayName("GET /api/assets/active - Should return only active assets")
    void shouldReturnOnlyActiveAssets() throws Exception {
        when(assetService.getActiveAssets()).thenReturn(Arrays.asList(testAsset));
        when(dtoMapper.toAssetDTO(testAsset)).thenReturn(testAssetDTO);

        mockMvc.perform(get("/api/assets/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticker", is("AAPL")))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    @DisplayName("GET /api/assets/{id} - Should return asset by ID")
    void shouldReturnAssetById() throws Exception {
        when(assetService.getAssetById(1L)).thenReturn(testAsset);
        when(dtoMapper.toAssetDTO(testAsset)).thenReturn(testAssetDTO);

        mockMvc.perform(get("/api/assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ticker", is("AAPL")));
    }

    @Test
    @DisplayName("GET /api/assets/{id} - Should return 404 when not found")
    void shouldReturn404WhenAssetNotFound() throws Exception {
        when(assetService.getAssetById(999L))
                .thenThrow(new ResourceNotFoundException("Asset", 999L));

        mockMvc.perform(get("/api/assets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/assets/ticker/{ticker} - Should return asset by ticker")
    void shouldReturnAssetByTicker() throws Exception {
        when(assetService.getAssetByTicker("AAPL")).thenReturn(testAsset);
        when(dtoMapper.toAssetDTO(testAsset)).thenReturn(testAssetDTO);

        mockMvc.perform(get("/api/assets/ticker/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker", is("AAPL")))
                .andExpect(jsonPath("$.name", is("Apple Inc.")));
    }

    @Test
    @DisplayName("GET /api/assets/search - Should search assets by query")
    void shouldSearchAssets() throws Exception {
        when(assetService.searchAssets("app")).thenReturn(Arrays.asList(testAsset));
        when(dtoMapper.toAssetDTO(testAsset)).thenReturn(testAssetDTO);

        mockMvc.perform(get("/api/assets/search?q=app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticker", is("AAPL")));
    }

    @Test
    @DisplayName("GET /api/assets/type/{type} - Should filter assets by type")
    void shouldFilterAssetsByType() throws Exception {
        when(assetService.getAssetsByType(AssetType.STOCK))
                .thenReturn(Arrays.asList(testAsset));
        when(dtoMapper.toAssetDTO(testAsset)).thenReturn(testAssetDTO);

        mockMvc.perform(get("/api/assets/type/STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assetType", is("STOCK")));
    }

    @Test
    @DisplayName("PUT /api/assets/{id} - Should update asset info")
    void shouldUpdateAssetInfo() throws Exception {
        AssetDTO.UpdateRequest request = new AssetDTO.UpdateRequest();
        request.setName("Apple Inc. (Updated)");
        request.setExchange("NASDAQ-GS");

        when(assetService.updateAssetInfo(anyLong(), anyString(), anyString()))
                .thenReturn(testAsset);
        when(dtoMapper.toAssetDTO(testAsset)).thenReturn(testAssetDTO);

        mockMvc.perform(put("/api/assets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(assetService).updateAssetInfo(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/assets/{id}/deactivate - Should deactivate asset")
    void shouldDeactivateAsset() throws Exception {
        mockMvc.perform(post("/api/assets/1/deactivate"))
                .andExpect(status().isNoContent());

        verify(assetService).deactivateAsset(1L);
    }

    @Test
    @DisplayName("POST /api/assets/{id}/reactivate - Should reactivate asset")
    void shouldReactivateAsset() throws Exception {
        mockMvc.perform(post("/api/assets/1/reactivate"))
                .andExpect(status().isNoContent());

        verify(assetService).reactivateAsset(1L);
    }
}