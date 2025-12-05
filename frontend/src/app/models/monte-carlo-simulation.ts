/**
 * Monte Carlo simulation results.
 * Probabilistic projections based on historical performance.
 */

export interface MonteCarloSimulation {
  portfolioId: number;
  iterations: number;
  daysProjected: number;
  
  // Current state
  initialValue: number;
  
  // Statistical projections
  expectedValue: number;
  medianValue: number;
  bestCase: number;  // 95th percentile
  worstCase: number; // 5th percentile
  
  // Confidence intervals
  percentile90High: number;
  percentile90Low: number;
  percentile50High: number;
  percentile50Low: number;
  
  // Risk metrics
  probabilityOfLoss: number;
  probabilityOfDoubling: number;
  
  // Historical data used
  historicalReturn: number;
  historicalVolatility: number;
  
  // Distribution data for charting (limited to 100 for performance)
  simulationResults: number[];
  
  calculatedAt: string;
}