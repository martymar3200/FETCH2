# develop stage
FROM node:22-alpine AS develop-stage

WORKDIR /app

COPY package*.json ./

# Tools for localhost cert gen
RUN apk add --no-cache curl nss
RUN curl -JLO https://github.com/FiloSottile/mkcert/releases/latest/download/mkcert-v1.4.4-linux-amd64 \
    && chmod +x mkcert-v1.4.4-linux-amd64 \
    && mv mkcert-v1.4.4-linux-amd64 /usr/local/bin/mkcert \
    && mkcert -install
# RUN apk add --no-cache mkcert nss-tools

RUN npm -g install @quasar/cli

COPY . .

# Gen localhost certs
RUN npm run generate-cert

# build stage
FROM develop-stage AS build-stage

RUN npm install

# if you need to change env reference just change the "ENVIRONMENT=STRING"
RUN ENVIRONMENT=local quasar build -m pwa --debug

# production stage
FROM nginx:1.27.2-alpine AS production-stage

COPY --from=build-stage /app/dist/pwa /usr/share/nginx/html

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx/local.conf /etc/nginx/conf.d/default.conf

# Add localhost certs
COPY --from=develop-stage /app/.cert/cert.pem /etc/nginx/cert.pem
COPY --from=develop-stage /app/.cert/key.pem /etc/nginx/key.pem

EXPOSE 443

CMD ["nginx", "-g", "daemon off;"]
