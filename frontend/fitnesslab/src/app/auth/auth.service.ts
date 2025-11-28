import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthService, AuthConfig } from 'angular-oauth2-oidc';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

export interface UserProfile {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  picture?: string;
}

const authConfig: AuthConfig = {
  issuer: 'https://auth.oliverzihler.ch/realms/fitnesslab',
  redirectUri: window.location.origin + '/login',
  clientId: 'fitnesslab-app',
  scope: 'openid profile email',
  responseType: 'code',
  showDebugInformation: true,
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
  private isInitialized = new BehaviorSubject<boolean>(false);
  public isInitialized$ = this.isInitialized.asObservable();

  constructor(
    private oauthService: OAuthService,
    private router: Router
  ) {
    this.configureOAuth();
  }

  private configureOAuth(): void {
    console.log('Configuring OAuth...');
    console.log('Current URL:', window.location.href);
    console.log('Redirect URI configured:', authConfig.redirectUri);
    console.log('Client ID:', authConfig.clientId);
    console.log('Issuer:', authConfig.issuer);

    this.oauthService.configure(authConfig);

    // Add event listeners to see what's happening
    this.oauthService.events.subscribe(event => {
      console.log('OAuth Event:', event);
      if (event.type === 'token_error' || event.type === 'code_error') {
        console.error('OAuth Error Event:', event);
      }
    });

    // First load discovery document
    this.oauthService.loadDiscoveryDocument()
      .then(() => {
        console.log('Discovery document loaded');
        // Then try to login (exchange code if present)
        return this.oauthService.tryLogin();
      })
      .then(() => {
        console.log('Login attempt completed');
        console.log('Has valid access token:', this.oauthService.hasValidAccessToken());
        console.log('Access token:', this.oauthService.getAccessToken());
        console.log('ID token:', this.oauthService.getIdToken());
        console.log('Identity claims:', this.oauthService.getIdentityClaims());

        if (this.oauthService.hasValidAccessToken()) {
          this.loadUserProfile();
          console.log('User authenticated, current route:', this.router.url);

          // Setup silent refresh after successful login
          this.oauthService.setupAutomaticSilentRefresh();

          // Redirect to main app if we're on the login page after successful auth
          if (this.router.url === '/login' || this.router.url === '/' || this.router.url.includes('code=')) {
            console.log('Redirecting to /customers');
            this.router.navigate(['/customers']);
          }
        } else {
          console.log('No valid access token after login attempt');
          console.log('Current URL after tryLogin:', window.location.href);
          console.log('URL has code param:', window.location.href.includes('code='));
        }

        this.isInitialized.next(true);
      })
      .catch((error) => {
        console.error('Error during OAuth initialization:', error);
        this.isInitialized.next(true);
      });
  }

  public login(): void {
    console.log('login() called - initiating code flow');
    console.log('Discovery document loaded?', this.oauthService.discoveryDocumentLoaded);
    this.oauthService.initCodeFlow();
    console.log('initCodeFlow() completed - should redirect to Keycloak now');
  }

  public logout(): void {
    this.oauthService.logOut();
    this.userProfileSubject.next(null);
    this.router.navigate(['/login']);
  }

  public isAuthenticated(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

  public getAccessToken(): string {
    return this.oauthService.getAccessToken();
  }

  public loadUserProfile(): void {
    const claims = this.oauthService.getIdentityClaims() as any;
    console.log('Loading user profile from claims:', claims);

    if (claims) {
      // Get roles from the access token instead of ID token
      const accessToken = this.oauthService.getAccessToken();
      let roles: string[] = [];

      if (accessToken) {
        try {
          // Decode the access token JWT to get the payload
          const payload = JSON.parse(atob(accessToken.split('.')[1]));
          console.log('Access token payload:', payload);
          roles = this.extractRoles(payload);
        } catch (error) {
          console.error('Error decoding access token:', error);
          // Fallback to ID token claims
          roles = this.extractRoles(claims);
        }
      } else {
        roles = this.extractRoles(claims);
      }

      console.log('Extracted roles:', roles);

      const profile: UserProfile = {
        username: claims.preferred_username || claims.sub,
        email: claims.email || '',
        firstName: claims.given_name || '',
        lastName: claims.family_name || '',
        roles: roles,
        picture: claims.picture,
      };
      console.log('User profile:', profile);
      this.userProfileSubject.next(profile);
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
