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
RUN ENVIRONMENT=local quasar build -m pwa --debug

# production stage
FROM nginx:1.27.2-alpine AS production-stage

COPY --from=build-stage /app/dist/pwa /usr/share/nginx/html

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx/local.conf /etc/nginx/conf.d/default.conf

# Certificates are mounted at runtime via docker-compose volumes

EXPOSE 443

CMD ["nginx", "-g", "daemon off;"]
