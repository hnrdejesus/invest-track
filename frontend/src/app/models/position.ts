/**
 * Position domain models.
 * Represents holdings of an asset within a portfolio.
 */

import { Asset } from './asset';

export interface Position {
  id?: number;
  portfolioId: number;
  asset: Asset;
  quantity: number;
  averagePrice: number;
  currentValue: number;
  costBasis: number;
  profitLoss: number;
  profitLossPercentage: number;
}

export interface TradeRequest {
  assetId: number;
  quantity: number;
  price: number;
  fees?: number;
}