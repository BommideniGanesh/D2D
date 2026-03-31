import { Component } from '@angular/core';
import { DriverDashboardComponent } from '../driver/dashboard/driver-dashboard.component';

@Component({
    selector: 'app-pending-orders',
    standalone: true,
    imports: [DriverDashboardComponent],
    templateUrl: './pending-orders.html',
    styles: []
})
export class PendingOrdersComponent { }
