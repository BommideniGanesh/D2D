export interface SenderDetails {
    firstName: string;
    lastName: string;
    phoneNumber: string;
    email?: string;
    addressLine1: string;
    addressLine2?: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    latitude?: number;
    longitude?: number;
    userId?: number | string;
}
