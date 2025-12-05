/**
 * Backtesting results.
 * Strategy performance vs buy-and-hold comparison.
 */

export interface DailyValue {
  date: Date;
  value: number;
}

export interface BacktestResult {
  strategyName: string;
  portfolioId: number;
  
  // Time period
  startDate: Date;
  endDate: Date;
  totalDays: number;
  
  // Capital
  initialCapital: number;
  finalCapital: number;
  totalReturn: number;
  totalReturnPercentage: number;
  
  // Performance metrics
  sharpeRatio: number;
  maxDrawdown: number;
  volatility: number;
  
  // Trading statistics
  totalTrades: number;
  winningTrades: number;
  losingTrades: number;
  winRate: number;
  avgWin: number;
  avgLoss: number;
  profitFactor: number;
  
  // Comparison with buy-and-hold
  buyAndHoldReturn: number;
  strategyVsBuyAndHold: number;
  
  // Daily portfolio values for charting
  portfolioHistory: DailyValue[];
  
  calculatedAt: string;
}