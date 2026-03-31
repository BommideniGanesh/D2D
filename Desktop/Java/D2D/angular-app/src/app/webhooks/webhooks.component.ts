import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface WebhookSubscription {
  id: number;
  targetUrl: string;
  secretKey: string;
  isActive: boolean;
  createdAt: string;
}

@Component({
  selector: 'app-webhooks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './webhooks.component.html',
  styleUrls: ['./webhooks.component.css']
})
export class WebhooksComponent implements OnInit {
  subscriptions: WebhookSubscription[] = [];
  newUrl: string = '';
  loading = true;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchWebhooks();
  }

  fetchWebhooks() {
    this.http.get<WebhookSubscription[]>('/api/v1/webhooks').subscribe({
      next: (data) => {
        this.subscriptions = data;
        this.loading = false;
      },
      error: () => {
        console.warn('Backend unavailable. Loading simulation data.');
        this.loadMockWebhooks();
      }
    });
  }

  loadMockWebhooks() {
    this.subscriptions = [
      { id: 1, targetUrl: 'https://api.acme.corp/logistics/events', secretKey: 'sha256_wk_928dj298dh3qwd2', isActive: true, createdAt: new Date().toISOString() },
      { id: 2, targetUrl: 'https://sandbox.b2b.io/webhook/receivers', secretKey: 'sha256_wk_119xx001plm', isActive: true, createdAt: new Date(Date.now() - 3600000).toISOString() }
    ];
    this.loading = false;
  }

  addWebhook() {
    if (!this.newUrl) return;
    const newSub: WebhookSubscription = {
      id: Math.floor(Math.random() * 1000),
      targetUrl: this.newUrl,
      secretKey: 'sha256_wk_' + Math.random().toString(36).substring(7),
      isActive: true,
      createdAt: new Date().toISOString()
    };
    
    this.subscriptions.push(newSub);
    this.newUrl = '';
  }

  deleteWebhook(id: number) {
    this.subscriptions = this.subscriptions.filter(s => s.id !== id);
  }
}
