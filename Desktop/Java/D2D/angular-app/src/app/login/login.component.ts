import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoginService } from '../services/login.service';
import { LoaderService } from '../services/loader.service';
import { StorageService } from '../services/storage.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  loginForm: FormGroup;
  showLoginPassword: boolean = false;

  togglePasswordVisibility() {
    this.showLoginPassword = !this.showLoginPassword;
  }

  // Toast State
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  constructor(
    private fb: FormBuilder,
    private loginService: LoginService,
    private router: Router,
    private loaderService: LoaderService,
    private storageService: StorageService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    // Subscribe to Login Service Toasts
    this.loginService.toast$.subscribe(evt => {
      this.loaderService.hide();
      this.showToastMessage(evt.message, evt.type);
      if (evt.type === 'success') {
        this.handleSuccessfulLogin();
      }
    });
  }

  get l() { return this.loginForm.controls; }

  onSubmit() {
    if (this.loginForm.valid) {
      this.confirmLogin();
    }
  }

  confirmLogin() {
    const payload = this.loginForm.value;
    // console.log('Submitting Login:', payload);

    this.loaderService.show();
    // Logic delegated to Service
    this.loginService.login(payload);
  }

  handleSuccessfulLogin() {
    const user = this.storageService.getUser();
    const roles = user?.roles || [];

    // Check if user is a driver
    if (roles.includes('DRIVER')) {
      // Always redirect drivers to details page on login
      this.router.navigate(['/details']);
    } else if (roles.includes('ADMIN')) {
      // Redirect admins to the dashboard
      this.router.navigate(['/admin/dashboard']);
    } else {
      // Not a driver or admin, navigate to create order
      this.router.navigate(['/create-order']);
    }
  }

  showToastMessage(msg: string, type: 'success' | 'error') {
    this.toastMessage = msg;
    this.toastType = type;
    this.showToast = true;
    setTimeout(() => this.showToast = false, 3000); // Hide after 3s
  }
}
