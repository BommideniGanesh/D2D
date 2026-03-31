export enum PaymentMode {
    PREPAID = 'PREPAID',
    COD = 'COD',
    THIRD_PARTY = 'THIRD_PARTY'
}

export enum ShipmentStatus {
    CREATED = 'CREATED',
    PICKUP_ASSIGNED = 'PICKUP_ASSIGNED',
    PICKED_UP = 'PICKED_UP',
    IN_TRANSIT = 'IN_TRANSIT',
    OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
    DELIVERED = 'DELIVERED',
    CANCELLED = 'CANCELLED',
    RETURN_REQUESTED = 'RETURN_REQUESTED'
}

export enum ShipmentSource {
    WEB = 'WEB',
    MOBILE = 'MOBILE',
    API = 'API'
}

export interface AgeRestrictionDetails {
    isRestricted: boolean;
    minimumAge: number;
    idCheckRequired: boolean;
}

export interface Shipment {
    id?: number;
    senderId: string;
    receiverId: string;
    packageId: number;

    senderDetails?: any;
    receiverDetails?: any;
    packageDetails?: any;

    baseShippingCost: number;
    taxAmount: number;
    insuranceAmount: number;
    discountAmount?: number;
    totalAmount: number;
    currency: string;

    paymentMode: PaymentMode;
    insured: boolean;
    insuranceProvider?: string;
    signatureRequired: boolean;
    ageRestrictionDetails: AgeRestrictionDetails;

    trackingNumber?: string;
    status: ShipmentStatus;
    history?: any[]; // JSON array

    createdBy: string;
    source: ShipmentSource;

    createdAt?: string;
    updatedAt?: string;
    lastUpdated?: string;
}
