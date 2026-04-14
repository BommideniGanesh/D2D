import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SentimentService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:5000/api/sentiment';

  private getHeaders() {
    const token = localStorage.getItem('access_token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getSentimentAnalysis(ticker: string) {
    return this.http.get<{raw: any[], structured: any[]}>(`${this.apiUrl}/${ticker}`, { headers: this.getHeaders() });
  }
}
