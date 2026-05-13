import {Injectable, inject} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {AuthService as Auth0AuthService} from '@auth0/auth0-angular';
import type {IdToken, User} from '@auth0/auth0-spa-js';
import {Observable, catchError, combineLatest, map, of, shareReplay, switchMap} from 'rxjs';

export interface UserProfile {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  picture?: string;
}

@Injectable({
  providedIn: 'root',
})
class AuthService {
  private auth0 = inject(Auth0AuthService);

  isLoading$ = this.auth0.isLoading$;
  isAuthenticated$ = this.auth0.isAuthenticated$;
  error$ = this.auth0.error$;

  userProfile$: Observable<UserProfile | null> = combineLatest([
    this.auth0.user$,
    this.auth0.idTokenClaims$,
    this.auth0.isAuthenticated$.pipe(
      switchMap((isAuthenticated) =>
        isAuthenticated
          ? this.auth0.getAccessTokenSilently().pipe(
              map((token) => this.decodeJwtPayload(token)),
              catchError(() => of({}))
            )
          : of({})
      )
    ),
  ]).pipe(
    map(([user, idTokenClaims, accessTokenClaims]) =>
      user ? this.toUserProfile(user, idTokenClaims, accessTokenClaims) : null
    ),
    shareReplay({bufferSize: 1, refCount: true})
  );

  isLoggedIn = toSignal(this.isAuthenticated$, {initialValue: false});
  roles = toSignal(
    this.userProfile$.pipe(map((profile) => profile?.roles ?? [])),
    {initialValue: []}
  );

  public login(returnTo = '/dashboard'): void {
    this.auth0.loginWithRedirect({appState: {target: returnTo}}).subscribe();
  }

  public signup(returnTo = '/dashboard'): void {
    this.auth0.loginWithRedirect({
      appState: {target: returnTo},
      authorizationParams: {screen_hint: 'signup'},
    }).subscribe();
  }

  public logout(): void {
    this.auth0.logout({logoutParams: {returnTo: `${window.location.origin}/`}}).subscribe();
  }

  public isAuthenticated(): boolean {
    return this.isLoggedIn();
  }

  public getAccessToken(): Observable<string> {
    return this.auth0.getAccessTokenSilently();
  }

  public hasRole(role: string): boolean {
    return this.roles().includes(role);
  }

  public hasAnyRole(roles: string[]): boolean {
    const currentRoles = this.roles();
    return roles.some((role) => currentRoles.includes(role));
  }

  private toUserProfile(
    user: User,
    idTokenClaims?: IdToken | null,
    accessTokenClaims: Record<string, unknown> = {}
  ): UserProfile {
    const roles = this.extractRolesFromClaims(
      user as Record<string, unknown>,
      (idTokenClaims ?? {}) as Record<string, unknown>,
      accessTokenClaims
    );

    return {
      username: user.nickname || user.name || user.email || user.sub || '',
      email: user.email || '',
      firstName: user.given_name || '',
      lastName: user.family_name || '',
      roles,
      picture: user.picture,
    };
  }

  private extractRolesFromClaims(...claimSets: Record<string, unknown>[]): string[] {
    const roleClaims = claimSets.flatMap((claims) => [
      claims['roles'],
      claims['permissions'],
      ...Object.entries(claims)
        .filter(([key]) => key.endsWith('/roles') || key.endsWith('/permissions'))
        .map(([, value]) => value),
    ]);

    return [...new Set(roleClaims.flatMap((claim) => (Array.isArray(claim) ? claim : [])))]
      .filter((role): role is string => typeof role === 'string');
  }

  private decodeJwtPayload(token: string): Record<string, unknown> {
    const payload = token.split('.')[1];
    if (!payload) {
      return {};
    }

    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
    const paddedPayload = normalizedPayload.padEnd(
      normalizedPayload.length + (4 - normalizedPayload.length % 4) % 4,
      '='
    );

    return JSON.parse(atob(paddedPayload)) as Record<string, unknown>;
  }
}

export default AuthService;
