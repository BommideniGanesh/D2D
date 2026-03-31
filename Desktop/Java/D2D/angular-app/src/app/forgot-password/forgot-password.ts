import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { LoginService } from '../services/login.service';
import { LoaderService } from '../services/loader.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPasswordComponent {
  forgotForm: FormGroup;
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';
  showLoginPassword = false;

  constructor(
    private fb: FormBuilder,
    private loginService: LoginService,
    private router: Router,
    private loaderService: LoaderService
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get l() { return this.forgotForm.controls; }

  togglePasswordVisibility() {
    this.showLoginPassword = !this.showLoginPassword;
  }

  onSubmit() {
    if (this.forgotForm.valid) {
      this.loaderService.show();
      const payload = this.forgotForm.value;

      this.loginService.forgotPassword(payload).subscribe({
        next: (response) => {
          this.loaderService.hide();
          this.showToastMessage('Password reset successfully. You can now login.', 'success');
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (err) => {
          this.loaderService.hide();
          this.showToastMessage('Failed to reset password. Please check your email.', 'error');
        }
      });
    }
  }

  showToastMessage(msg: string, type: 'success' | 'error') {
    this.toastMessage = msg;
    this.toastType = type;
    this.showToast = true;
    setTimeout(() => this.showToast = false, 3000); // Hide after 3s
  }
}
