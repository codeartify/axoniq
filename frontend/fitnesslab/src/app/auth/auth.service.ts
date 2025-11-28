import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { OAuthService, AuthConfig } from 'angular-oauth2-oidc';
import { BehaviorSubject, Observable, firstValueFrom } from 'rxjs';
import { filter, map } from 'rxjs/operators';

export interface UserProfile {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  picture?: string;
}

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  id_token: string;
  token_type: string;
}

const authConfig: AuthConfig = {
  issuer: 'https://auth.oliverzihler.ch/realms/fitnesslab',
  redirectUri: window.location.origin + '/login',
  clientId: 'fitnesslab-app',
  scope: 'openid profile email',
  responseType: 'code',
  showDebugInformation: false,
  requireHttps: true,
  oidc: true,
  useSilentRefresh: true,
  silentRefreshRedirectUri: window.location.origin + '/silent-refresh.html',
};

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private userProfileSubject = new BehaviorSubject<UserProfile | null>(null);
  public userProfile$: Observable<UserProfile | null> = this.userProfileSubject.asObservable();
  private tokenEndpoint = 'https://auth.oliverzihler.ch/realms/fitnesslab/protocol/openid-connect/token';

  constructor(
    private oauthService: OAuthService,
    private router: Router,
    private http: HttpClient
  ) {
    this.configureOAuth();
  }

  private configureOAuth(): void {
    this.oauthService.configure(authConfig);

    // Try to restore session from storage
    this.oauthService.loadDiscoveryDocument().then(() => {
      this.oauthService.tryLogin().then(() => {
        if (this.oauthService.hasValidAccessToken()) {
          this.loadUserProfile();
        }
      });
    });
  }

  public async loginWithCredentials(username: string, password: string): Promise<void> {
    const body = new URLSearchParams();
    body.set('grant_type', 'password');
    if (authConfig.clientId != null) {
      body.set('client_id', authConfig.clientId);
    }
    body.set('username', username);
    body.set('password', password);
    body.set('scope', authConfig.scope || '');

    const response = await firstValueFrom(
      this.http.post<TokenResponse>(this.tokenEndpoint, body.toString(), {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      })
    );

    // Manually set tokens in OAuthService
    this.oauthService.getAccessToken = () => response.access_token;
    this.oauthService.getIdToken = () => response.id_token;
    this.oauthService.getRefreshToken = () => response.refresh_token;

    // Store tokens in sessionStorage for persistence
    sessionStorage.setItem('access_token', response.access_token);
    sessionStorage.setItem('id_token', response.id_token);
    sessionStorage.setItem('refresh_token', response.refresh_token);
    sessionStorage.setItem('expires_at', String(Date.now() + response.expires_in * 1000));

    // Load user profile
    this.loadUserProfile();
  }

  public login(): void {
    // This method is no longer used but kept for compatibility
    this.oauthService.initCodeFlow();
  }

  public logout(): void {
    this.oauthService.logOut();
    sessionStorage.clear();
    this.userProfileSubject.next(null);
    this.router.navigate(['/login']);
  }

  public isAuthenticated(): boolean {
    const token = sessionStorage.getItem('access_token');
    const expiresAt = sessionStorage.getItem('expires_at');

    if (!token || !expiresAt) {
      return false;
    }

    return Date.now() < parseInt(expiresAt);
  }

  public getAccessToken(): string {
    return sessionStorage.getItem('access_token') || '';
  }

  public loadUserProfile(): void {
    const accessToken = this.getAccessToken();
    const idToken = sessionStorage.getItem('id_token');

    if (!accessToken || !idToken) {
      return;
    }

    try {
      // Decode both tokens
      const accessPayload = JSON.parse(atob(accessToken.split('.')[1]));
      const idPayload = JSON.parse(atob(idToken.split('.')[1]));

      // Extract roles from access token
      const roles = this.extractRoles(accessPayload);

      // Extract user info from ID token
      const profile: UserProfile = {
        username: idPayload.preferred_username || idPayload.sub,
        email: idPayload.email || '',
        firstName: idPayload.given_name || '',
        lastName: idPayload.family_name || '',
        roles: roles,
        picture: idPayload.picture,
      };

      this.userProfileSubject.next(profile);
    } catch (error) {
      console.error('Error loading user profile:', error);
    }
  }

  private extractRoles(claims: any): string[] {
    console.log('Extracting roles from claims:', claims);
    const roles: string[] = [];

    // Try different locations where Keycloak might put roles
    if (claims.realm_access && claims.realm_access.roles) {
      console.log('Found roles in realm_access:', claims.realm_access.roles);
      roles.push(...claims.realm_access.roles);
    }

    // Also check resource_access
    if (claims.resource_access) {
      Object.keys(claims.resource_access).forEach(key => {
        if (claims.resource_access[key].roles) {
          console.log(`Found roles in resource_access.${key}:`, claims.resource_access[key].roles);
          roles.push(...claims.resource_access[key].roles);
        }
      });
    }

    // Check for roles claim directly
    if (claims.roles && Array.isArray(claims.roles)) {
      console.log('Found roles in claims.roles:', claims.roles);
      roles.push(...claims.roles);
    }

    console.log('Final extracted roles:', roles);
    return roles;
  }

  public hasRole(role: string): boolean {
    const profile = this.userProfileSubject.value;
    return profile ? profile.roles.includes(role) : false;
  }

  public hasAnyRole(roles: string[]): boolean {
    return roles.some((role) => this.hasRole(role));
  }

  public getUserProfile(): UserProfile | null {
    return this.userProfileSubject.value;
  }

  public get isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  public get isTrainer(): boolean {
    return this.hasRole('TRAINER');
  }

  public get isMember(): boolean {
    return this.hasRole('MEMBER');
  }
}
