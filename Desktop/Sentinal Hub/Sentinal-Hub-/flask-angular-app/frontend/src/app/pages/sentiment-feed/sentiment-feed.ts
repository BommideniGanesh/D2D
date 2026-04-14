import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SentimentService } from '../../services/sentiment';
import { MarketService, DashboardData } from '../../services/market';
import { TradingService } from '../../services/trading';
import { NASDAQ_100 } from '../../utils/nasdaq100';

@Component({
  selector: 'app-sentiment-feed',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sentiment-feed.html',
  styleUrl: './sentiment-feed.css'
})
export class SentimentFeed implements OnInit {
  private sentimentService = inject(SentimentService);
  private marketService    = inject(MarketService);
  private tradingService   = inject(TradingService);

  // Stock picker
  popularStocks   = NASDAQ_100;
  selectedTicker  = 'AAPL';
  customTicker    = '';

  // Active ticker shown in headers
  currentTicker = '';

  // Loading / error states
  isLoading        = false;
  isHistoryLoading = false;
  isDashboardLoading = false;
  error         = '';
  historyError  = '';
  dashboardError = '';

  // Trade execution panel
  tradeLotSize   = 1.0;
  isTrading      = false;
  tradeMessage   = '';
  tradeSuccess   = false;

  // ML Prediction cards
  predictionData: any   = null;
  isPredicting          = false;
  predictionError       = '';

  // Tab state
  activeTab: 'dashboard' | 'structured' | 'raw' | 'historical' | 'powerbi' = 'dashboard';

  // Data
  rawData:        any[]         = [];
  structuredData: any[]         = [];
  historicalData: any[]         = [];
  dashboardData:  DashboardData | null = null;

  readonly apiBase = 'http://localhost:5000/api/powerbi';

  get powerbiNewsUrl(): string {
    return `${this.apiBase}/news-features/${this.currentTicker}`;
  }

  get powerbiHistoricalUrls(): { label: string; url: string }[] {
    const base = `${this.apiBase}/historical/${this.currentTicker}`;
    return [
      { label: '5-Min  (7 days)',   url: `${base}?tf=M5&days=7`   },
      { label: '15-Min (14 days)',  url: `${base}?tf=M15&days=14` },
      { label: 'Hourly (30 days)',  url: `${base}?tf=H1&days=30`  },
      { label: 'Daily  (365 days)', url: `${base}?tf=D1&days=365` },
    ];
  }

  /** Top-5 most confident headlines for the dashboard tab */
  get topHeadlines(): any[] {
    return (this.dashboardData?.structured ?? []).slice(0, 5);
  }

  ngOnInit() {
    this.selectedTicker = this.marketService.getTicker() || 'AAPL';
    this.onAnalyze();
  }

  searchTarget(): string {
    return this.customTicker.trim().toUpperCase() || this.selectedTicker;
  }

  onAnalyze() {
    const target = this.searchTarget();
    if (!target) return;
    this.currentTicker = target;
    this.marketService.setTicker(target);

    this._fetchNltk(target);
    this._fetchDashboard(target);
    this._fetchMt5History(target);
    this._fetchPredictions(target);
  }

  private _fetchNltk(ticker: string) {
    this.isLoading = true;
    this.error = '';
    this.sentimentService.getSentimentAnalysis(ticker).subscribe({
      next: (res) => {
        this.rawData       = res.raw;
        this.structuredData = res.structured;
        this.isLoading     = false;
      },
      error: (err) => {
        this.error     = 'Error executing NLP algorithms. See console.';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  private _fetchDashboard(ticker: string) {
    this.isDashboardLoading = true;
    this.dashboardError     = '';
    this.dashboardData      = null;
    this.marketService.getDashboard(ticker).subscribe({
      next: (res) => {
        this.dashboardData      = res;
        this.isDashboardLoading = false;
      },
      error: (err) => {
        this.dashboardError     = err.error?.message || 'Failed to load dashboard data.';
        this.isDashboardLoading = false;
        console.error('Dashboard Error:', err);
      }
    });
  }

  private _fetchMt5History(ticker: string) {
    this.isHistoryLoading = true;
    this.historyError    = '';
    this.tradingService.getHistoricalRates(ticker).subscribe({
      next: (res) => {
        this.historicalData   = res.data || [];
        this.isHistoryLoading = false;
      },
      error: (err) => {
        this.historyError    = err.error?.message || 'Failed to establish MT5 terminal rates.';
        this.isHistoryLoading = false;
        console.error('MT5 History Error:', err);
      }
    });
  }

  setTab(tab: 'dashboard' | 'structured' | 'raw' | 'historical' | 'powerbi') {
    this.activeTab = tab;
  }

  private _fetchPredictions(ticker: string) {
    this.isPredicting    = true;
    this.predictionError = '';
    this.predictionData  = null;
    this.tradingService.getMlPredictions(ticker).subscribe({
      next: (data) => {
        this.predictionData = data;
        this.isPredicting   = false;
      },
      error: (err) => {
        this.predictionError = err.error?.message || 'ML predictions unavailable. Ensure MT5 is connected.';
        this.isPredicting    = false;
      }
    });
  }

  executeTrade(action: 'BUY' | 'SELL') {
    const ticker = this.currentTicker;
    if (!ticker) {
      this.tradeMessage = 'Please fetch data for a ticker first.';
      this.tradeSuccess = false;
      return;
    }
    const volume = Math.max(1.0, Number(this.tradeLotSize) || 1.0);
    this.isTrading    = true;
    this.tradeMessage = '';
    this.tradingService.executeTrade(ticker, action, volume, 2.0, 100).subscribe({
      next: (res) => {
        this.tradeMessage = res.message;
        this.tradeSuccess = true;
        this.isTrading    = false;
      },
      error: (err) => {
        this.tradeMessage = err.error?.message || `${action} order failed. Check MT5 terminal.`;
        this.tradeSuccess = false;
        this.isTrading    = false;
      }
    });
  }
}
