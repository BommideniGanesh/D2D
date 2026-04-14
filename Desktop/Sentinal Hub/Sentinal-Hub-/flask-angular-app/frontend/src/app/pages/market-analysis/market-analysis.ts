import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarketService } from '../../services/market';
import { NASDAQ_100 } from '../../utils/nasdaq100';

@Component({
  selector: 'app-market-analysis',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './market-analysis.html',
  styleUrl: './market-analysis.css'
})
export class MarketAnalysis {
  private marketService = inject(MarketService);

  popularStocks = NASDAQ_100;

  selectedTicker = 'AAPL';
  customTicker = '';
  
  isLoading = false;
  error = '';
  newsResults: any[] = [];
  
  openPanels: { [key: string]: boolean } = {};

  searchTarget() {
    return this.customTicker.trim().toUpperCase() || this.selectedTicker;
  }

  onSearch() {
    const target = this.searchTarget();
    if (!target) return;
    
    // Save to global state so it syncs with Sentiment Feed
    this.marketService.setTicker(target);
    
    this.isLoading = true;
    this.error = '';
    this.newsResults = [];
    
    this.marketService.getNews(target).subscribe({
      next: (data) => {
        this.newsResults = data;
        this.isLoading = false;
        
        // Open the first panel that has articles
        for(let result of data) {
           if(result.articles.length > 0) {
               this.openPanels[result.source] = true;
               break;
           }
        }
      },
      error: (err) => {
        this.error = "Failed to fetch market data. Try again later.";
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  togglePanel(source: string) {
    this.openPanels[source] = !this.openPanels[source];
  }
}
