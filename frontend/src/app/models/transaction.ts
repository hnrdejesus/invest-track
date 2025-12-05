/**
 * Transaction domain models.
 * Immutable audit trail of portfolio operations.
 */

export enum TransactionType {
  BUY = 'BUY',
  SELL = 'SELL',
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  DIVIDEND = 'DIVIDEND'
}

export interface Transaction {
  id?: number;
  portfolioId: number;
  assetId?: number;
  assetTicker?: string;
  type: TransactionType;
  quantity?: number;
  price?: number;
  totalAmount: number;
  fees?: number;
  transactionDate: Date;
  notes?: string;
  createdAt?: Date;
}

export interface PagedTransactions {
  content: Transaction[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}