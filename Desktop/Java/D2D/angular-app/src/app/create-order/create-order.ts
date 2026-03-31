import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { StorageService } from '../services/storage.service';
import { OrderService } from '../services/order.service';
import { LoaderService } from '../services/loader.service';

import { BoxType, PackagingType, SealType } from '../models/package-details.model';
import { Shipment, PaymentMode, ShipmentStatus, ShipmentSource } from '../models/shipment.model';

@Component({
  selector: 'app-create-order',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-order.html',
  styleUrl: './create-order.css',
})
export class CreateOrder {
  senderForm: FormGroup;
  receiverForm: FormGroup;
  packageForm: FormGroup;
  shipmentForm: FormGroup;
  currentStep = 1;

  boxTypes = Object.values(BoxType);
  packagingTypes = Object.values(PackagingType);
  sealTypes = Object.values(SealType);

  constructor(
    private fb: FormBuilder,
    private storageService: StorageService,
    private orderService: OrderService,
    private loaderService: LoaderService,
    private router: Router
  ) {
    this.senderForm = this.fb.group({
      firstName: storageService.getUsername.name,
      lastName: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      email: ['', [Validators.email]],
      addressLine1: ['', Validators.required],
      addressLine2: [''],
      city: ['', Validators.required],
      state: ['', Validators.required],
      postalCode: ['', Validators.required],
      country: ['', Validators.required],
      latitude: [null],
      longitude: [null]
    });

    this.receiverForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      email: ['', [Validators.email]],
      addressLine1: ['', Validators.required],
      addressLine2: [''],
      city: ['', Validators.required],
      state: ['', Validators.required],
      postalCode: ['', Validators.required],
      country: ['', Validators.required],
      deliveryInstructions: [''],
      isResidential: [false]
    });

    this.packageForm = this.fb.group({
      packageCount: [1, [Validators.required, Validators.min(1)]],
      boxType: [BoxType.MEDIUM, Validators.required],
      lengthCm: ['', [Validators.required, Validators.min(0.1)]],
      widthCm: ['', [Validators.required, Validators.min(0.1)]],
      heightCm: ['', [Validators.required, Validators.min(0.1)]],
      weightKg: ['', [Validators.required, Validators.min(0.1)]],
      fragile: [false],
      hazardousMaterial: [false],
      packagingType: [PackagingType.CARTON, Validators.required],
      sealType: [null],
      handlingInstructions: ['']
    });

    this.shipmentForm = this.fb.group({
      paymentMode: [PaymentMode.PREPAID, Validators.required],
      insured: [false],
      insuranceProvider: [''],
      signatureRequired: [false],
      ageRestrictionDetails: this.fb.group({
        isRestricted: [false],
        minimumAge: [21],
        idCheckRequired: [false]
      })
    });
  }

  onSubmit() {
    if (this.senderForm.valid) {
      const user = this.storageService.getUser();
      const payload = {
        ...this.senderForm.value,
        userId: user.id // Assuming user object has an 'id' property
      };

      console.log('Submitting Sender Details:', payload);

      this.loaderService.show();
      this.orderService.createSender(payload).subscribe({
        next: (res) => {
          this.loaderService.hide();
          console.log('Sender created successfully:', res);
          this.createdSenderId = res.id;
          this.currentStep = 2;
        },
        error: (err) => {
          this.loaderService.hide();
          console.error('Error creating sender:', err);
        }
      });
    } else {
    }
  }

  backToSender() {
    this.currentStep = 1;
  }

  backToReceiver() {
    this.currentStep = 2;
  }

  onPackageSubmit() {
    if (this.packageForm.valid) {
      this.loaderService.show();

      const formValue = this.packageForm.value;
      const payload = {
        ...formValue,
        boxId: `BOX-${Date.now()}` // Generating a temporary Box ID
      };

      console.log('Submitting Package Details:', payload);

      this.orderService.createPackage(payload).subscribe({
        next: (res) => {
          this.loaderService.hide();
          console.log('Package created successfully:', res);
          // Store package ID for Shipment creation
          this.createdPackageId = res.id;

          // Calculate initial costs
          this.calculateCosts();

          this.currentStep = 4;
        },
        error: (err) => {
          this.loaderService.hide();
          console.error('Error creating package:', err);
        }
      });
    } else {
      this.packageForm.markAllAsTouched();
    }
  }

  onReceiverSubmit() {
    if (this.receiverForm.valid) {
      this.loaderService.show();
      const formValue = this.receiverForm.value;

      const payload = {
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        phoneNumber: formValue.phoneNumber,
        email: formValue.email,
        address: {
          addressLine1: formValue.addressLine1,
          addressLine2: formValue.addressLine2,
          city: formValue.city,
          state: formValue.state,
          postalCode: formValue.postalCode,
          country: formValue.country,
          deliveryInstructions: formValue.deliveryInstructions,
          isResidential: formValue.isResidential
        }
      };

      console.log('Submitting Receiver Details:', payload);

      this.orderService.createReceiver(payload).subscribe({
        next: (res) => {
          this.loaderService.hide();
          console.log('Receiver created successfully:', res);
          // Store receiver ID for Shipment creation
          this.createdReceiverId = res.id;
          this.currentStep = 3;
        },
        error: (err) => {
          this.loaderService.hide();
          console.error('Error creating receiver:', err);
        }
      });
    } else {
      this.receiverForm.markAllAsTouched();
    }
  }

  // Step 4: Shipment & Confirmation
  // shipmentForm already defined
  paymentModes = Object.values(PaymentMode);
  shippingCost = 0;
  taxAmount = 0;
  totalAmount = 0;

  // Store IDs from previous steps
  createdSenderId: string = '';
  createdReceiverId: string = '';
  createdPackageId: number = 0;

  calculateCosts() {
    // Pricing is dynamically calculated on the backend by the PricingEngine
    this.shippingCost = 0;
    this.taxAmount = 0;
    this.totalAmount = 0;
  }

  onShipmentSubmit() {
    if (this.shipmentForm.valid) {
      this.loaderService.show();

      if (!this.createdSenderId || !this.createdReceiverId || !this.createdPackageId) {
        this.loaderService.hide();
        console.error('Missing IDs:', { sender: this.createdSenderId, receiver: this.createdReceiverId, pkg: this.createdPackageId });
        alert('Error: Missing step details. Please go back and ensure all steps are completed.');
        return;
      }

      const payload: Shipment = {
        senderId: this.createdSenderId,
        receiverId: this.createdReceiverId,
        packageId: this.createdPackageId,

        baseShippingCost: this.shippingCost,
        taxAmount: this.taxAmount,
        insuranceAmount: 0, // Simplified
        discountAmount: 0,
        totalAmount: this.totalAmount,
        currency: 'USD',

        paymentMode: this.shipmentForm.get('paymentMode')?.value,
        insured: this.shipmentForm.get('insured')?.value,
        insuranceProvider: this.shipmentForm.get('insuranceProvider')?.value,
        signatureRequired: this.shipmentForm.get('signatureRequired')?.value,
        ageRestrictionDetails: this.shipmentForm.get('ageRestrictionDetails')?.value,

        status: ShipmentStatus.CREATED,
        source: ShipmentSource.WEB,
        createdBy: 'User' // Should come from AuthService
      };

      console.log('Submitting Shipment:', payload);

      this.orderService.createShipment(payload).subscribe({
        next: (res) => {
          this.loaderService.hide();
          console.log('Order Placed Successfully:', res);
          alert('Order Placed Successfully!\nCalculated Total Cost: $' + res.totalAmount + '\nTracking Number: ' + res.trackingNumber);
          this.router.navigate(['/my-orders']);
        },
        error: (err) => {
          this.loaderService.hide();
          console.error('Error creating shipment:', err);
        }
      });
    } else {
      this.shipmentForm.markAllAsTouched();
    }
  }

  backToPackage() {
    this.currentStep = 3;
  }
}
