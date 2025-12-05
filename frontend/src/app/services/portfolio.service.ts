import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { 
  Portfolio, 
  CreatePortfolioRequest, 
  UpdatePortfolioRequest,
  CashOperationRequest 
} from '../models/portfolio';

/**
 * Portfolio management service.
 * Handles CRUD operations and cash management.
 */
@Injectable({
  providedIn: 'root'
})
export class PortfolioService {
  private readonly apiUrl = `${environment.apiUrl}/portfolios`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Portfolio[]> {
    return this.http.get<Portfolio[]>(this.apiUrl);
  }

  getById(id: number): Observable<Portfolio> {
    return this.http.get<Portfolio>(`${this.apiUrl}/${id}`);
  }

  create(request: CreatePortfolioRequest): Observable<Portfolio> {
    return this.http.post<Portfolio>(this.apiUrl, request);
  }

  update(id: number, request: UpdatePortfolioRequest): Observable<Portfolio> {
    return this.http.put<Portfolio>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deposit(id: number, request: CashOperationRequest): Observable<Portfolio> {
    return this.http.post<Portfolio>(`${this.apiUrl}/${id}/deposit`, request);
  }

  withdraw(id: number, request: CashOperationRequest): Observable<Portfolio> {
    return this.http.post<Portfolio>(`${this.apiUrl}/${id}/withdraw`, request);
  }

  recalculateValue(id: number): Observable<Portfolio> {
    return this.http.post<Portfolio>(`${this.apiUrl}/${id}/recalculate`, {});
  }
}