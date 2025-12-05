/**
 * Backtesting strategy configuration.
 * Defines trading rules for historical simulation.
 */

export interface BacktestStrategy {
  strategyName: string;
  initialCapital: number;
  
  // Buy/Sell thresholds (percentages: -0.05 = -5%, 0.10 = 10%)
  buyThreshold: number;
  sellThreshold: number;
  
  // Position sizing (0.30 = 30% of portfolio)
  maxPositionSize: number;
  
  // Risk management (optional)
  stopLoss?: number;
  takeProfit?: number;
  
  // Rebalancing (0 = no rebalancing)
  rebalanceDays?: number;
}