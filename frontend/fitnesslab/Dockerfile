# Build stage
FROM node:22-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Runtime stage
FROM nginx:alpine

# Copy built application to nginx html directory
COPY --from=build /app/dist/fitnesslab/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy environment configuration script
COPY env-config.sh /docker-entrypoint.d/env-config.sh
RUN chmod +x /docker-entrypoint.d/env-config.sh

# Expose port 80
EXPOSE 80

# Start nginx (env-config.sh will run automatically via docker-entrypoint.d)
CMD ["nginx", "-g", "daemon off;"]
