import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { 
  Asset, 
  AssetType, 
  CreateAssetRequest, 
  UpdateAssetRequest 
} from '../models/asset';

/**
 * Asset catalog service.
 * Manages financial instruments (stocks, ETFs, etc).
 */
@Injectable({
  providedIn: 'root'
})
export class AssetService {
  private readonly apiUrl = `${environment.apiUrl}/assets`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Asset[]> {
    return this.http.get<Asset[]>(this.apiUrl);
  }

  getActive(): Observable<Asset[]> {
    return this.http.get<Asset[]>(`${this.apiUrl}/active`);
  }

  getById(id: number): Observable<Asset> {
    return this.http.get<Asset>(`${this.apiUrl}/${id}`);
  }

  getByTicker(ticker: string): Observable<Asset> {
    return this.http.get<Asset>(`${this.apiUrl}/ticker/${ticker}`);
  }

  getByType(type: AssetType): Observable<Asset[]> {
    return this.http.get<Asset[]>(`${this.apiUrl}/type/${type}`);
  }

  search(query: string): Observable<Asset[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Asset[]>(`${this.apiUrl}/search`, { params });
  }

  create(request: CreateAssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.apiUrl, request);
  }

  update(id: number, request: UpdateAssetRequest): Observable<Asset> {
    return this.http.put<Asset>(`${this.apiUrl}/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  reactivate(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/reactivate`, {});
  }
}