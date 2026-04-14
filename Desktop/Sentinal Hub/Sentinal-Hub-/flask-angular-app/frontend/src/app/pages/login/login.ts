import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  errorMessage = '';
  isLoading = false;

  onSubmit() {
    if (!this.username || !this.password) return;
    this.isLoading = true;
    this.errorMessage = '';
    
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        if (this.authService.isAdmin()) {
          this.router.navigate(['/admin-kyc']);
        } else {
          this.router.navigate(['/market-analysis']);
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Login failed';
        this.isLoading = false;
      }
    });
  }
}
