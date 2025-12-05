import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PortfolioMetrics } from '../models/portfolio-metrics';

/**
 * Portfolio metrics service.
 * Financial performance and risk analysis.
 */
@Injectable({
  providedIn: 'root'
})
export class MetricsService {
  private readonly apiUrl = `${environment.apiUrl}/portfolios`;

  constructor(private http: HttpClient) {}

  getMetrics(portfolioId: number): Observable<PortfolioMetrics> {
    return this.http.get<PortfolioMetrics>(
      `${this.apiUrl}/${portfolioId}/metrics`
    );
  }

  getSharpeRatio(portfolioId: number, riskFreeRate: number = 0.02): Observable<number> {
    const params = new HttpParams().set('riskFreeRate', riskFreeRate.toString());
    return this.http.get<number>(
      `${this.apiUrl}/${portfolioId}/metrics/sharpe-ratio`,
      { params }
    );
  }

  getVolatility(portfolioId: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/${portfolioId}/metrics/volatility`
    );
  }

  getMaxDrawdown(portfolioId: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/${portfolioId}/metrics/max-drawdown`
    );
  }

  getTotalReturn(portfolioId: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/${portfolioId}/metrics/total-return`
    );
  }
}