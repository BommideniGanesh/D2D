import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  isAdmin = false;
  errorMessage = '';
  isLoading = false;

  onSubmit() {
    if (!this.username || !this.password) return;
    this.isLoading = true;
    this.errorMessage = '';
    
    this.authService.register({ username: this.username, password: this.password, is_admin: this.isAdmin }).subscribe({
      next: () => {
        this.router.navigate(['/login']); 
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Registration failed';
        this.isLoading = false;
      }
    });
  }
}
