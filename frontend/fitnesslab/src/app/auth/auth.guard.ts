import {inject} from '@angular/core';
import {Router, CanActivateFn} from '@angular/router';
import {combineLatest, filter, map, take} from 'rxjs';
import AuthService from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return combineLatest([
    authService.isLoading$,
    authService.isAuthenticated$,
    authService.userProfile$,
  ]).pipe(
    filter(([isLoading]) => !isLoading),
    take(1),
    map(([, isAuthenticated, profile]) => {
      if (!isAuthenticated) {
        authService.login(state.url);
        return false;
      }

      const requiredRoles = route.data['roles'] as string[] | undefined;
      if (requiredRoles?.length && !requiredRoles.some((role) => profile?.roles.includes(role))) {
        return router.parseUrl('/unauthorized');
      }

      return true;
    })
  );
};
