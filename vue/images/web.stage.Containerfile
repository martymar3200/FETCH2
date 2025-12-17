# develop stage
FROM node:22-alpine AS develop-stage

WORKDIR /app

COPY package*.json ./

RUN npm -g install @quasar/cli

COPY . .

RUN cat env/.env

# build stage
FROM develop-stage AS build-stage

RUN npm install
RUN npm config set strict-ssl false

# if you need to change env reference just change the "ENVIRONMENT=STRING"
RUN quasar build -m pwa

ADD certificates/ca-bundle.crt /usr/local/share/ca-certificates/ca-bundle.crt
ADD certificates/ca-bundle.trust.crt /usr/local/share/ca-certificates/ca-bundle.trust.crt
ADD certificates/LOC-INTERMEDIATE-CA-2.crt /usr/share/ca-certificates/LOC-INTERMEDIATE-CA-2.crt
RUN chmod 644 /usr/local/share/ca-certificates/ca-bundle.crt
RUN chmod 644 /usr/local/share/ca-certificates/ca-bundle.trust.crt
ENV REQUESTS_CA_BUNDLE=/usr/local/share/ca-certificates/ca-bundle.crt

# production stage
FROM nginx:1.27.2-alpine AS production-stage

COPY --from=build-stage /app/dist/pwa /usr/share/nginx/html

# This is for confirmation only, bundler already ran
COPY --from=build-stage /app/env/.env env/.env

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx/staging.conf /etc/nginx/conf.d/default.conf

RUN apk --update --no-cache add openssl
RUN rm -rf /etc/ssl/certs
RUN rm -rf /etc/ssl/private
RUN mkdir /etc/ssl/certs
RUN mkdir /etc/ssl/private
RUN chmod 600 /etc/ssl/certs
RUN chmod 600 /etc/ssl/private
RUN openssl req -x509 -nodes -days 365 -newkey rsa:2048 -subj "/C=US/ST=DC/L=Washington/O=LOC/OU=Web Services/CN=localhost" -keyout /etc/ssl/private/develop.key -out /etc/ssl/certs/develop.crt
RUN openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
