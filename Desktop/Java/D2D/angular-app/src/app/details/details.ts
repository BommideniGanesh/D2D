import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { DriverService } from '../services/driver.service';
import { StorageService } from '../services/storage.service';
import { LoaderService } from '../services/loader.service';
import { Driver, LicenseType, VehicleType, AvailabilityStatus } from '../models/driver.model';

@Component({
    selector: 'app-details',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './details.html',
    styles: []
})
export class DetailsComponent implements OnInit {
    driverForm: FormGroup;
    licenseTypes = Object.values(LicenseType);
    vehicleTypes = Object.values(VehicleType);
    availabilityStatuses = Object.values(AvailabilityStatus);

    existingDriver: Driver | null = null;
    isEditMode = false;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private driverService: DriverService,
        private storageService: StorageService,
        private loaderService: LoaderService
    ) {
        this.driverForm = this.fb.group({
            firstName: ['', Validators.required],
            lastName: ['', Validators.required],
            addressLine1: ['', Validators.required],
            addressLine2: [''],
            state: ['', Validators.required],
            pincode: ['', [Validators.required, Validators.pattern(/^\d{5,6}$/)]],
            phoneNumber: ['', [Validators.required, Validators.pattern(/^\+?\d{10,15}$/)]],
            licenseNumber: ['', Validators.required],
            licenseType: [LicenseType.REGULAR, Validators.required],
            licenseExpiryDate: ['', Validators.required],
            vehicleNumber: ['', Validators.required],
            vehicleType: [VehicleType.CAR, Validators.required],
            vehicleModel: ['', Validators.required],
            vehicleColor: ['', Validators.required],
            availabilityStatus: [AvailabilityStatus.AVAILABLE, Validators.required]
        });
    }

    ngOnInit(): void {
        this.loadDriverDetails();
    }

    loadDriverDetails() {
        const user = this.storageService.getUser();
        if (user && user.id) {
            this.loaderService.show();
            this.driverService.getDriverByUserId(user.id).subscribe({
                next: (driver) => {
                    this.existingDriver = driver;
                    this.isEditMode = true;
                    this.driverForm.patchValue(driver);
                    this.loaderService.hide();
                },
                error: (err) => {
                    this.loaderService.hide();
                    console.log('No existing driver details found, showing empty form');
                }
            });
        }
    }

    onSubmit() {
        if (this.driverForm.valid) {
            const user = this.storageService.getUser();
            const driverData: Driver = {
                ...this.driverForm.value,
                userId: user.id
            };

            this.loaderService.show();

            if (this.isEditMode && this.existingDriver?.id) {
                this.driverService.updateDriver(this.existingDriver.id, driverData).subscribe({
                    next: (res) => {
                        this.loaderService.hide();
                        alert('Driver details updated successfully!');
                        this.existingDriver = res;
                    },
                    error: (err) => {
                        this.loaderService.hide();
                        console.error('Error updating driver:', err);
                        alert('Failed to update driver details');
                    }
                });
            } else {
                this.driverService.createDriver(driverData).subscribe({
                    next: (res) => {
                        this.loaderService.hide();
                        alert('Driver details saved successfully!');
                        this.existingDriver = res;
                        this.isEditMode = true;
                        // Redirect to pending orders after successful creation
                        this.router.navigate(['/pending-orders']);
                    },
                    error: (err) => {
                        this.loaderService.hide();
                        console.error('Error creating driver:', err);
                        alert('Failed to save driver details');
                    }
                });
            }
        } else {
            this.driverForm.markAllAsTouched();
        }
    }

    verifyDriver() {
        if (this.existingDriver?.id) {
            this.loaderService.show();
            this.driverService.verifyDriver(this.existingDriver.id).subscribe({
                next: (res) => {
                    this.loaderService.hide();
                    this.existingDriver = res;
                    alert('Driver verified successfully! You can now receive orders.');
                },
                error: (err) => {
                    this.loaderService.hide();
                    console.error('Error verifying driver:', err);
                    alert('Failed to verify driver');
                }
            });
        }
    }
}
