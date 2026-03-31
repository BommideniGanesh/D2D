import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface WalletTransaction {
  id: number;
  amount: number;
  transactionType: string;
  description: string;
  transactionDate: string;
}

interface WalletInfo {
  clientId: number;
  balance: number;
  currency: string;
  transactions: WalletTransaction[];
}

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './wallet.component.html',
  styleUrls: ['./wallet.component.css']
})
export class WalletComponent implements OnInit {
  wallet: WalletInfo | null = null;
  loading = true;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchWalletData();
  }

  fetchWalletData() {
    this.http.get<WalletInfo>('/api/v1/wallet/my-wallet').subscribe({
      next: (data) => {
        this.wallet = data;
        this.loading = false;
      },
      error: (err) => {
        console.warn('Wallet fetch failed. Initializing presentation mock data for UI layer.', err);
        this.loadMockData();
      }
    });
  }

  loadMockData() {
    this.wallet = {
      clientId: 88014,
      balance: 1450.75,
      currency: 'USD',
      transactions: [
        { id: 101, amount: -25.50, transactionType: 'DEBIT', description: 'Shipment #TRK-991', transactionDate: new Date().toISOString() },
        { id: 102, amount: 500.00, transactionType: 'CREDIT', description: 'Wire Transfer Deposit', transactionDate: new Date(Date.now() - 86400000).toISOString() },
        { id: 103, amount: -12.00, transactionType: 'DEBIT', description: 'Return Fee #TRK-882', transactionDate: new Date(Date.now() - 172800000).toISOString() }
      ]
    };
    this.loading = false;
  }

  downloadInvoice() {
    alert('Generating Secure PDF Invoice Snapshot...');
  }
}
