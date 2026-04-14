import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TradingService } from '../../services/trading';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings implements OnInit {
  private tradingService = inject(TradingService);

  loginId = '';
  password = '';
  serverName = 'MetaQuotes-Demo';

  isLoading = false;
  isTesting = false;
  saveMessage = '';
  connectionMessage = '';
  connectionStatus: 'none' | 'success' | 'error' = 'none';
  
  accountData: any = null;

  ngOnInit() {
    this.tradingService.getCredentials().subscribe({
      next: (res) => {
        this.loginId = res.login_id;
        this.password = res.password;
        this.serverName = res.server;
      },
      error: () => {}
    });
  }

  onSave() {
    if (!this.loginId || !this.password || !this.serverName) return;

    this.isLoading = true;
    this.saveMessage = '';
    
    const payload = {
      login_id: this.loginId,
      password: this.password,
      server: this.serverName
    };
    
    this.tradingService.saveCredentials(payload).subscribe({
      next: () => {
        this.isLoading = false;
        this.saveMessage = '✅ MT5 Credentials securely saved to database!';
        setTimeout(() => this.saveMessage = '', 4000);
      },
      error: (err) => {
        this.isLoading = false;
        this.saveMessage = '❌ Error saving credentials.';
        console.error(err);
      }
    });
  }

  onTestConnection() {
    this.isTesting = true;
    this.connectionMessage = '';
    this.connectionStatus = 'none';
    this.accountData = null;

    this.tradingService.testConnection().subscribe({
      next: (res) => {
        this.isTesting = false;
        this.connectionStatus = 'success';
        this.connectionMessage = res.message;
        this.accountData = res.data;
      },
      error: (err) => {
        this.isTesting = false;
        this.connectionStatus = 'error';
        this.connectionMessage = err.error?.message || 'Connection failed.';
      }
    });
  }
}
