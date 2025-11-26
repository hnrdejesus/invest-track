package com.github.hnrdejesus.invest_track.dto;

import com.github.hnrdejesus.invest_track.domain.Asset;
import com.github.hnrdejesus.invest_track.domain.Portfolio;
import com.github.hnrdejesus.invest_track.domain.Position;
import com.github.hnrdejesus.invest_track.domain.Transaction;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert between entities and DTOs.
 * Keeps controller layer clean and focused.
 */
@Component
public class DTOMapper {

    /**
     * Converts Portfolio entity to DTO.
     */
    public PortfolioDTO toPortfolioDTO(Portfolio portfolio) {

        PortfolioDTO dto = new PortfolioDTO();
        dto.setId(portfolio.getId());
        dto.setName(portfolio.getName());
        dto.setDescription(portfolio.getDescription());
        dto.setTotalValue(portfolio.getTotalValue());
        dto.setAvailableCash(portfolio.getAvailableCash());
        dto.setPositionCount(portfolio.getPositions() != null ? portfolio.getPositions().size() : 0);
        dto.setCreatedAt(portfolio.getCreatedAt());
        dto.setUpdatedAt(portfolio.getUpdatedAt());
        return dto;
    }

    /**
     * Converts Asset entity to DTO.
     */
    public AssetDTO toAssetDTO(Asset asset) {

        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setTicker(asset.getTicker());
        dto.setName(asset.getName());
        dto.setAssetType(asset.getAssetType());
        dto.setCurrentPrice(asset.getCurrentPrice());
        dto.setCurrency(asset.getCurrency());
        dto.setExchange(asset.getExchange());
        dto.setActive(asset.getActive());
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());
        return dto;
    }

    /**
     * Converts Position entity to DTO with calculated fields.
     */
    public PositionDTO toPositionDTO(Position position) {

        PositionDTO dto = new PositionDTO();
        dto.setId(position.getId());
        dto.setPortfolioId(position.getPortfolio().getId());
        dto.setAsset(toAssetDTO(position.getAsset()));
        dto.setQuantity(position.getQuantity());
        dto.setAveragePrice(position.getAveragePrice());
        dto.setCurrentValue(position.getCurrentValue());
        dto.setCostBasis(position.getCostBasis());
        dto.setProfitLoss(position.getUnrealizedProfitLoss());
        dto.setProfitLossPercentage(position.getProfitLossPercentage());
        return dto;
    }

    /**
     * Converts Transaction entity to DTO.
     */
    public TransactionDTO toTransactionDTO(Transaction transaction) {

        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setPortfolioId(transaction.getPortfolio().getId());

        if (transaction.getAsset() != null) {
            dto.setAssetId(transaction.getAsset().getId());
            dto.setAssetTicker(transaction.getAsset().getTicker());
        }

        dto.setType(transaction.getType());
        dto.setQuantity(transaction.getQuantity());
        dto.setPrice(transaction.getPrice());
        dto.setTotalAmount(transaction.getTotalAmount());
        dto.setFees(transaction.getFees());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setNotes(transaction.getNotes());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}