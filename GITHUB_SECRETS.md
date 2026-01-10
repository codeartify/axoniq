# Required GitHub Secrets

Add these secrets to your GitHub repository at Settings → Secrets and variables → Actions → Repository secrets.

## Docker Hub
- `DOCKERHUB_USERNAME` - Your Docker Hub username
- `DOCKERHUB_TOKEN` - Docker Hub access token
- `DOCKER_BACKEND_IMAGE_NAME` - e.g., `yourusername/fitnesslab-backend`
- `DOCKER_FRONTEND_IMAGE_NAME` - e.g., `yourusername/fitnesslab-frontend`

## SSH Deployment
- `SSH_HOST` - Server hostname or IP address
- `SSH_PORT` - SSH port (usually 22)
- `SSH_USER` - SSH username
- `SSH_PASS` - SSH password
- `DEPLOY_PATH` - Deployment directory on server, e.g., `/home/user/fitnesslab`
- `DOCKER_PATH_ON_SERVER` - Path to docker binary on server (e.g., `/usr/bin/` or leave empty if docker is in PATH)

## Backend Configuration
- `POSTGRES_USER` - PostgreSQL username (e.g., `pg_user`)
- `POSTGRES_PASSWORD` - PostgreSQL password (e.g., `a-password232A`)
- `POSTGRES_DB` - PostgreSQL database name (e.g., `fitnesslab`)
- `POSTGRES_PORT` - PostgreSQL port (e.g., `5432`)
- `BACKEND_PORT` - Backend application port (e.g., `8080`)
- `MAIL_PASSWORD` - SMTP mail password (if needed)
- `MAILHOG_SMTP_PORT` - MailHog SMTP port (e.g., `1025`)
- `MAILHOG_WEB_PORT` - MailHog web UI port (e.g., `8025`)

## Frontend Configuration
- `FRONTEND_PORT` - Frontend application port (e.g., `8081`)
- `API_URL` - Backend API URL (e.g., `http://your-server.com:8080` or `https://api.yourdomain.com`)
- `AUTH_ISSUER` - Keycloak/OAuth issuer URL (e.g., `https://auth.oliverzihler.ch/realms/fitnesslab`)
- `AUTH_CLIENT_ID` - OAuth client ID (e.g., `fitnesslab-app`)

## How It Works

### Backend
Environment variables from GitHub Secrets are:
1. Passed to `docker-compose.backend.yml` during deployment
2. Injected into the Spring Boot application via `application.yml`
3. Used to configure database, mail, and other services

### Frontend
Environment variables from GitHub Secrets are:
1. Passed to `docker-compose.frontend.yml` during deployment
2. Injected into the container at runtime
3. The `env-config.sh` script replaces placeholders in the built JavaScript files
4. Used to configure API endpoints and authentication

This approach allows you to:
- Deploy the same Docker images to different environments
- Change configuration without rebuilding
- Keep secrets secure in GitHub Secrets
