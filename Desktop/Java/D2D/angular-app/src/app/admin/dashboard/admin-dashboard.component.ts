import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService, AdminDashboardDTO } from '../../services/analytics.service';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './admin-dashboard.component.html',
  styles: [`
    .chart-container {
      height: 200px;
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      padding-top: 20px;
    }
    .bar-group {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-end;
      width: 100%;
      height: 100%;
    }
    .bar {
      width: 30px;
      background-color: #0d6efd;
      border-radius: 4px 4px 0 0;
      transition: height 0.5s ease;
      position: relative;
    }
    .bar:hover {
      opacity: 0.8;
    }
    .bar-count {
      font-size: 0.75rem;
      margin-bottom: 3px;
      font-weight: 600;
      color: #0d6efd;
      min-height: 1rem;
    }
    .bar-label {
      font-size: 0.75rem;
      margin-top: 5px;
      color: #6c757d;
    }
    .driver-stat-card {
        transition: transform 0.2s;
    }
    .driver-stat-card:hover {
        transform: translateY(-5px);
    }
  `]
})
export class AdminDashboardComponent implements OnInit {
  dashboardData: AdminDashboardDTO | null = null;
  loading = true;
  error = '';

  activeModalType: 'USERS' | 'DRIVERS' | 'DELIVERED' | 'DAMAGED' | null = null;
  modalData: any[] = [];
  isModalLoading = false;

  constructor(private analyticsService: AnalyticsService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.fetchDashboardData();
  }

  fetchDashboardData(): void {
    this.loading = true;
    this.error = '';
    this.analyticsService.getDashboardData().subscribe({
      next: (data) => {
        this.dashboardData = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading dashboard', err);
        this.error = 'Failed to load dashboard data';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Helper to calculate bar height relative to max value
  getBarHeight(value: number): string {
    if (!this.dashboardData || !this.dashboardData.ordersGraphData) return '0%';
    const max = Math.max(...this.dashboardData.ordersGraphData.map(d => d.orders), 1); // Avoid div by zero
    const percentage = (value / max) * 100;
    return `${percentage}%`;
  }

  openDetailsModal(type: 'USERS' | 'DRIVERS' | 'DELIVERED' | 'DAMAGED') {
    this.activeModalType = type;
    this.isModalLoading = true;
    this.modalData = [];

    let request;
    if (type === 'USERS') {
      request = this.analyticsService.getActiveUsers();
    } else if (type === 'DRIVERS') {
      request = this.analyticsService.getActiveDrivers();
    } else if (type === 'DELIVERED') {
      request = this.analyticsService.getDeliveredOrders();
    } else if (type === 'DAMAGED') {
      request = this.analyticsService.getDamagedOrReturnedOrders();
    }

    if (request) {
      request.subscribe({
        next: (data) => {
          this.modalData = data;
          this.isModalLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error fetching details', err);
          this.isModalLoading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      this.isModalLoading = false;
    }
  }

  closeModal() {
    this.activeModalType = null;
    this.modalData = [];
  }
}
