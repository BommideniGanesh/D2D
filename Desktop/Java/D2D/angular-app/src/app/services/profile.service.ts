import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ProfileService {
    private apiUrl = 'http://localhost:8080/users';

    constructor(private http: HttpClient) { }

    updateProfile(profileData: { name: string, email: string, phone: string }): Observable<any> {
        return this.http.put(`${this.apiUrl}/profile`, profileData);
    }

    // To fetch the full profile if needed instead of relying just on the token claims
    getProfile(userId: string): Observable<any> {
        return this.http.get(`${this.apiUrl}/${userId}`);
    }
}
