import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { UserService } from '../services/user.service';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
    templateUrl: './register.component.html',
    styleUrl: './register.component.css',
})
export class RegisterComponent {
    registerForm: FormGroup;
    passwordStrength: 'Weak' | 'Medium' | 'Strong' | '' = '';
    passwordStrengthClass: string = '';
    showRegisterPassword = false;

    // Toast State
    showToast = false;
    toastMessage = '';
    toastType: 'success' | 'error' = 'success';

    // Roles List
    roles = ['USER', 'DRIVER', 'ADMIN', 'SUPPORT'];

    otpStep: 'form' | 'verify' = 'form';
    otpCode: string = '';
    otpLoading: boolean = false;

    constructor(
        private fb: FormBuilder,
        private userService: UserService,
        private router: Router
    ) {
        this.registerForm = this.fb.group({
            name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z ]+$/)]],
            email: ['', [Validators.required, Validators.email]],
            role: ['USER', Validators.required],
            phone: ['', [Validators.required, Validators.pattern(/^[0-9]+$/)]],
            password: ['', [Validators.required, Validators.minLength(12), this.createPasswordValidator()]],
            confirmPassword: ['', Validators.required],
            terms: [false, Validators.requiredTrue]
        }, { validators: this.passwordMatchValidator });
    }

    passwordMatchValidator(g: FormGroup) {
        return g.get('password')?.value === g.get('confirmPassword')?.value
            ? null : { mismatch: true };
    }

    // Custom validator and strength calculator
    createPasswordValidator() {
        return (control: AbstractControl): ValidationErrors | null => {
            const value = control.value;
            if (!value) return null;

            const hasUpper = /[A-Z]+/.test(value);
            const hasLower = /[a-z]+/.test(value);
            const hasNumeric = /[0-9]+/.test(value);
            const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+/.test(value);
            const isValidLength = value.length >= 12;

            // Calculate strength for UI
            let strengthPoints = 0;
            if (isValidLength) strengthPoints++;
            if (hasUpper && hasLower) strengthPoints++;
            if (hasNumeric) strengthPoints++;
            if (hasSpecial) strengthPoints++;

            if (strengthPoints < 2) {
                this.passwordStrength = 'Weak';
                this.passwordStrengthClass = 'text-danger';
            } else if (strengthPoints < 4) {
                this.passwordStrength = 'Medium';
                this.passwordStrengthClass = 'text-warning';
            } else {
                this.passwordStrength = 'Strong';
                this.passwordStrengthClass = 'text-success';
            }

            const passwordValid = hasUpper && hasLower && hasNumeric && hasSpecial && isValidLength;

            return !passwordValid ? { passwordCompromised: true } : null;
        };
    }

    get r() { return this.registerForm.controls; }

    sendOtpAndProceed() {
        if (this.registerForm.invalid) {
            this.registerForm.markAllAsTouched();
            return;
        }

        this.otpLoading = true;
        const email = this.registerForm.value.email;
        const phone = this.registerForm.value.phone;

        this.userService.sendOtp(email, phone).subscribe({
            next: () => {
                this.otpLoading = false;
                this.otpStep = 'verify';
                this.showToastMessage('OTP sent to your email!', 'success');
            },
            error: (err) => {
                this.otpLoading = false;
                this.showToastMessage(err.error?.message || 'Failed to send OTP', 'error');
            }
        });
    }

    verifyOtpAndRegister() {
        if (!this.otpCode || this.otpCode.length < 6) {
            this.showToastMessage('Please enter the 6-digit OTP', 'error');
            return;
        }

        this.otpLoading = true;
        const email = this.registerForm.value.email;

        this.userService.verifyOtp(email, this.otpCode).subscribe({
            next: (res) => {
                if (res.valid) {
                    this.confirmRegister();
                } else {
                    this.otpLoading = false;
                    this.showToastMessage('Invalid or expired OTP', 'error');
                }
            },
            error: () => {
                this.otpLoading = false;
                this.showToastMessage('Invalid or expired OTP', 'error');
            }
        });
    }

    confirmRegister() {
        const formVal = this.registerForm.value;
        const payload = {
            name: formVal.name,
            email: formVal.email,
            phone: formVal.phone,
            password: formVal.password,
            acceptedTerms: formVal.terms,
            role: formVal.role
        };

        this.userService.register(payload).subscribe({
            next: (response) => {
                this.otpLoading = false;
                this.showToastMessage('Registration Successful! Redirecting to login...', 'success');
                this.registerForm.reset();
                setTimeout(() => this.router.navigate(['/login']), 1500);
            },
            error: (error) => {
                this.otpLoading = false;
                let errorMessage = 'Registration Failed. Please try again.';

                if (error.status === 409 && error.error && error.error.message) {
                    errorMessage = error.error.message;
                } else if (error.error && error.error.message) {
                    errorMessage = error.error.message;
                }

                this.showToastMessage(errorMessage, 'error');
            }
        });
    }

    showToastMessage(msg: string, type: 'success' | 'error') {
        this.toastMessage = msg;
        this.toastType = type;
        this.showToast = true;
        setTimeout(() => this.showToast = false, 3000); // Hide after 3s
    }
}
