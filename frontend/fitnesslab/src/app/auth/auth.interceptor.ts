import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip adding token for OAuth/Keycloak endpoints and translation files
  if (req.url.includes('auth.oliverzihler.ch') ||
      req.url.includes('oauth') ||
      req.url.includes('/i18n/')) {
    return next(req);
  }

  const oauthService = inject(OAuthService);
  const token = oauthService.getAccessToken();

  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    return next(cloned);
  }

  return next(req);
};
