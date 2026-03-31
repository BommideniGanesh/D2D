export interface DriverDashboardDTO {
  assignmentId: number;
  assignmentStatus: string;
  assignmentType: string;
  assignedAt: string;

  // Shipment Details
  shipmentId: number;
  trackingNumber: string;
  shipmentStatus: string;
  totalAmount: number;
  paymentMode: string;
  signatureRequired: boolean;

  // Sender Details (Pickup Location)
  senderName: string;
  senderPhone: string;
  pickupAddress: string;
  pickupCity: string;
  pickupState: string;
  pickupPostalCode: string;
  pickupLatitude: number;
  pickupLongitude: number;

  // Receiver Details (Delivery Location)
  receiverName: string;
  receiverPhone: string;
  deliveryAddress: string;
  deliveryCity: string;
  deliveryState: string;
  deliveryPostalCode: string;
  deliveryLatitude: number;
  deliveryLongitude: number;

  // Package Details
  packageDescription: string;
  weight: number;
  dimensions: string;

  // Proof of Delivery
  podImageUrl?: string;
  podStatus?: string; // PENDING | PASSED | FAILED
}
