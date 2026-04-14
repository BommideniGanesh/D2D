import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KycService } from '../../services/kyc';


@Component({
  selector: 'app-admin-kyc',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-kyc.html',
  styleUrl: './admin-kyc.css',
})
export class AdminKyc implements OnInit {
  private kycService = inject(KycService);

  kycList: any[] = [];
  selectedDocumentUrl: string | null = null;
  error: string | null = null;

  ngOnInit() {
    this.loadKycList();
  }

  loadKycList() {
    this.kycService.getAllKyc().subscribe({
      next: (data) => this.kycList = data,
      error: (err) => this.error = "Unauthorized. Make sure you are logged in as admin."
    });
  }

  viewDocument(id: number) {
    this.kycService.getDocumentBlob(id).subscribe({
      next: (blob) => {
        this.selectedDocumentUrl = URL.createObjectURL(blob);
      },
      error: (err) => console.error("Could not fetch document", err)
    });
  }

  closeDocument() {
    if (this.selectedDocumentUrl) {
      URL.revokeObjectURL(this.selectedDocumentUrl);
    }
    this.selectedDocumentUrl = null;
  }

  approveKyc(id: number) {
    this.kycService.approveKyc(id).subscribe({
      next: () => {
        const item = this.kycList.find(k => k.id === id);
        if (item) item.status = 'Approved';
      },
      error: (err) => console.error(err)
    });
  }
}
