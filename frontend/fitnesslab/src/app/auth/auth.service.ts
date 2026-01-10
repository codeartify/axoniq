import {inject, Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {AuthConfig, OAuthService} from 'angular-oauth2-oidc';
import {BehaviorSubject, firstValueFrom, Observable} from 'rxjs';
import {toSignal} from '@angular/core/rxjs-interop';
import {environment} from '../../environments/environment';

export interface UserProfile {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  picture?: string;
}

interface RealmAccess {
  roles?: string[];
}

interface ResourceAccessEntry {
  roles?: string[];
}

type ResourceAccess = Record<string, ResourceAccessEntry>;

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  id_token: string;
  token_type: string;
}

const authConfig: AuthConfig = {
  issuer: environment.authIssuer,
  redirectUri: window.location.origin + '/login',
  clientId: environment.authClientId,
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
class AuthService {
  private userProfileSubject = new BehaviorSubject<UserProfile | null>(null);
  public userProfile$: Observable<UserProfile | null> = this.userProfileSubject.asObservable();
  private tokenEndpoint = `${environment.authIssuer}/protocol/openid-connect/token`;

  isLoggedIn = toSignal((this.userProfile$));

  roles = signal<string[]>([]);
  private oauthService = inject(OAuthService);
  private router = inject(Router);
  private http = inject(HttpClient);

  constructor() {
    this.configureOAuth();
  }

  private configureOAuth(): void {
    this.oauthService.configure(authConfig);

    // If tokens exist in sessionStorage, load profile immediately
    if (this.isAuthenticated()) {
      this.loadUserProfile();
    }

    // Try to restore session from OAuth storage
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
    this.roles.set([]);               // 🔹 clear roles
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
      const accessPayload = JSON.parse(atob(accessToken.split('.')[1]));
      const idPayload = JSON.parse(atob(idToken.split('.')[1]));

      // 1) Extract roles from access token
      const roles = this.extractRolesFromClaims(accessPayload);

      // 2) Update roles signal
      this.roles.set(roles);

      // 3) Build profile as before
      const profile: UserProfile = {
        username: idPayload.preferred_username || idPayload.sub,
        email: idPayload.email || '',
        firstName: idPayload.given_name || '',
        lastName: idPayload.family_name || '',
        roles,
        picture: idPayload.picture,
      };

      this.userProfileSubject.next(profile);
    } catch (error) {
      console.error(error);
    }
  }

private extractRolesFromClaims(claims: Record<string, unknown>): string[] {
  const roles: string[] = [];

  // Try different locations where Keycloak might put roles
  const realmAccess = claims['realm_access'] as RealmAccess | undefined;
  if (realmAccess && Array.isArray(realmAccess.roles)) {
    console.log('Found roles in realm_access:', realmAccess.roles);
    roles.push(...realmAccess.roles);
  }

  // Also check resource_access
  const resourceAccess = claims['resource_access'] as ResourceAccess | undefined;
  if (resourceAccess) {
    Object.keys(resourceAccess).forEach((key) => {
      const entry = resourceAccess[key];
      if (entry && Array.isArray(entry.roles)) {
        console.log(`Found roles in resource_access.${key}:`, entry.roles);
        roles.push(...entry.roles);
      }
    });
  }

  // Check for roles claim directly
  const directRoles = claims['roles'] as unknown;
  if (Array.isArray(directRoles)) {
    console.log('Found roles in claims.roles:', directRoles);
    roles.push(...directRoles);
  }

  console.log('Final extracted roles:', roles);
  return roles;
}

  public hasRole(role: string): boolean {
    return this.roles().includes(role);
  }

  public hasAnyRole(roles: string[]): boolean {
    const currentRoles = this.roles();
    console.log('Checking roles:', currentRoles, 'against:', roles);
    return roles.some((role) => currentRoles.includes(role));
  }
}

export default AuthService
