import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TradingService } from '../../services/trading';

@Component({
  selector: 'app-trade-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './trade-history.html',
  styleUrl: './trade-history.css',
})
export class TradeHistory implements OnInit, OnDestroy {
  tradingService = inject(TradingService);

  // ── Trade history ──────────────────────────────────────────────
  trades: any[] = [];
  loading = true;
  error = '';
  accountData: any = null;

  // ── Open positions ─────────────────────────────────────────────
  positions: any[] = [];
  positionsLoading = false;
  positionsError   = '';
  closingTicket: number | null = null;
  closeMessage = '';
  closeSuccess = false;

  private refreshInterval: any = null;

  // ── Tabs ───────────────────────────────────────────────────────
  activeTab: 'positions' | 'history' = 'positions';

  setTab(tab: 'positions' | 'history') {
    this.activeTab = tab;
  }

  get totalRealizedProfit(): number {
    return this.trades
      .filter(t => t.type <= 1)
      .reduce((sum, t) => sum + (t.profit || 0), 0);
  }

  get totalFloatingPnl(): number {
    return this.positions.reduce((sum, p) => sum + (p.profit || 0), 0);
  }

  ngOnInit() {
    this.fetchHistory();
    this.fetchAccountInfo();
    this.fetchPositions();
    // Auto-refresh positions every 10 seconds
    this.refreshInterval = setInterval(() => this.fetchPositions(), 10000);
  }

  ngOnDestroy() {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }

  fetchAccountInfo() {
    this.tradingService.testConnection().subscribe({
      next: (res) => this.accountData = res.data,
      error: () => {}
    });
  }

  fetchHistory() {
    this.loading = true;
    this.error = '';
    this.tradingService.getTradeHistory().subscribe({
      next: (res) => {
        this.trades = res.data || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load trade history.';
        this.loading = false;
      }
    });
  }

  fetchPositions() {
    this.positionsLoading = true;
    this.tradingService.getOpenPositions().subscribe({
      next: (res) => {
        this.positions = res.data || [];
        this.positionsLoading = false;
      },
      error: () => {
        this.positionsLoading = false;
      }
    });
  }

  closePosition(ticket: number) {
    this.closingTicket = ticket;
    this.closeMessage  = '';
    this.tradingService.closePosition(ticket).subscribe({
      next: (res) => {
        this.closeMessage  = res.message;
        this.closeSuccess  = true;
        this.closingTicket = null;
        // Refresh both positions and history after close
        setTimeout(() => {
          this.fetchPositions();
          this.fetchHistory();
          this.fetchAccountInfo();
          setTimeout(() => this.closeMessage = '', 6000);
        }, 800);
      },
      error: (err) => {
        this.closeMessage  = err.error?.message || `Failed to close #${ticket}.`;
        this.closeSuccess  = false;
        this.closingTicket = null;
      }
    });
  }

  getTypeString(rawType: number): string {
    switch (rawType) {
      case 0: return 'Buy';
      case 1: return 'Sell';
      case 2: return 'Balance';
      case 3: return 'Credit';
      case 4: return 'Charge';
      case 5: return 'Correction';
      case 6: return 'Bonus';
      case 7: return 'Commission';
      default: return 'Trade';
    }
  }

  getEntryString(rawEntry: number): string {
    switch (rawEntry) {
      case 0: return 'In';
      case 1: return 'Out';
      case 2: return 'In/Out';
      case 3: return 'Out By';
      default: return '';
    }
  }
}
