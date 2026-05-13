import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService as Auth0AuthService} from '@auth0/auth0-angular';
import {switchMap} from 'rxjs';
import {environment} from '../../environments/environment';

const auth0Audience = 'https://www.oliverzihler.ch';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const isApiRequest = req.url.startsWith(`${environment.apiUrl}/`);
  const isPublicRequest = req.url === `${environment.apiUrl}/api/version` || req.url.includes('/i18n/');

  if (!isApiRequest || isPublicRequest) {
    return next(req);
  }

  return inject(Auth0AuthService).getAccessTokenSilently({
    authorizationParams: {
      audience: auth0Audience,
    },
  }).pipe(
    switchMap((token) =>
      next(req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      }))
    )
  );
};
