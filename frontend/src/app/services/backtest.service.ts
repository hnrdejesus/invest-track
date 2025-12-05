import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { BacktestStrategy } from '../models/backtest-strategy';
import { BacktestResult } from '../models/backtest-result';

/**
 * Backtesting service.
 * Tests trading strategies on historical data.
 */
@Injectable({
  providedIn: 'root'
})
export class BacktestService {
  private readonly apiUrl = `${environment.apiUrl}/portfolios`;

  constructor(private http: HttpClient) {}

  runBacktest(
    portfolioId: number,
    strategy: BacktestStrategy,
    days?: number
  ): Observable<BacktestResult> {
    let params = new HttpParams();
    
    if (days) {
      params = params.set('days', days.toString());
    }
    
    return this.http.post<BacktestResult>(
      `${this.apiUrl}/${portfolioId}/backtest`,
      strategy,
      { params }
    );
  }
}