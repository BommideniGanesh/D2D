import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

export interface DashboardData {
  ticker: string;
  generated_at: string;
  quote: {
    open: number; high: number; low: number; close: number;
    volume: number; prev_close: number; change: number; change_pct: number;
  };
  sentiment_summary: {
    positive_count: number; negative_count: number; neutral_count: number;
    total: number; avg_confidence: number;
    positive_pct: number; negative_pct: number; neutral_pct: number;
    overall: string;
  };
  structured: any[];
  historical: any[];
}

@Injectable({
  providedIn: 'root'
})
export class MarketService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:5000/api/market';

  private currentTicker = 'AAPL'; // Shared state

  private getHeaders() {
    const token = localStorage.getItem('access_token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  setTicker(ticker: string) {
    this.currentTicker = ticker;
  }

  getTicker() {
    return this.currentTicker;
  }

  getNews(ticker: string) {
    return this.http.get<any[]>(`${this.apiUrl}/news/${ticker}`, { headers: this.getHeaders() });
  }

  getDashboard(ticker: string) {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/${ticker}`, { headers: this.getHeaders() });
  }
}
