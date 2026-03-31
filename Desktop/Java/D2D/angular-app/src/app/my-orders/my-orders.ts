import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../services/order.service';
import { LoaderService } from '../services/loader.service';
import { Shipment } from '../models/shipment.model';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import { DatePipe, CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-my-orders',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './my-orders.html',
  styleUrl: './my-orders.css',
})
export class MyOrders implements OnInit {
  shipments: Shipment[] = [];
  selectedShipment: Shipment | null = null;

  // ── Payment ────────────────────────────────────────────
  showPaymentModal = false;
  paymentShipment: Shipment | null = null;
  paymentMethod = 'CARD';
  pendingPaymentId: number | null = null;
  isProcessingPayment = false;
  shipmentPayments: Record<number, string> = {}; // shipmentId → status

  // ── Rating ─────────────────────────────────────────────
  showRatingModal = false;
  ratingShipment: Shipment | null = null;
  ratingValue = 0;
  ratingComment = '';
  isSubmittingRating = false;
  shipmentRatings: Record<number, number> = {}; // shipmentId → submitted rating

  constructor(
    private orderService: OrderService,
    private loaderService: LoaderService
  ) { }

  ngOnInit(): void {
    this.fetchShipments();
  }

  fetchShipments() {
    this.loaderService.show();
    this.orderService.getShipments().subscribe({
      next: (data) => {
        this.shipments = data;
        this.loaderService.hide();
        console.log('Shipments loaded:', this.shipments);
      },
      error: (err) => {
        this.loaderService.hide();
        console.error('Error fetching shipments:', err);
      }
    });
  }

  openDetails(shipment: Shipment) {
    this.selectedShipment = shipment;
  }

  closeDetails() {
    this.selectedShipment = null;
  }

  // ── Payment Methods ──────────────────────────────────────
  openPaymentModal(shipment: Shipment) {
    this.paymentShipment = shipment;
    this.paymentMethod = 'CARD';
    this.pendingPaymentId = null;
    this.showPaymentModal = true;
  }

  closePaymentModal() {
    this.showPaymentModal = false;
    this.paymentShipment = null;
    this.pendingPaymentId = null;
  }

  initiateAndConfirmPayment() {
    if (!this.paymentShipment?.id) return;
    this.isProcessingPayment = true;
    this.orderService.initiatePayment(this.paymentShipment.id, this.paymentMethod).subscribe({
      next: (res) => {
        this.orderService.confirmPayment(res.id).subscribe({
          next: () => {
            this.isProcessingPayment = false;
            this.shipmentPayments[this.paymentShipment!.id!] = 'PAID';
            alert('✅ Payment successful!');
            this.closePaymentModal();
          },
          error: (err) => {
            this.isProcessingPayment = false;
            alert('Payment confirmation failed: ' + (err.error?.message || err.message));
          }
        });
      },
      error: (err) => {
        this.isProcessingPayment = false;
        alert('Payment initiation failed: ' + (err.error?.message || err.message));
      }
    });
  }

  isPaymentPaid(shipment: Shipment): boolean {
    return this.shipmentPayments[shipment.id!] === 'PAID';
  }

  // ── Rating Methods ───────────────────────────────────────
  openRatingModal(shipment: Shipment) {
    this.ratingShipment = shipment;
    this.ratingValue = 0;
    this.ratingComment = '';
    this.showRatingModal = true;
  }

  closeRatingModal() {
    this.showRatingModal = false;
    this.ratingShipment = null;
  }

  setRating(value: number) {
    this.ratingValue = value;
  }

  starsArray(): number[] {
    return [1, 2, 3, 4, 5];
  }

  submitRating() {
    if (!this.ratingShipment?.id || this.ratingValue === 0) {
      alert('Please select a star rating.');
      return;
    }
    this.isSubmittingRating = true;
    this.orderService.submitRating({
      shipmentId: this.ratingShipment.id,
      rating: this.ratingValue,
      comment: this.ratingComment
    }).subscribe({
      next: (res) => {
        this.isSubmittingRating = false;
        this.shipmentRatings[this.ratingShipment!.id!] = res.rating;
        alert(`⭐ Thank you! You rated this delivery ${res.rating}/5.`);
        this.closeRatingModal();
      },
      error: (err) => {
        this.isSubmittingRating = false;
        alert('Rating failed: ' + (err.error?.message || err.message));
      }
    });
  }

  isRated(shipment: Shipment): boolean {
    return this.shipmentRatings[shipment.id!] !== undefined;
  }

  downloadInvoice(shipment: Shipment) {
    if (!shipment) return;

    const doc = new jsPDF('p', 'mm', 'a4');
    const currencyPipe = new CurrencyPipe('en-US');
    const datePipe = new DatePipe('en-US');

    // Header
    doc.setFontSize(22);
    doc.setTextColor(40, 40, 40);
    doc.text('DOOR-TO-DOOR', 14, 20);
    
    doc.setFontSize(10);
    doc.setTextColor(100, 100, 100);
    doc.text('Your reliable delivery partner', 14, 26);
    
    doc.setFontSize(12);
    doc.setTextColor(0, 0, 0);
    doc.text('INVOICE / RECEIPT', 150, 20);
    doc.setFontSize(10);
    doc.text(`Tracking #: ${shipment.trackingNumber}`, 150, 26);
    doc.text(`Date: ${datePipe.transform(shipment.createdAt, 'mediumDate')}`, 150, 32);
    doc.text(`Status: ${shipment.status}`, 150, 38);

    doc.setDrawColor(200, 200, 200);
    doc.line(14, 45, 196, 45);

    // Addresses
    doc.setFontSize(11);
    doc.setTextColor(40, 40, 40);
    doc.text('Sender Details:', 14, 55);
    doc.setFontSize(10);
    doc.setTextColor(80, 80, 80);
    let startY = 62;
    if (shipment.senderDetails) {
      doc.text(`${shipment.senderDetails.firstName} ${shipment.senderDetails.lastName}`, 14, startY); startY += 6;
      if (shipment.senderDetails.company) { doc.text(shipment.senderDetails.company, 14, startY); startY += 6; }
      doc.text(shipment.senderDetails.addressLine1 || '', 14, startY); startY += 6;
      doc.text(`${shipment.senderDetails.city}, ${shipment.senderDetails.state} ${shipment.senderDetails.zipCode}`, 14, startY);
    }

    doc.setFontSize(11);
    doc.setTextColor(40, 40, 40);
    doc.text('Receiver Details:', 110, 55);
    doc.setFontSize(10);
    doc.setTextColor(80, 80, 80);
    let rY = 62;
    if (shipment.receiverDetails) {
      doc.text(`${shipment.receiverDetails.firstName} ${shipment.receiverDetails.lastName}`, 110, rY); rY += 6;
      if (shipment.receiverDetails.company) { doc.text(shipment.receiverDetails.company, 110, rY); rY += 6; }
      doc.text(shipment.receiverDetails.addressLine1 || '', 110, rY); rY += 6;
      doc.text(`${shipment.receiverDetails.city}, ${shipment.receiverDetails.state} ${shipment.receiverDetails.zipCode}`, 110, rY);
    }

    // Package details
    startY = rY > startY ? rY + 15 : startY + 15;
    doc.setFontSize(12);
    doc.setTextColor(40, 40, 40);
    doc.text('Package Information', 14, startY);
    
    autoTable(doc, {
      startY: startY + 5,
      head: [['Type', 'Weight', 'Dimensions', 'Declared Value']],
      body: [
        [
          shipment.packageDetails?.packageType || shipment.packageDetails?.boxType || 'Standard',
          `${shipment.packageDetails?.weightKg ?? shipment.packageDetails?.weight ?? 0} kg`,
          `${shipment.packageDetails?.lengthCm ?? shipment.packageDetails?.length ?? 0}x${shipment.packageDetails?.widthCm ?? shipment.packageDetails?.width ?? 0}x${shipment.packageDetails?.heightCm ?? shipment.packageDetails?.height ?? 0} cm`,
          currencyPipe.transform(shipment.packageDetails?.declaredValue || 0, shipment.currency || 'USD') || '$0.00'
        ]
      ],
      headStyles: { fillColor: [41, 128, 185], textColor: 255 },
      alternateRowStyles: { fillColor: [245, 245, 245] }
    });

    // Billing details
    autoTable(doc, {
      startY: (doc as any).lastAutoTable.finalY + 15,
      head: [['Description', 'Amount']],
      body: [
        ['Base Shipping Cost', currencyPipe.transform(shipment.baseShippingCost, shipment.currency) || '$0.00'],
        ['Tax', currencyPipe.transform(shipment.taxAmount, shipment.currency) || '$0.00'],
        ['Insurance', currencyPipe.transform(shipment.insuranceAmount, shipment.currency) || '$0.00']
      ],
      foot: [['TOTAL', currencyPipe.transform(shipment.totalAmount, shipment.currency) || '$0.00']],
      headStyles: { fillColor: [52, 73, 94], textColor: 255 },
      footStyles: { fillColor: [200, 200, 200], textColor: [0, 0, 0], fontStyle: 'bold' },
      theme: 'grid'
    });

    // Footer
    const finalY = (doc as any).lastAutoTable.finalY + 20;
    doc.setFontSize(10);
    doc.setTextColor(150, 150, 150);
    doc.text('Thank you for choosing Door-To-Door Services!', 105, finalY, { align: 'center' });
    
    // Save PDF
    doc.save(`Invoice_${shipment.trackingNumber}.pdf`);
  }

  // --- Return/Damage Flow ---
  showIssueModal: boolean = false;
  issueShipment: Shipment | null = null;
  issueReason: string = 'Damage';
  issueDescription: string = '';
  isSubmittingIssue: boolean = false;

  openIssueModal(shipment: Shipment) {
    this.issueShipment = shipment;
    this.showIssueModal = true;
    this.issueReason = 'Damage';
    this.issueDescription = '';
  }

  closeIssueModal() {
    this.showIssueModal = false;
    this.issueShipment = null;
  }

  submitIssue() {
    if (!this.issueShipment) return;

    this.isSubmittingIssue = true;
    const backendPayload = {
      shipmentId: this.issueShipment.id,
      reason: this.issueReason + (this.issueDescription ? ': ' + this.issueDescription : '')
    };

    this.orderService.submitReturnRequest(backendPayload).subscribe({
      next: (res) => {
        this.isSubmittingIssue = false;
        alert('Your return/damage request has been submitted successfully.');
        this.closeIssueModal();
      },
      error: (err) => {
        this.isSubmittingIssue = false;
        alert('Failed to submit request: ' + (err.error?.message || err.message));
      }
    });
  }
}
