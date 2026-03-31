import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subject } from 'rxjs';
import { StorageService } from './storage.service';
import { jwtDecode } from 'jwt-decode';
import { User } from '../models/user.model';

export interface ToastMessage {
    message: string;
    type: 'success' | 'error';
}

@Injectable({
    providedIn: 'root'
})
export class LoginService {
    private apiUrl = 'http://localhost:8080/auth/login';

    // Component subscribes to this
    public toast$ = new Subject<ToastMessage>();

    constructor(private http: HttpClient, private storageService: StorageService) { }

    login(credentials: any): void {
        this.http.post(this.apiUrl, credentials).subscribe({
            next: (response: any) => {
                const token = response.token || response.jwt || response.accessToken;

                if (token) {
                    this.storageService.saveToken(token);

                    // Decode token to get user info (roles, email)
                    try {
                        const decodedToken: any = jwtDecode(token);
                        const user: User = {
                            id: decodedToken.userId || decodedToken.id || decodedToken.sub,
                            email: decodedToken.sub,
                            roles: decodedToken.roles
                        };
                        this.storageService.saveUser(user);

                        // Attempt to save username if present (or name/email)
                        const username = response.username || response.name || response.email || decodedToken.sub;
                        if (username) {
                            this.storageService.saveUsername(username);
                        }

                    } catch (e) {
                        console.error('Error decoding token', e);
                    }

                    this.toast$.next({ message: 'User Login Successful', type: 'success' });
                } else {
                    this.toast$.next({ message: 'User Login Successful', type: 'success' });
                }
            },
            error: (error) => {
                console.error('Login Error:', error);
                this.toast$.next({ message: 'User Login Failed', type: 'error' });
            }
        });
    }

    forgotPassword(payload: { email: string; newPassword: string }) {
        return this.http.post('http://localhost:8080/auth/forgot-password', payload, { responseType: 'text' });
    }
}

