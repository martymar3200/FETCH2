# develop stage
FROM node:22-alpine AS develop-stage

WORKDIR /app

COPY package*.json ./

RUN npm -g install @quasar/cli

COPY . .

# build stage
FROM develop-stage AS build-stage

RUN npm install

# if you need to change env reference just change the "ENVIRONMENT=STRING"
RUN quasar build -m pwa

# CUSTOM CA CERTIFICATES: If your environment uses internal/private CA certificates,
# add them here. Example:
# ADD certificates/your-ca-bundle.crt /usr/local/share/ca-certificates/your-ca-bundle.crt
# RUN chmod 644 /usr/local/share/ca-certificates/your-ca-bundle.crt

# production stage
FROM nginx:1.27.2-alpine AS production-stage

COPY --from=build-stage /app/dist/pwa /usr/share/nginx/html

# This is for confirmation only, bundler already ran
COPY --from=build-stage /app/env/.env env/.env

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx/production.conf /etc/nginx/conf.d/default.conf

# Install openssl for TLS cert generation
RUN apk --update --no-cache add openssl

# Create an entrypoint script that generates self-signed certs ONLY if
# real certs are not mounted at runtime. This allows operators to mount
# production TLS certificates without rebuilding the image.
RUN printf '#!/bin/sh\n\
if [ ! -f /etc/ssl/certs/server.crt ]; then\n\
  echo "No TLS certificate found at /etc/ssl/certs/server.crt — generating self-signed cert..."\n\
  mkdir -p /etc/ssl/certs /etc/ssl/private\n\
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \\\n\
    -subj "/C=US/ST=DC/L=Washington/O=FETCH2/OU=Web Services/CN=localhost" \\\n\
    -keyout /etc/ssl/private/server.key \\\n\
    -out /etc/ssl/certs/server.crt 2>/dev/null\n\
  openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048 2>/dev/null\n\
  echo "Self-signed certificate generated."\n\
else\n\
  echo "TLS certificate found — using mounted certificate."\n\
fi\n\
exec "$@"\n' > /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
