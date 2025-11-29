import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import AuthService from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // If URL contains OAuth callback parameters, allow navigation so they can be processed
  const url = state.url;
  if (url.includes('code=') || url.includes('state=') || url.includes('session_state=')) {
    console.log('Auth guard: OAuth callback detected, allowing navigation');
    return true;
  }

  if (authService.isAuthenticated()) {
    // Check for required roles
    const requiredRoles = route.data['roles'] as string[] | undefined;
    if (requiredRoles && requiredRoles.length > 0) {
      if (authService.hasAnyRole(requiredRoles)) {
        return true;
      } else {
        router.navigate(['/unauthorized']);
        return false;
      }
    }
    return true;
  } else {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
};
