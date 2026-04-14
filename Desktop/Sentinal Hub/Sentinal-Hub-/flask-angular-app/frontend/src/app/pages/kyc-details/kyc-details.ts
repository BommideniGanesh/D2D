import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { KycService } from '../../services/kyc';

@Component({
  selector: 'app-kyc-details',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './kyc-details.html',
  styleUrl: './kyc-details.css',
})
export class KycDetails {
  private fb = inject(FormBuilder);
  private kycService = inject(KycService);
  private router = inject(Router);

  error = '';
  isSubmitting = false;

  kycForm = this.fb.nonNullable.group({
    first_name: ['', Validators.required],
    last_name: ['', Validators.required],
    phone: ['', Validators.required],
    address: ['', Validators.required],
    apt_no: [''],
    city: ['', Validators.required],
    state: ['', Validators.required],
    country: ['', Validators.required],
    zipcode: ['', Validators.required]
  });

  onSubmit() {
    if (this.kycForm.invalid) {
      this.error = "Please fill out all required fields.";
      return;
    }

    this.isSubmitting = true;
    this.kycService.submitDetails(this.kycForm.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/kyc-upload']);
      },
      error: (err) => {
        // Assume failure means it was already submitted in some edge cases
        if (err.status === 400 && err.error?.msg?.includes('already')) {
            this.router.navigate(['/kyc-upload']);
        } else {
            this.error = err.error?.msg || "Failed to submit details";
            this.isSubmitting = false;
        }
      }
    });
  }
}
