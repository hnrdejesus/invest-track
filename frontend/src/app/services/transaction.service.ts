import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Transaction, TransactionType, PagedTransactions } from '../models/transaction';

/**
 * Transaction history service.
 * Read-only audit trail of portfolio operations.
 */
@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private readonly apiUrl = `${environment.apiUrl}/portfolios`;

  constructor(private http: HttpClient) {}

  getPortfolioTransactions(
    portfolioId: number,
    page: number = 0,
    size: number = 20
  ): Observable<PagedTransactions> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedTransactions>(
      `${this.apiUrl}/${portfolioId}/transactions`,
      { params }
    );
  }

  getTransactionById(transactionId: number): Observable<Transaction> {
    return this.http.get<Transaction>(
      `${environment.apiUrl}/transactions/${transactionId}`
    );
  }

  getRecentTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(
      `${environment.apiUrl}/transactions/recent`
    );
  }

  getAssetTransactions(portfolioId: number, assetId: number): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(
      `${this.apiUrl}/${portfolioId}/assets/${assetId}/transactions`
    );
  }

  getTransactionsByDateRange(
    portfolioId: number,
    start: Date,
    end: Date
  ): Observable<Transaction[]> {
    const params = new HttpParams()
      .set('start', start.toISOString())
      .set('end', end.toISOString());
    
    return this.http.get<Transaction[]>(
      `${this.apiUrl}/${portfolioId}/transactions/range`,
      { params }
    );
  }

  getTotalFees(portfolioId: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/${portfolioId}/transactions/fees`
    );
  }

  countByType(portfolioId: number, type: TransactionType): Observable<number> {
    const params = new HttpParams().set('type', type);
    return this.http.get<number>(
      `${this.apiUrl}/${portfolioId}/transactions/count`,
      { params }
    );
  }
}