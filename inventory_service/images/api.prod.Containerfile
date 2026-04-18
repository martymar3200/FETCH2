# Stage 1: Requirements stage
FROM python:3.11.4-slim AS requirements-stage

WORKDIR /tmp

RUN pip install poetry==1.6.1

COPY pyproject.toml ../poetry.lock* /tmp/

# SECURITY: .env is NOT copied into the image. Secrets must be injected at runtime
# via Kubernetes Secrets, docker-compose environment, or a vault provider.

RUN poetry export -f requirements.txt --output requirements.txt --without-hashes

# Stage 2: Build the actual image
FROM python:3.11.4-slim

# Install system dependencies
# NOTE: Java/Graphviz/SchemaSpy removed — SchemaSpy is disabled in main.py and added ~200MB of bloat.
#
# SAML SIGNING: If your Identity Provider requires signed SAML assertions,
# uncomment the xmlsec build dependencies below AND the pip install line further down.
# Most IdPs work without SP-side signing, but enterprise/government IdPs often require it.
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y \
        git \
        postgresql-client && \
        # --- SAML SIGNING DEPENDENCIES (uncomment if your IdP requires signed assertions) ---
        # gcc \
        # g++ \
        # make \
        # libxml2-dev \
        # libxslt-dev \
        # libxmlsec1-dev \
        # libxmlsec1-openssl \
        # pkg-config && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /code

# Copy Python requirements from the first stage
COPY --from=requirements-stage /tmp/requirements.txt /code/requirements.txt

# Install dependencies
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt
    # SAML SIGNING: Uncomment if xmlsec build deps are enabled above
    # pip install --no-cache-dir --force-reinstall lxml xmlsec

# Copy your application code
COPY app /code/app
# SECURITY: .env removed — secrets injected at runtime (see compose.yml or K8s Secrets)
COPY migrations /code/migrations
COPY alembic.ini /code/alembic.ini

# Expose the application port
EXPOSE 8001

# workers = 2 * cpu_cores + 1 (adjust GUNICORN_WORKERS env var for your server)
CMD ["gunicorn", "app.main:app", \
    "-k", "uvicorn.workers.UvicornWorker", \
    "--workers", "5", "--bind", "0.0.0.0:8001", \
    "--max-requests", "2500", \
    "--max-requests-jitter", "1250"]
