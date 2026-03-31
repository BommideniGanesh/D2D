import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-warehouse',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './warehouse.component.html',
  styleUrls: ['./warehouse.component.css']
})
export class WarehouseComponent {
  bagBarcode: string = '';
  shipmentBarcode: string = '';
  terminalLog: string[] = [];
  statusMessage: string = '';
  isError: boolean = false;

  constructor(private http: HttpClient) {}

  logEvent(msg: string, isErr = false) {
    this.statusMessage = msg;
    this.isError = isErr;
    const time = new Date().toLocaleTimeString();
    this.terminalLog.unshift(`[${time}] ${isErr ? 'ERROR' : 'INFO'}: ${msg}`);
  }

  openBag() {
    if (!this.bagBarcode) return this.logEvent('Scan a bag barcode first.', true);
    // Real call: this.http.post(`/api/v1/warehouse/bag/${this.bagBarcode}/open`, {})...
    this.logEvent(`Transit Bag ${this.bagBarcode} opened and securely ready for sortation.`);
  }

  scanShipment() {
    if (!this.bagBarcode || !this.shipmentBarcode) {
      return this.logEvent('Both Bag and Shipment barcodes are required.', true);
    }
    // Real call: this.http.post(`/api/v1/warehouse/bag/${this.bagBarcode}/scan/${this.shipmentBarcode}`, {})...
    this.logEvent(`Shipment ${this.shipmentBarcode} successfully scanned and assigned seamlessly to Bag ${this.bagBarcode}.`);
    this.shipmentBarcode = ''; 
  }

  sealBag() {
    if (!this.bagBarcode) return this.logEvent('Scan a bag barcode first.', true);
    // Real call: this.http.post(`/api/v1/warehouse/bag/${this.bagBarcode}/seal`, {})...
    this.logEvent(`Transit Bag ${this.bagBarcode} SEALED. Ready for linehaul transit.`);
    this.bagBarcode = ''; 
  }

  receiveBag() {
    if (!this.bagBarcode) return this.logEvent('Scan a bag barcode first.', true);
    // Real call: this.http.post(`/api/v1/warehouse/bag/${this.bagBarcode}/receive/HUB-CURRENT`, {})...
    this.logEvent(`Transit Bag ${this.bagBarcode} Received at Hub and successfully Unpacked.`);
    this.bagBarcode = '';
  }
}
