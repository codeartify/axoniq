#!/bin/sh
# Replace environment variables in the built Angular application

# Set defaults if not provided
API_URL=${API_URL:-http://localhost:8080}
AUTH_ISSUER=${AUTH_ISSUER:-https://auth.oliverzihler.ch/realms/fitnesslab}
AUTH_CLIENT_ID=${AUTH_CLIENT_ID:-fitnesslab-app}

# Find all JavaScript files in the build directory
find /usr/share/nginx/html -type f -name '*.js' -exec sed -i \
  -e "s|__API_URL__|$API_URL|g" \
  -e "s|__AUTH_ISSUER__|$AUTH_ISSUER|g" \
  -e "s|__AUTH_CLIENT_ID__|$AUTH_CLIENT_ID|g" \
  {} \;

echo "Environment configuration applied:"
echo "  API_URL=$API_URL"
echo "  AUTH_ISSUER=$AUTH_ISSUER"
echo "  AUTH_CLIENT_ID=$AUTH_CLIENT_ID"
