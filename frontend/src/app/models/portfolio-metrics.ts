/**
 * Portfolio financial metrics.
 * Risk-adjusted returns and performance analysis.
 */

export interface PortfolioMetrics {
  portfolioId: number;
  portfolioName: string;
  
  // Value metrics
  totalValue: number;
  totalCost: number;
  totalProfitLoss: number;
  totalReturn: number;
  
  // Risk metrics
  sharpeRatio: number;
  volatility: number;
  maxDrawdown: number;
  
  // Performance metrics
  winRate: number;
  totalPositions: number;
  profitablePositions: number;
  losingPositions: number;
  
  // Individual position metrics
  bestPerformer: number;
  worstPerformer: number;
  
  calculatedAt: string;
}