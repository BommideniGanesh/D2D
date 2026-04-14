import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { KycService } from '../../services/kyc';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-kyc-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kyc-upload.html',
  styleUrl: './kyc-upload.css',
})
export class KycUpload {
  private kycService = inject(KycService);
  private authService = inject(AuthService);
  private router = inject(Router);

  error = '';
  isUploading = false;
  selectedFile: File | null = null;
  filePreview: string | null = null;

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        this.error = "Please upload an image file (JPEG, PNG).";
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        this.error = "File size must be less than 5MB.";
        return;
      }
      
      this.error = '';
      this.selectedFile = file;
      
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.filePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  onUpload() {
    if (!this.selectedFile) {
        this.error = "Please select a file to upload.";
        return;
    }
    
    this.isUploading = true;
    this.kycService.uploadDocument(this.selectedFile).subscribe({
      next: () => {
        this.authService.updateKycStatus('Submitted');
        this.router.navigate(['/kyc-pending']);
      },
      error: (err) => {
        this.error = err.error?.msg || "Failed to upload document.";
        this.isUploading = false;
      }
    });
  }
}
