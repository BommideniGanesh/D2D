export enum BoxType {
    SMALL = 'SMALL',
    MEDIUM = 'MEDIUM',
    LARGE = 'LARGE',
    CUSTOM = 'CUSTOM'
}

export enum PackagingType {
    CARTON = 'CARTON',
    WOODEN_CRATE = 'WOODEN_CRATE',
    PLASTIC = 'PLASTIC',
    ENVELOPE = 'ENVELOPE'
}

export enum SealType {
    TAPE = 'TAPE',
    STRAP = 'STRAP',
    LOCK = 'LOCK'
}

export interface PackageDetails {
    packageCount: number;
    boxId?: string;
    boxType: BoxType;
    lengthCm: number;
    widthCm: number;
    heightCm: number;
    weightKg: number;
    fragile: boolean;
    hazardousMaterial: boolean;
    packagingType: PackagingType;
    sealType?: SealType;
    handlingInstructions?: string;
}
