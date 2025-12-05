import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MonteCarloSimulation } from '../models/monte-carlo-simulation';

/**
 * Monte Carlo simulation service.
 * Probabilistic portfolio projections.
 */
@Injectable({
  providedIn: 'root'
})
export class MonteCarloService {
  private readonly apiUrl = `${environment.apiUrl}/portfolios`;

  constructor(private http: HttpClient) {}

  runSimulation(
    portfolioId: number,
    iterations?: number,
    days?: number
  ): Observable<MonteCarloSimulation> {
    let params = new HttpParams();
    
    if (iterations) {
      params = params.set('iterations', iterations.toString());
    }
    if (days) {
      params = params.set('days', days.toString());
    }
    
    return this.http.get<MonteCarloSimulation>(
      `${this.apiUrl}/${portfolioId}/monte-carlo/simulate`,
      { params }
    );
  }
}