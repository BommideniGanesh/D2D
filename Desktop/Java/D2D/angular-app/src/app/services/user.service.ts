import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    // Using the literal URL as requested by the user, or I could derive it from environment if aligned.
    // User req: http://localhost:8080/users/register
    private apiUrl = 'http://localhost:8080/users/register';

    constructor(private http: HttpClient) { }

    register(userData: any): Observable<any> {
        return this.http.post(this.apiUrl, userData);
    }

    sendOtp(email: string, phone?: string): Observable<any> {
        return this.http.post('http://localhost:8080/auth/otp/send', { email, phone });
    }

    verifyOtp(email: string, code: string): Observable<any> {
        return this.http.post('http://localhost:8080/auth/otp/verify', { email, code });
    }
}
