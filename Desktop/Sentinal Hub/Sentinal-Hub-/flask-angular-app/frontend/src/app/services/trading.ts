import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TradingService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:5000/api/trading';

  private getHeaders() {
    const token = localStorage.getItem('access_token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getCredentials() {
    return this.http.get<any>(`${this.apiUrl}/credentials`, { headers: this.getHeaders() });
  }

  saveCredentials(data: any) {
    return this.http.post<any>(`${this.apiUrl}/credentials`, data, { headers: this.getHeaders() });
  }

  testConnection() {
    return this.http.get<any>(`${this.apiUrl}/connect`, { headers: this.getHeaders() });
  }

  getTradeHistory() {
    return this.http.get<any>(`${this.apiUrl}/history`, { headers: this.getHeaders() });
  }

  getHistoricalRates(ticker: string) {
    return this.http.get<any>(`${this.apiUrl}/rates/${ticker}`, { headers: this.getHeaders() });
  }

  executeTrade(ticker: string, action: string, volume: number = 1.0, rr_ratio: number = 2.0, risk_pips: number = 100) {
    return this.http.post<any>(`${this.apiUrl}/execute`, { ticker, action, volume, rr_ratio, risk_pips }, { headers: this.getHeaders() });
  }

  getMlPredictions(ticker: string) {
    return this.http.get<any>(`${this.apiUrl}/ml/predict/${ticker}`, { headers: this.getHeaders() });
  }

  getOpenPositions() {
    return this.http.get<any>(`${this.apiUrl}/positions`, { headers: this.getHeaders() });
  }

  closePosition(ticket: number) {
    return this.http.post<any>(`${this.apiUrl}/close`, { ticket }, { headers: this.getHeaders() });
  }
}
