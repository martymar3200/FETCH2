FROM postgres:16.2 as postgres_build

ENV POSTGRES_USER=postgres
ARG POSTGRES_PASSWORD=postgres
ENV POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
ENV POSTGRES_DB=keycloak

# Not needed, yet
# COPY init.sql /docker-entrypoint-initdb.d/

EXPOSE 5432

# Named Volume for persistance (check if absolute path needed)
# VOLUME /opt/data/keycloak_data:/var/lib/postgresql/data
VOLUME keycloak_data:/var/lib/postgresql/data
