import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SenderDetails } from '../models/sender-details.model';
import { ReceiverDetails } from '../models/receiver-details.model';
import { PackageDetails } from '../models/package-details.model';
import { DriverDashboardDTO } from '../models/driver-dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private apiUrl = 'http://localhost:8080/api/senders';
  private receiversUrl = 'http://localhost:8080/api/receivers';
  private packagesUrl = 'http://localhost:8080/api/packages';
  private shipmentsUrl = 'http://localhost:8080/api/shipments';
  private deliveriesUrl = 'http://localhost:8080/api/deliveries';

  constructor(private http: HttpClient) { }

  createSender(senderDetails: SenderDetails): Observable<any> {
    return this.http.post(this.apiUrl, senderDetails);
  }

  createReceiver(receiverDetails: ReceiverDetails): Observable<any> {
    return this.http.post(this.receiversUrl, receiverDetails);
  }

  createPackage(packageDetails: PackageDetails): Observable<any> {
    return this.http.post(this.packagesUrl, packageDetails);
  }

  createShipment(shipmentDetails: any): Observable<any> {
    return this.http.post(this.shipmentsUrl, shipmentDetails);
  }

  getShipments(): Observable<any[]> {
    return this.http.get<any[]>(this.shipmentsUrl);
  }

  trackShipment(trackingNumber: string): Observable<any> {
    return this.http.get<any>(`${this.shipmentsUrl}/${trackingNumber}`);
  }

  getPendingShipments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.shipmentsUrl}/pending`);
  }

  getDriverDashboard(driverId: number): Observable<DriverDashboardDTO[]> {
    return this.http.get<DriverDashboardDTO[]>(`http://localhost:8080/api/assignments/driver/${driverId}/dashboard`);
  }

  triggerBatchAssignment(batchSize: number = 10): Observable<any> {
    return this.http.post<any>(`http://localhost:8080/api/assignments/trigger-batch?batchSize=${batchSize}`, {});
  }

  completePickup(assignmentId: number): Observable<any> {
    return this.http.post<any>(`http://localhost:8080/api/assignments/${assignmentId}/pickup`, {});
  }

  submitReturnRequest(requestDetails: any): Observable<any> {
    return this.http.post<any>('http://localhost:8080/api/returns', requestDetails);
  }

  completeDelivery(assignmentId: number): Observable<any> {
    return this.http.post<any>(`http://localhost:8080/api/assignments/${assignmentId}/deliver`, {});
  }

  /** Upload image file → backend saves to static/pod-images/ → returns { imageUrl } */
  uploadPodImage(file: File): Observable<{ imageUrl: string; filename: string }> {
    const form = new FormData();
    form.append('file', file, file.name);
    return this.http.post<{ imageUrl: string; filename: string }>(`${this.deliveriesUrl}/upload-image`, form);
  }

  /** Submit PoD with a stored image URL */
  submitProofOfDelivery(assignmentId: number, imageUrl: string): Observable<any> {
    return this.http.post<any>(`${this.deliveriesUrl}/${assignmentId}/proof`, { imageUrl });
  }

  /** Get PoD record for a shipment (customer view) */
  getPodByShipment(shipmentId: number): Observable<any> {
    return this.http.get<any>(`${this.deliveriesUrl}/shipment/${shipmentId}/proof`);
  }

  rejectAssignment(assignmentId: number): Observable<any> {
    return this.http.post<any>(`http://localhost:8080/api/assignments/${assignmentId}/reject`, {});
  }

  assignPendingShipment(shipmentId: number, driverId: number): Observable<any> {
    return this.http.post<any>(`http://localhost:8080/api/assignments/shipment/${shipmentId}/assign/${driverId}`, {});
  }

  /** Mark a pending shipment as "Not Interested" — persisted to DB */
  skipShipment(driverId: number, shipmentId: number): Observable<any> {
    return this.http.post<any>(`http://localhost:8080/api/assignments/driver/${driverId}/skip/${shipmentId}`, {});
  }

  /** Get all shipments a driver has skipped (for Rejected tab) */
  getSkippedShipments(driverId: number): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8080/api/assignments/driver/${driverId}/skipped`);
  }

  /** Get just the IDs of skipped shipments (for filtering pending list) */
  getSkippedShipmentIds(driverId: number): Observable<number[]> {
    return this.http.get<number[]>(`http://localhost:8080/api/assignments/driver/${driverId}/skipped-ids`);
  }

  getDriverEarnings(driverId: number): Observable<any> {
    return this.http.get<any>(`http://localhost:8080/api/assignments/driver/${driverId}/earnings`);
  }

  // ── Payment ──────────────────────────────────────────────
  initiatePayment(shipmentId: number, paymentMethod: string): Observable<any> {
    return this.http.post<any>('http://localhost:8080/api/payments/initiate', { shipmentId, paymentMethod });
  }

  confirmPayment(paymentId: number): Observable<any> {
    return this.http.post<any>(`http://localhost:8080/api/payments/${paymentId}/confirm`, {});
  }

  getPaymentStatus(shipmentId: number): Observable<any> {
    return this.http.get<any>(`http://localhost:8080/api/payments/shipment/${shipmentId}`);
  }

  // ── Rating ───────────────────────────────────────────────
  submitRating(payload: { shipmentId: number; rating: number; comment: string }): Observable<any> {
    return this.http.post<any>('http://localhost:8080/api/ratings', payload);
  }

  getShipmentRating(shipmentId: number): Observable<any> {
    return this.http.get<any>(`http://localhost:8080/api/ratings/shipment/${shipmentId}`);
  }
}
