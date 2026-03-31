import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { OrderService } from '../services/order.service';
import { StorageService } from '../services/storage.service';
import { DriverService } from '../services/driver.service';

@Component({
    selector: 'app-earnings',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './earnings.html',
    styles: [
        `.history-table th { color: #6c757d; font-size: 0.85rem; font-weight: 600; text-transform: uppercase; }`,
        `.history-table td { vertical-align: middle; }`
    ],
    providers: [CurrencyPipe, DatePipe]
})
export class EarningsComponent implements OnInit {
    todayEarnings: number = 0;
    weekEarnings: number = 0;
    monthEarnings: number = 0;
    totalDeliveries: number = 0;
    history: any[] = [];
    loading = true;

    constructor(
        private orderService: OrderService,
        private storageService: StorageService,
        private driverService: DriverService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.fetchEarnings();
    }

    fetchEarnings() {
        const user = this.storageService.getUser();
        if (!user || (!user.id && !user.sub)) {
            this.loading = false;
            return;
        }

        const userId = user.id || user.sub;
        
        this.loading = true;

        // First resolve the Driver Profile ID by User ID
        this.driverService.getDriverByUserId(userId).subscribe({
            next: (driver) => {
                if (driver && driver.id) {
                    this.loadEarningsData(driver.id);
                } else {
                    this.loading = false;
                    this.cdr.detectChanges();
                }
            },
            error: (err) => {
                console.error('Could not find driver profile', err);
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    private loadEarningsData(driverId: number) {
        this.orderService.getDriverEarnings(driverId).subscribe({
            next: (data) => {
                this.todayEarnings = data.todayEarnings || 0;
                this.weekEarnings = data.weekEarnings || 0;
                this.monthEarnings = data.monthEarnings || 0;
                this.totalDeliveries = data.totalDeliveries || 0;
                this.history = data.history || [];
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error fetching earnings', err);
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }
}
