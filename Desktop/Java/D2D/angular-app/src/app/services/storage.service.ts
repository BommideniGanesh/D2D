import { Injectable } from '@angular/core';

const USER_KEY = 'auth-user';
const TOKEN_KEY = 'auth-token';

@Injectable({
    providedIn: 'root'
})
export class StorageService {
    constructor() { }

    clean(): void {
        window.sessionStorage.clear();
    }

    public saveUser(user: any): void {
        window.sessionStorage.removeItem(USER_KEY);
        window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    }

    public getUser(): any {
        const user = window.sessionStorage.getItem(USER_KEY);
        if (user) {
            return JSON.parse(user);
        }
        return {};
    }

    public saveToken(token: string): void {
        window.sessionStorage.removeItem(TOKEN_KEY);
        window.sessionStorage.setItem(TOKEN_KEY, token);
    }

    public getToken(): string | null {
        return window.sessionStorage.getItem(TOKEN_KEY);
    }

    public isLoggedIn(): boolean {
        const token = window.sessionStorage.getItem(TOKEN_KEY);
        return !!token;
    }

    public saveUsername(username: string): void {
        window.sessionStorage.removeItem('auth-username');
        window.sessionStorage.setItem('auth-username', username);
    }

    public getUsername(): string | null {
        return window.sessionStorage.getItem('auth-username');
    }
}
