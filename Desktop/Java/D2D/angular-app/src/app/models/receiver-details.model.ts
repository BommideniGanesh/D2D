export interface Address {
    addressLine1: string;
    addressLine2?: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    deliveryInstructions?: string;
    isResidential: boolean;
}

export interface ReceiverDetails {
    firstName: string;
    lastName: string;
    phoneNumber: string;
    email?: string;
    address: Address;
}
