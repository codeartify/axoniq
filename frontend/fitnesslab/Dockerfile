# Build stage
FROM node:22-alpine AS build

WORKDIR /app

# Copy OpenAPI spec from backend (expecting context at repo root)
COPY backend/src/main/resources/openapi.yaml /tmp/openapi.yaml

# Copy package files
COPY frontend/fitnesslab/package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Copy frontend source code
COPY frontend/fitnesslab/ .

# Generate API client from OpenAPI spec
RUN npm run generate-api:docker

# Build the application
RUN npm run build

# Runtime stage
FROM nginx:alpine

# Copy built application to nginx html directory
COPY --from=build /app/dist/fitnesslab/browser /usr/share/nginx/html

# Copy nginx configuration
COPY frontend/fitnesslab/nginx.conf /etc/nginx/nginx.conf

# Copy environment configuration script
COPY frontend/fitnesslab/env-config.sh /docker-entrypoint.d/env-config.sh
RUN chmod +x /docker-entrypoint.d/env-config.sh

# Expose port 80
EXPOSE 80

# Start nginx (env-config.sh will run automatically via docker-entrypoint.d)
CMD ["nginx", "-g", "daemon off;"]
