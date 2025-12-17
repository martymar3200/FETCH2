# Use an official Python image as the base
# FROM python:3.8-slim
FROM dpage/pgadmin4

# # Set environment variables
# ENV PGADMIN_VERSION=7.1
# ENV PGADMIN_DEFAULT_EMAIL=admin@fetch.example.com
# ENV PGADMIN_DEFAULT_PASSWORD=admin

# # Install dependencies
# RUN apt-get update && apt-get install -y libgmp-dev libpq-dev
# RUN pip install https://ftp.postgresql.org/pub/pgadmin/pgadmin4/v$PGADMIN_VERSION/pip/pgadmin4-$PGADMIN_VERSION-py3-none-any.whl



# # Create a configuration file for pgAdmin4
# COPY ../config_local.py /pgadmin4/config_local.py

# Expose the web interface port
EXPOSE 5050

# Run pgAdmin4
# CMD ["python", "/usr/local/lib/python3.8/site-packages/pgadmin4/pgAdmin4.py"]
