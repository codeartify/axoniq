# Authentication Setup Guide

This guide explains how to set up and use the authentication system for FitnessLab.

## Overview

The application uses:
- **Keycloak** at `https://auth.oliverzihler.ch` as the authentication provider (OpenID Connect/OAuth2)
- **Spring Security** with OAuth2 Resource Server for backend protection
- **Angular OAuth2 OIDC** for frontend authentication

## Roles

Three roles are configured:
- **ADMIN**: Full access to all features
- **TRAINER**: Access to customer and product management
- **MEMBER**: Limited access (not yet implemented in UI)

## Prerequisites

Your Keycloak server at `https://auth.oliverzihler.ch` must have:
1. A realm named `fitnesslab`
2. Three realm roles: `ADMIN`, `TRAINER`, `MEMBER`
3. A client named `fitnesslab-app` configured as:
   - Client Protocol: `openid-connect`
   - Access Type: `public`
   - Valid Redirect URIs:
     - `http://localhost:4200/*`
     - `http://localhost:8081/*`
     - `https://yourdomain.com/*` (for production)
   - Web Origins: `+` (or specify origins)
   - Direct Access Grants Enabled: `ON`
   - Standard Flow Enabled: `ON`
4. At least one user with `ADMIN` or `TRAINER` role assigned

## Setup Instructions

### 1. Configure Keycloak Realm

#### Option A: Import Pre-configured Realm

1. Access Keycloak Admin Console: https://auth.oliverzihler.ch
2. Login with admin credentials:
   - Username: `admin`
   - Password: `admin`
3. Click "Create Realm" or the realm dropdown
4. Click "Import" and select `/Users/ozihler/axoniq/axoniq/keycloak/fitnesslab-realm.json`
5. Click "Create"
6. Update the client's redirect URIs if needed for your environment

#### Option B: Manual Configuration

1. **Create Realm**:
   - Go to https://auth.oliverzihler.ch/admin
   - Click "Create Realm"
   - Name: `fitnesslab`
   - Click "Create"

2. **Create Roles**:
   - Go to "Realm roles"
   - Create three roles: `ADMIN`, `TRAINER`, `MEMBER`

3. **Create Client**:
   - Go to "Clients" → "Create client"
   - Client ID: `fitnesslab-app`
   - Client Protocol: `openid-connect`
   - Click "Next"
   - Standard flow: `ON`
   - Direct access grants: `ON`
   - Click "Save"
   - In "Settings" tab:
     - Valid redirect URIs: `http://localhost:4200/*`, `http://localhost:8081/*`
     - Web origins: `http://localhost:4200`, `http://localhost:8081`
   - Click "Save"

4. **Create Admin User**:
   - Go to "Users" → "Add user"
   - Username: `admin`
   - Email: `admin@fitnesslab.ch`
   - Email verified: `ON`
   - First name: `Admin`
   - Last name: `User`
   - Click "Create"
   - Go to "Credentials" tab
   - Click "Set password"
   - Password: `admin123`
   - Temporary: `OFF`
   - Click "Save"
   - Go to "Role mappings" tab
   - Assign realm role: `ADMIN`

### 2. Start Local Docker Containers

```bash
cd /Users/ozihler/axoniq/axoniq
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- AxonServer (ports 8024, 8124)
- MailHog (ports 1025, 8025)

Note: Keycloak is NOT started locally - we use your existing server at auth.oliverzihler.ch

### 3. Install Frontend Dependencies

```bash
cd frontend/fitnesslab
npm install
```

This installs the new `angular-oauth2-oidc` dependency.

### 4. Start the Application

#### Backend
```bash
# From project root
mvn spring-boot:run
```

The backend will start on port 8081 and connect to:
- PostgreSQL for data storage
- AxonServer for event sourcing
- Keycloak at auth.oliverzihler.ch for JWT validation

#### Frontend
```bash
cd frontend/fitnesslab
npm start
```

The frontend will start on http://localhost:4200

### 5. Login

1. Navigate to http://localhost:4200
2. You will be automatically redirected to the login page
3. Click "Sign In"
4. You will be redirected to Keycloak at https://auth.oliverzihler.ch
5. Enter credentials (e.g., admin / admin123)
6. After successful login, you'll be redirected back to the application

## Configuration Files

### Backend Configuration
**File**: `src/main/resources/application.yml`
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.oliverzihler.ch/realms/fitnesslab
          jwk-set-uri: https://auth.oliverzihler.ch/realms/fitnesslab/protocol/openid-connect/certs
```

### Frontend Configuration
**File**: `frontend/fitnesslab/src/app/auth/auth.service.ts`
```typescript
const authConfig: AuthConfig = {
  issuer: 'https://auth.oliverzihler.ch/realms/fitnesslab',
  redirectUri: window.location.origin,
  clientId: 'fitnesslab-app',
  scope: 'openid profile email',
  responseType: 'code',
  requireHttps: true,
};
```

## Creating Additional Users

1. Go to Keycloak Admin Console: https://auth.oliverzihler.ch/admin
2. Select "fitnesslab" realm
3. Go to "Users" → "Add user"
4. Fill in user details:
   - Username
   - Email
   - First/Last name
   - Email verified: ON
5. Click "Create"
6. Go to "Credentials" tab
7. Click "Set password"
8. Enter password and set "Temporary" to OFF
9. Go to "Role mappings" tab
10. Assign realm roles (ADMIN, TRAINER, or MEMBER)

## Architecture

### Backend Security

**SecurityConfig.kt** configures:
- JWT validation using Keycloak's public keys from auth.oliverzihler.ch
- CORS for localhost:4200 and localhost:8081
- Role-based access control on all `/api/**` endpoints
- Stateless session management

All REST controllers require authentication and ADMIN or TRAINER role.

### Frontend Security

**AuthService** handles:
- OAuth2 code flow with PKCE
- Token management and refresh
- User profile extraction from JWT claims
- Role checking

**authGuard** protects routes:
- Checks authentication status
- Verifies required roles
- Redirects to login if needed

**authInterceptor** adds:
- Bearer token to all API requests
- Skips token for OAuth endpoints

**Login flow**:
1. User visits protected route
2. authGuard redirects to /login
3. User clicks "Sign In"
4. Redirected to auth.oliverzihler.ch
5. After authentication, redirected back with code
6. Angular OAuth2 OIDC exchanges code for tokens
7. Tokens stored in memory
8. User profile loaded from JWT
9. Redirected to original route

## Troubleshooting

### "401 Unauthorized" on API calls

**Check**:
1. User is logged in (check browser console for token)
2. Token is being sent (check Network tab → Request Headers → Authorization)
3. Backend can reach auth.oliverzihler.ch (check backend logs)
4. User has correct role (ADMIN or TRAINER)
5. Realm name is correct: `fitnesslab`

**Fix**:
- Verify realm exists at https://auth.oliverzihler.ch/realms/fitnesslab
- Check realm roles are assigned to user
- Restart backend if configuration changed

### Redirect loop

**Check**:
1. Keycloak client redirect URIs include your application URL
2. Frontend OAuth config matches Keycloak client ID
3. Client is public and has standard flow enabled

### CORS errors

**Check**:
1. SecurityConfig allows your frontend origin
2. Keycloak client has Web Origins configured
3. No @CrossOrigin annotations remain on controllers

### Cannot reach auth.oliverzihler.ch

**Check**:
1. DNS resolves: `nslookup auth.oliverzihler.ch`
2. HTTPS certificate is valid
3. Server is running: `curl https://auth.oliverzihler.ch/realms/fitnesslab/.well-known/openid-configuration`

**Fix**: Verify your Keycloak server is accessible and properly configured.

### Token validation fails

**Check**:
1. Issuer URI matches in backend config and Keycloak realm
2. JWK Set URI is accessible from backend
3. Backend logs for JWT validation errors

**Fix**: Verify URLs in application.yml match your Keycloak realm URLs exactly.

## Production Deployment

When deploying to production:

1. **Update Redirect URIs** in Keycloak client to include production URLs
2. **Update Backend Configuration** to allow production CORS origin
3. **Environment Variables**: Use env vars for sensitive config
4. **HTTPS**: Ensure all connections use HTTPS
5. **Token Lifespan**: Review and adjust in Keycloak realm settings
6. **Security**: Enable Keycloak security features (brute force protection, etc.)

## Security Notes

- All passwords should be changed in production
- Use environment-specific configuration files
- Keep `showDebugInformation: false` in production
- Regular security updates for Keycloak
- Monitor authentication logs for suspicious activity
