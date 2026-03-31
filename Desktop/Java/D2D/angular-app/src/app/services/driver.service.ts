import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Driver } from '../models/driver.model';

@Injectable({
    providedIn: 'root'
})
export class DriverService {
    private apiUrl = 'http://localhost:8080/api/drivers';

    constructor(private http: HttpClient) { }

    createDriver(driver: Driver): Observable<Driver> {
        return this.http.post<Driver>(this.apiUrl, driver);
    }

    getDriverByUserId(userId: string): Observable<Driver> {
        return this.http.get<Driver>(`${this.apiUrl}/user/${userId}`);
    }

    updateDriver(id: number, driver: Driver): Observable<Driver> {
        return this.http.put<Driver>(`${this.apiUrl}/${id}`, driver);
    }

    verifyDriver(id: number): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/${id}/verify`, {});
    }
}
