import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NycDriverService } from '../../services/nyc-driver.service';
import { NycDriver } from '../../models/nyc-driver.model';

@Component({
  selector: 'app-hire-driver',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hire-driver.component.html',
  styles: [`
    .driver-table-wrapper { border-radius: 12px; overflow: hidden; }
    .search-bar { max-width: 380px; }
    .badge-type { font-size: 0.75rem; padding: 4px 10px; border-radius: 20px; }
    .btn-hire { min-width: 80px; }
    .toast-wrap {
      position: fixed; top: 20px; right: 20px; z-index: 2000;
      animation: fadeIn 0.3s ease;
    }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
    .page-badge { background: #f1f3f5; color: #495057; border-radius: 20px; padding: 4px 14px; font-size: 0.85rem; }
    .empty-state { padding: 60px 20px; color: #adb5bd; }
    .header-gradient {
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 60%, #0f3460 100%);
    }
    .spinner-wrap { min-height: 300px; }
  `]
})
export class HireDriverComponent implements OnInit {
  drivers: NycDriver[] = [];
  loading = false;
  error = '';
  searchQuery = '';
  currentPage = 0;
  hasMore = true;

  toastMessage = '';
  toastType: 'success' | 'info' = 'success';
  showToast = false;
  private toastTimer: any;

  hiredDrivers = new Set<string>();

  constructor(private nycDriverService: NycDriverService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.fetchDrivers();
  }

  fetchDrivers(): void {
    this.loading = true;
    this.error = '';
    this.nycDriverService.getDrivers(this.currentPage, this.searchQuery).subscribe({
      next: (data) => {
        this.drivers = data;
        this.hasMore = data.length === 25;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Failed to load driver data. Please try again.';
        this.loading = false;
        console.error('NYC API error:', err);
        this.cdr.detectChanges();
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.fetchDrivers();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.currentPage = 0;
    this.fetchDrivers();
  }

  nextPage(): void {
    if (this.hasMore) {
      this.currentPage++;
      this.fetchDrivers();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.fetchDrivers();
    }
  }

  hireDriver(driver: NycDriver): void {
    const key = driver.vehicle_license_number;
    this.hiredDrivers.add(key);
    this.showToastMsg(`✅ ${this.formatName(driver.name)} has been hired successfully!`, 'success');
  }

  isHired(driver: NycDriver): boolean {
    return this.hiredDrivers.has(driver.vehicle_license_number);
  }

  showToastMsg(msg: string, type: 'success' | 'info'): void {
    this.toastMessage = msg;
    this.toastType = type;
    this.showToast = true;
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.showToast = false, 3500);
  }

  getLicenseBadgeClass(type: string): string {
    const map: Record<string, string> = {
      'FHV': 'bg-primary',
      'HAIL': 'bg-success',
      'MEDALLION': 'bg-warning text-dark',
    };
    return map[type?.toUpperCase()] || 'bg-secondary';
  }

  formatName(raw: string): string {
    if (!raw) return '—';
    if (raw.includes(',')) {
      const [last, first] = raw.split(',').map(s => s.trim());
      const toTitle = (s: string) => s.charAt(0).toUpperCase() + s.slice(1).toLowerCase();
      return `${toTitle(first)} ${toTitle(last)}`;
    }
    return raw.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');
  }

  formatExpiry(dateStr: string): { text: string; cls: string } {
    if (!dateStr) return { text: 'N/A', cls: 'text-muted' };
    const expiry = new Date(dateStr);
    const now = new Date();
    const daysLeft = Math.ceil((expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    const text = expiry.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    if (daysLeft < 0) return { text, cls: 'text-danger fw-bold' };
    if (daysLeft < 90) return { text, cls: 'text-warning fw-bold' };
    return { text, cls: 'text-success' };
  }
}
