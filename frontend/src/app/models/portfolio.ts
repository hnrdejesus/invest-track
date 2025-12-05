/**
 * Portfolio domain models.
 * Represents investment portfolio with cash and positions.
 */

export interface Portfolio {
  id?: number;
  name: string;
  description?: string;
  totalValue: number;
  availableCash: number;
  positionCount: number;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface CreatePortfolioRequest {
  name: string;
  description?: string;
  initialCash: number;
}

export interface UpdatePortfolioRequest {
  name: string;
  description?: string;
}

export interface CashOperationRequest {
  amount: number;
  notes?: string;
}