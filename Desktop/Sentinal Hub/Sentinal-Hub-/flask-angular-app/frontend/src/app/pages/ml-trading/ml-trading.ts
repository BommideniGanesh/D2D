import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TradingService } from '../../services/trading';
import { MarketService } from '../../services/market';

@Component({
  selector: 'app-ml-trading',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ml-trading.html',
  styleUrl: './ml-trading.css'
})
export class MlTrading implements OnInit {
  private tradingService = inject(TradingService);
  private marketService = inject(MarketService);

  ticker: string = 'AAPL';
  volume: number = 1.0;
  rrRatio: string = '1:2';
  suggestedTp: number = 0;
  suggestedSl: number = 0;
  
  isLoading: boolean = false;
  isExecuting: boolean = false;
  isRefreshingBalance: boolean = false;
  currentAction: string = '';
  
  predictionData: any = null;
  accountData: any = null;
  error: string | null = null;
  tradeStatus: string | null = null;

  ngOnInit() {
    this.ticker = this.marketService.getTicker() || 'AAPL';
    this.loadPredictions();
    this.fetchAccountInfo();
  }

  fetchAccountInfo() {
    this.isRefreshingBalance = true;
    this.tradingService.testConnection().subscribe({
      next: (res) => {
        this.accountData = res.data;
        this.isRefreshingBalance = false;
      },
      error: () => {
        this.isRefreshingBalance = false;
      }
    });
  }

  calculateRiskReward() {
    if (!this.predictionData || !this.predictionData.historical_model) return;
    
    const hist = this.predictionData.historical_model;
    const currentPrice = hist.current_price;
    const predictedClose = hist.predicted_close;
    
    if (!currentPrice || !predictedClose) return;

    // Based on predicted close, determine the "Reward" margin
    const rewardMargin = Math.abs(predictedClose - currentPrice);
    
    // Parse RR Ratio, e.g. "1:2" -> Risk=1, Reward=2
    const parts = this.rrRatio.split(':');
    const riskFactor = parseFloat(parts[0]);
    const rewardFactor = parseFloat(parts[1]);
    
    const riskMargin = rewardMargin * (riskFactor / rewardFactor);

    if (hist.signal === 'BUY') {
      this.suggestedTp = currentPrice + rewardMargin;
      this.suggestedSl = currentPrice - riskMargin;
    } else if (hist.signal === 'SELL') {
      this.suggestedTp = currentPrice - rewardMargin;
      this.suggestedSl = currentPrice + riskMargin;
    }
  }

  loadPredictions() {
    if (!this.ticker) return;
    
    this.isLoading = true;
    this.error = null;
    this.predictionData = null;
    
    this.tradingService.getMlPredictions(this.ticker).subscribe({
      next: (data) => {
        this.predictionData = data;
        this.calculateRiskReward();
        this.isLoading = false;
      },
      error: (err) => {
        this.error = "Failed to load ML predictions. Ensure MT5 is connected and backend is running.";
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  executeTrade(action: string) {
    if (!this.ticker || !this.volume) return;
    
    this.isExecuting = true;
    this.currentAction = action;
    this.tradeStatus = null;

    // Parse rr_ratio numerically from rrRatio string (e.g. "1:2" → 2.0)
    const parts = this.rrRatio.split(':');
    const rrNum = parts.length === 2 ? parseFloat(parts[1]) / parseFloat(parts[0]) : 2.0;
    
    this.tradingService.executeTrade(this.ticker, action, this.volume, rrNum, 100).subscribe({
      next: (res) => {
        this.tradeStatus = res.message || `Successfully executed ${action}.`;
        this.isExecuting = false;
        this.currentAction = '';
        this.fetchAccountInfo();
        setTimeout(() => this.tradeStatus = null, 8000);
      },
      error: (err) => {
        this.tradeStatus = err.error?.message || `Failed to execute ${action}.`;
        this.isExecuting = false;
        this.currentAction = '';
      }
    });
  }
}
