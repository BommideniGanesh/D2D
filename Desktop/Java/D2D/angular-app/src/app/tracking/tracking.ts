import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../services/order.service';
import { LoaderService } from '../services/loader.service';
import { Shipment } from '../models/shipment.model';

@Component({
  selector: 'app-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tracking.html',
  styleUrl: './tracking.css',
})
export class Tracking {
  trackingNumber: string = '';
  shipment: Shipment | null = null;
  errorMessage: string = '';
  searched: boolean = false;

  constructor(
    private orderService: OrderService,
    private loaderService: LoaderService
  ) { }

  trackOrder() {
    if (!this.trackingNumber.trim()) return;

    this.loaderService.show();
    this.searched = false;
    this.errorMessage = '';
    this.shipment = null;

    this.orderService.trackShipment(this.trackingNumber).subscribe({
      next: (data) => {
        this.shipment = data;
        this.searched = true;
        this.loaderService.hide();
      },
      error: (err) => {
        this.searched = true;
        this.errorMessage = 'Shipment not found or error occurred.';
        this.loaderService.hide();
        console.error('Tracking error:', err);
      }
    });
  }
}
