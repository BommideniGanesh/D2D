import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { StorageService } from '../services/storage.service';

@Injectable({
    providedIn: 'root'
})
export class RoleGuard implements CanActivate {

    constructor(private storageService: StorageService, private router: Router) { }

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

        // Get roles required for the route
        const requiredRoles = route.data['roles'] as string[];

        // Get current user's roles
        const user = this.storageService.getUser();
        const userRoles = user ? user.roles : [];

        // Check if user has at least one of the required roles
        if (!userRoles || !requiredRoles) {
            // If no roles required or no user roles, decide policy. 
            // For now, if route requires roles and user has none, redirect.
            if (requiredRoles && requiredRoles.length > 0) {
                this.router.navigate(['/']);
                return false;
            }
            return true;
        }

        // Check intersection
        const hasRole = requiredRoles.some(role => userRoles.includes(role));

        if (hasRole) {
            return true;
        } else {
            this.router.navigate(['/']); // Redirect to home or unauthorized page
            return false;
        }
    }
}
