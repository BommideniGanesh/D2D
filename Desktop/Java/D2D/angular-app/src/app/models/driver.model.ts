export enum LicenseType {
    REGULAR = 'REGULAR',
    CDL = 'CDL',
    MOTORCYCLE = 'MOTORCYCLE'
}

export enum VehicleType {
    CAR = 'CAR',
    VAN = 'VAN',
    TRUCK = 'TRUCK',
    MOTORCYCLE = 'MOTORCYCLE'
}

export enum AvailabilityStatus {
    AVAILABLE = 'AVAILABLE',
    BUSY = 'BUSY',
    OFFLINE = 'OFFLINE'
}

export interface Driver {
    id?: number;
    userId: string;
    firstName: string;
    lastName: string;
    addressLine1: string;
    addressLine2?: string;
    state: string;
    pincode: string;
    phoneNumber: string;
    licenseNumber: string;
    licenseType: LicenseType;
    licenseExpiryDate: string;
    vehicleNumber: string;
    vehicleType: VehicleType;
    vehicleModel: string;
    vehicleColor: string;
    availabilityStatus: AvailabilityStatus;
    isVerified?: boolean;
    isActive?: boolean;
    createdAt?: string;
    updatedAt?: string;
}
