# Keycloak Client Setup for FitnessLab

Follow these steps to configure the Keycloak client on your server at `https://auth.oliverzihler.ch`.

## Step 1: Create the Realm

1. Go to https://auth.oliverzihler.ch/admin
2. Login with your admin credentials
3. Click the realm dropdown (top left, next to the Keycloak logo)
4. Click "Create Realm"
5. Enter:
   - **Realm name**: `fitnesslab`
6. Click "Create"

## Step 2: Create Realm Roles

1. In the `fitnesslab` realm, go to "Realm roles" in the left menu
2. Click "Create role"
3. Create these three roles (one at a time):
   - **Role name**: `ADMIN` (Description: Administrator with full access)
   - **Role name**: `TRAINER` (Description: Trainer with customer management access)
   - **Role name**: `MEMBER` (Description: Member with limited access)

## Step 3: Create the Client

1. Go to "Clients" in the left menu
2. Click "Create client"
3. On the "General Settings" page:
   - **Client type**: OpenID Connect
   - **Client ID**: `fitnesslab-app`
   - Click "Next"
4. On the "Capability config" page:
   - **Client authentication**: OFF (this makes it a public client)
   - **Authorization**: OFF
   - **Authentication flow**:
     - ✅ Standard flow (Authorization Code Flow)
     - ✅ Direct access grants
     - ❌ Implicit flow
     - ❌ Service accounts roles
   - Click "Next"
5. On the "Login settings" page:
   - **Root URL**: Leave empty
   - **Home URL**: Leave empty
   - **Valid redirect URIs**: Add these (click "+" to add each one):
     - `http://localhost:4200/*`
     - `http://localhost:8081/*`
   - **Valid post logout redirect URIs**: Add these:
     - `http://localhost:4200/*`
   - **Web origins**: Add these:
     - `http://localhost:4200`
     - `http://localhost:8081`
   - Click "Save"

## Step 4: Configure Client Settings (Additional)

1. In the client settings, go to the "Settings" tab
2. Scroll down and verify/set:
   - **Access Token Lifespan**: Leave default (5 minutes) or set to desired value
   - **Client Session Idle**: Leave default
   - **Client Session Max**: Leave default

## Step 5: Create an Admin User

1. Go to "Users" in the left menu
2. Click "Create new user"
3. Fill in:
   - **Username**: `admin` (or your preferred username)
   - **Email**: `admin@fitnesslab.ch`
   - **Email verified**: Toggle ON
   - **First name**: `Admin`
   - **Last name**: `User`
4. Click "Create"

5. After creating, you'll be on the user details page:
   - Go to the "Credentials" tab
   - Click "Set password"
   - Enter password: `admin123` (or your preferred password)
   - **Temporary**: Toggle OFF (so user doesn't need to change password)
   - Click "Save"

6. Go to the "Role mappings" tab
   - Click "Assign role"
   - Filter: Select "Filter by realm roles"
   - Check the box next to `ADMIN`
   - Click "Assign"

## Step 6: Verify Configuration

1. **Check Client Settings**:
   - Go to Clients → fitnesslab-app
   - Verify "Client authentication" is OFF
   - Verify redirect URIs include `http://localhost:4200/*`

2. **Test OIDC Configuration**:
   - Open in browser: https://auth.oliverzihler.ch/realms/fitnesslab/.well-known/openid-configuration
   - You should see a JSON response with endpoints

## Step 7: Test the Login

1. Start your FitnessLab application:
   ```bash
   # Terminal 1 - Backend
   mvn spring-boot:run

   # Terminal 2 - Frontend
   cd frontend/fitnesslab
   npm start
   ```

2. Open browser to http://localhost:4200
3. You should be redirected to the login page
4. Click "Sign In"
5. You should be redirected to Keycloak at `https://auth.oliverzihler.ch`
6. Login with your credentials (admin / admin123)
7. After successful login, you should be redirected back to http://localhost:4200
8. You should automatically navigate to the customers page

## Troubleshooting

### Error: "Invalid redirect_uri"
- **Cause**: The redirect URI doesn't match what's configured in Keycloak
- **Fix**: Verify `http://localhost:4200/*` is in the Valid redirect URIs list

### Error: "Client not found"
- **Cause**: Client ID mismatch
- **Fix**: Verify client ID is exactly `fitnesslab-app` (case-sensitive)

### Cannot access Keycloak admin console
- **Cause**: Keycloak server not running or network issue
- **Fix**: Verify https://auth.oliverzihler.ch is accessible

### User cannot login - "Invalid credentials"
- **Cause**: Password not set or user disabled
- **Fix**: Check user credentials tab and ensure user is enabled

### Backend returns 401 after successful login
- **Cause**: User doesn't have required role (ADMIN or TRAINER)
- **Fix**: Assign ADMIN or TRAINER role to the user in Keycloak

## Configuration Summary

When complete, your Keycloak configuration should have:

✅ Realm: `fitnesslab`
✅ Roles: `ADMIN`, `TRAINER`, `MEMBER`
✅ Client: `fitnesslab-app` (public client, standard flow enabled)
✅ Redirect URIs: `http://localhost:4200/*`, `http://localhost:8081/*`
✅ Web Origins: `http://localhost:4200`, `http://localhost:8081`
✅ User: At least one user with ADMIN or TRAINER role

## Console Logs After Successful Login

When everything works correctly, you should see logs like:
```
Configuring OAuth...
Current URL: http://localhost:4200/?code=abc123...
OAuth Event: OAuthSuccessEvent {type: 'discovery_document_loaded'}
OAuth Event: OAuthSuccessEvent {type: 'token_received'}
Discovery document loaded, login attempt result: true
Has valid access token: true
Access token: eyJhbGci...
User authenticated, current route: /
Redirecting to /customers
```
