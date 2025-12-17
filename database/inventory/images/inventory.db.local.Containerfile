FROM postgres:16.8 as postgres_build

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=inventory_service

# install xz for dump compression
RUN apt update && apt install -y xz-utils

# Not needed, yet
# COPY init.sql /docker-entrypoint-initdb.d/

EXPOSE 5432

# Named Volume is managed in compose (fetch-local)
