import os

# Set the server mode to 'True' for development or 'False' for production
SERVER_MODE = True

# Define the host and port where pgAdmin4 should run
DEFAULT_SERVER = 'localhost'
DEFAULT_SERVER_PORT = os.environ.get('PGADMIN_PORT', 5050)

# Enable or disable debug mode
DEBUG = True

# Set the SECRET_KEY for added security
SECRET_KEY = os.urandom(24)

# Configure the database connection to PostgreSQL
SQLALCHEMY_DATABASE_URI = (
    # 'postgresql://postgres:postgres@fetch-postgres:5432/inventory_service'
    # 'postgresql://postgres:postgres@host.docker.internal:5432/inventory_service'
    # 'postgresql://postgres:postgres@inventory-database:5432/inventory_service'
    'postgresql://postgres:postgres@postgres:5432/inventory_service'
)

# SQLALCHEMY_DATABASE_URI = 'postgresql://your_postgres_user:your_postgres_password@postgres:5432/your_database_name'


# Specify the maximum number of rows to be fetched in a single query
ROWS_PER_PAGE = 50

# Use a Docker network to connect to your PostgreSQL container
# It's really Podman now, no network. Connect to service on port. Can't find if this is used.
POSTGRES_DOCKER_NETWORK = 'fetch'

# Define a volume mount located at ${HOME}/workspace/fetch/data/pgadmin
DATA_DIR = os.path.expanduser('~/workspace/fetch/data/pgadmin')

logging_collector = on
log_directory = 'pg_log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_statement = 'all'
