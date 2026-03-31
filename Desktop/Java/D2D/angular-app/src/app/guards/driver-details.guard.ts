import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { StorageService } from '../services/storage.service';
import { DriverService } from '../services/driver.service';
import { map, catchError, of } from 'rxjs';

export const driverDetailsGuard: CanActivateFn = (route, state) => {
    const storageService = inject(StorageService);
    const driverService = inject(DriverService);
    const router = inject(Router);

    const user = storageService.getUser();
    const roles = user?.roles || [];

    // Only apply this guard to drivers
    if (!roles.includes('DRIVER')) {
        return true;
    }

    // If user ID is missing, allow navigation (user needs to re-login)
    if (!user || !user.id) {
        console.warn('User ID not found in session. Please log in again.');
        return true; // Allow navigation, but they should re-login
    }

    // Check if driver has completed their details
    return driverService.getDriverByUserId(user.id).pipe(
        map(driver => {
            // If driver details exist, allow navigation
            if (driver && driver.id) {
                return true;
            }
            // If no driver details, redirect to details page
            console.log('Driver details not found, redirecting to /details');
            router.navigate(['/details']);
            return false;
        }),
        catchError((error) => {
            console.error('Error checking driver details:', error);
            // If error (likely 404 - no driver found), redirect to details page
            router.navigate(['/details']);
            return of(false);
        })
    );
};
