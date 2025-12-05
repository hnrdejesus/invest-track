/**
 * Asset domain models.
 * Represents financial instruments (stocks, ETFs, etc).
 */

export enum AssetType {
  STOCK = 'STOCK',
  ETF = 'ETF',
  REIT = 'REIT',
  CRYPTO = 'CRYPTO',
  BOND = 'BOND',
  MUTUAL_FUND = 'MUTUAL_FUND',
  COMMODITY = 'COMMODITY',
  OTHER = 'OTHER'
}

export interface Asset {
  id?: number;
  ticker: string;
  name: string;
  assetType: AssetType;
  currentPrice?: number;
  currency: string;
  exchange?: string;
  active: boolean;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface CreateAssetRequest {
  ticker: string;
  name: string;
  assetType: AssetType;
  currency: string;
  exchange?: string;
}

export interface UpdateAssetRequest {
  name: string;
  exchange?: string;
}