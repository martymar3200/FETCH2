FROM postgres:16.8 as postgres_build

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=inventory_service

# Not needed, yet
# COPY init.sql /docker-entrypoint-initdb.d/

EXPOSE 5432

# Named Volume for persistance (check if absolute path needed)
# VOLUME /opt/data/inventory_service_data:/var/lib/postgresql/data
VOLUME inventory_service_data:/var/lib/postgresql/data
