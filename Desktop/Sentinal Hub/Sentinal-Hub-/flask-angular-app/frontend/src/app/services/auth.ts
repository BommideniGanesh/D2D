import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';

export interface User {
  id: number;
  username: string;
  role: string;
  kyc_status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:5000/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    const user = localStorage.getItem('user');
    if (user) {
      this.currentUserSubject.next(JSON.parse(user));
    }
  }

  register(data: any) {
    return this.http.post(`${this.apiUrl}/register`, data);
  }

  login(data: any) {
    return this.http.post<any>(`${this.apiUrl}/login`, data).pipe(
      tap(response => {
        localStorage.setItem('access_token', response.access_token);
        localStorage.setItem('user', JSON.stringify(response.user));
        this.currentUserSubject.next(response.user);
      })
    );
  }

  logout() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  updateKycStatus(status: string) {
    const user = this.currentUserSubject.value;
    if (user) {
      user.kyc_status = status;
      localStorage.setItem('user', JSON.stringify(user));
      this.currentUserSubject.next({ ...user });
    }
  }

  get currentUserValue() {
    return this.currentUserSubject.value;
  }

  isLoggedIn() {
    return !!localStorage.getItem('access_token');
  }

  isAdmin() {
    return this.currentUserValue?.role === 'admin';
  }
}
