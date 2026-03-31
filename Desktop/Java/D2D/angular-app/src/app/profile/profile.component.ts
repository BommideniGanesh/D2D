import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileService } from '../services/profile.service';
import { StorageService } from '../services/storage.service';
import { LoaderService } from '../services/loader.service';
import { User } from '../models/user.model';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './profile.component.html',
    styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
    profileForm!: FormGroup;
    currentUser: User | null = null;
    loading = false;
    showToast = false;
    toastMessage = '';
    toastType: 'success' | 'error' = 'success';

    constructor(
        private fb: FormBuilder,
        private profileService: ProfileService,
        private storageService: StorageService,
        private loaderService: LoaderService
    ) { }

    ngOnInit(): void {
        this.currentUser = this.storageService.getUser();

        this.profileForm = this.fb.group({
            name: ['', Validators.required],
            email: ['', [Validators.required, Validators.email]],
            phone: ['', Validators.required]
        });

        if (this.currentUser && this.currentUser.id) {
            this.loaderService.show();
            this.profileService.getProfile(this.currentUser.id).subscribe({
                next: (data) => {
                    this.profileForm.patchValue({
                        name: data.name,
                        email: data.email,
                        phone: data.phone
                    });
                    this.loaderService.hide();
                },
                error: (err) => {
                    console.error('Failed to load profile', err);
                    this.loaderService.hide();
                    this.showToastMessage('Failed to load profile data', 'error');
                }
            });
        }
    }

    get f() { return this.profileForm.controls; }

    onSubmit(): void {
        if (this.profileForm.invalid) {
            return;
        }

        this.loaderService.show();
        this.profileService.updateProfile(this.profileForm.value).subscribe({
            next: (updatedUser) => {
                this.loaderService.hide();
                this.showToastMessage('Profile updated successfully!', 'success');

                // Update local storage if needed (optional since we mostly rely on JWT, but good for local cache)
                if (this.currentUser) {
                    this.currentUser.email = updatedUser.email;
                    this.storageService.saveUser(this.currentUser);
                    // Optionally update username cache if name is stored there
                    if (updatedUser.name) {
                        this.storageService.saveUsername(updatedUser.name);
                    }
                }
            },
            error: (err) => {
                this.loaderService.hide();
                this.showToastMessage(err.error?.message || 'Failed to update profile. Email or Phone might be taken.', 'error');
            }
        });
    }

    showToastMessage(msg: string, type: 'success' | 'error') {
        this.toastMessage = msg;
        this.toastType = type;
        this.showToast = true;
        setTimeout(() => this.showToast = false, 3000);
    }
}
