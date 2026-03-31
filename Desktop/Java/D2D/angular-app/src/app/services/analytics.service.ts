import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AdminDashboardDTO {
    totalUsers: number;
    totalDeliveredOrders: number;
    activeDrivers: number;
    revenue: number;
    totalDamagedOrders: number;
    ordersGraphData: Array<{ date: string; orders: number }>;
    driverStatusStats: { [key: string]: number };
}

@Injectable({
    providedIn: 'root'
})
export class AnalyticsService {
    private apiUrl = 'http://localhost:8080/api/analytics';

    constructor(private http: HttpClient) { }

    getDashboardData(): Observable<AdminDashboardDTO> {
        return this.http.get<AdminDashboardDTO>(`${this.apiUrl}/dashboard`);
    }

    getActiveUsers(): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/users/active`);
    }

    getActiveDrivers(): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/drivers/active`);
    }

    getDeliveredOrders(): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/shipments/delivered`);
    }

    getDamagedOrReturnedOrders(): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/shipments/damaged-returns`);
    }
}
