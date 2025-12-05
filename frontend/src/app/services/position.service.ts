import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Position, TradeRequest } from '../models/position';

/**
 * Position management service.
 * Handles buy/sell operations and portfolio composition.
 */
@Injectable({
  providedIn: 'root'
})
export class PositionService {
  private readonly apiUrl = `${environment.apiUrl}/portfolios`;

  constructor(private http: HttpClient) {}

  getPortfolioPositions(portfolioId: number): Observable<Position[]> {
    return this.http.get<Position[]>(`${this.apiUrl}/${portfolioId}/positions`);
  }

  getPosition(portfolioId: number, assetId: number): Observable<Position> {
    return this.http.get<Position>(
      `${this.apiUrl}/${portfolioId}/positions/asset/${assetId}`
    );
  }

  buy(portfolioId: number, request: TradeRequest): Observable<Position> {
    return this.http.post<Position>(
      `${this.apiUrl}/${portfolioId}/positions/buy`,
      request
    );
  }

  sell(portfolioId: number, request: TradeRequest): Observable<Position> {
    return this.http.post<Position>(
      `${this.apiUrl}/${portfolioId}/positions/sell`,
      request
    );
  }

  closePosition(portfolioId: number, assetId: number, sellPrice: number): Observable<void> {
    const params = new HttpParams().set('sellPrice', sellPrice.toString());
    return this.http.delete<void>(
      `${this.apiUrl}/${portfolioId}/positions/asset/${assetId}`,
      { params }
    );
  }

  getTotalInvestment(portfolioId: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/${portfolioId}/positions/total-investment`
    );
  }

  countPositions(portfolioId: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/${portfolioId}/positions/count`
    );
  }
}