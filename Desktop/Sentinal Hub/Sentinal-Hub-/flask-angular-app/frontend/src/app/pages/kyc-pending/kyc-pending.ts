import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-kyc-pending',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kyc-pending.html',
  styleUrls: ['../kyc-details/kyc-details.css']
})
export class KycPending implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit() {
    this.checkStatus();
  }

  checkStatus() {
     const user = this.authService.currentUserValue;
     if(user && user.kyc_status === 'Approved') {
         this.router.navigate(['/dashboard']);
     }
  }
}
