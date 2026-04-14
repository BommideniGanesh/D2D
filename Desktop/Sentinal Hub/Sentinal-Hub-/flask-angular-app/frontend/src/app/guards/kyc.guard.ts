import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth';
import { map, take } from 'rxjs';

export const kycGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // If user is admin, allow, but redirect from dashboard to admin-kyc
  if (authService.isAdmin()) {
    if (state.url === '/dashboard') {
      return router.createUrlTree(['/admin-kyc']);
    }
    return true;
  }

  // Otherwise, user must have Approved KYC
  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      if (!user) return router.createUrlTree(['/login']);
      
      const status = user.kyc_status || 'Pending';
      if (status === 'Approved') {
        return true;
      }
      
      if (status === 'Submitted') {
        return router.createUrlTree(['/kyc-pending']);
      }
      
      return router.createUrlTree(['/kyc-details']);
    })
  );
};
