import os
import time
import logging
import pytest
import subprocess
import json

from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from sqlalchemy_utils import database_exists, create_database, drop_database

from app.database.session import get_session
from app.database.base import Base
from app.main import app

# ... imports ...
from tests.fixtures.configtest import (
    ROOT_FILE_PATH,
    CREATE_DATA_SAMPLER_FIXTURE,
    DATA_RESPONSE,
    UPDATE_DATA_SAMPLER_FIXTURE,
    EMPTY_RESPONSE,
    PAGE_EMPTY_RESPONSE,
    SIZE_EMPTY_RESPONSE,
    DATA_PAGE_RESPONSE,
    DATA_SIZE_RESPONSE,
    get_data_from_file,
    generate_barcode_id,
    populate_record,
)

# Define the Docker command to run the Postgres container
DOCKER_RUN_COMMAND = (
    f"docker compose -f {ROOT_FILE_PATH}/tests/test-docker-compose.yml up -d"
)
DOCKER_DOWN_COMMAND = (
    f"docker compose -f {ROOT_FILE_PATH}/tests/test-docker-compose.yml down test_db"
)
DOCKER_CLEANUP_COMMAND = "docker system prune -fa"
DOCKER_CLEANUP_VOLUME_COMMAND = "docker volume prune -fa"

ALEMBIC_UPGRADE_COMMAND = "alembic upgrade head"
TEST_DATABASE_URL = "postgresql://postgres:postgres@localhost:5433/test_database"

# Create a new database for testing
engine = create_engine(TEST_DATABASE_URL)

logger = logging.getLogger("tests.configtest")


@pytest.fixture(scope="session")
def init_db():
    # ... (same content) ...
    result = subprocess.run(
        DOCKER_RUN_COMMAND.split(), check=True, capture_output=True, text=True
    )

    if result.returncode != 0:
        logging.error("Failed to start Docker container: %s", result.stderr)
    else:
        logging.info("Docker container started successfully.")

    time.sleep(10)  # Wait for the database to be ready

    yield

    drop_database(engine.url)
    subprocess.run(DOCKER_DOWN_COMMAND.split())
    subprocess.run(DOCKER_CLEANUP_COMMAND.split())
    subprocess.run(DOCKER_CLEANUP_VOLUME_COMMAND.split())


@pytest.fixture(scope="module")
def session():
    """
    Fixture that provides a session object for testing.
    Yields:
    - Session: The session object.
    """
    session = Session(engine)
    yield session

    # Close the session after the test is done
    session.close()


@pytest.fixture(name="client", scope="module")
def client(session):
    """
    Fixture that returns a TestClient instance for testing FastAPI application.
    Parameters:
    - session: Session object for dependency injection.
    Returns:
    - TestClient: TestClient instance for testing.
    """

    # Dependency override for the session
    def get_session_override():
        return session

    # Override the dependency with the test session
    app.dependency_overrides[get_session] = get_session_override
    client = TestClient(app)
    yield client

    # Clear overrides after the test is done
    app.dependency_overrides.clear()



@pytest.fixture(scope="module")
def test_database(client, init_db):
    """
    Initialize and test the database.

    Args :
    - init_db: A boolean indicating whether to initialize the database.

    Return:
    - None
    """
    if not database_exists(engine.url):
        create_database(engine.url)

    Base.metadata.create_all(engine)

    subprocess.run(ALEMBIC_UPGRADE_COMMAND.split())

    # Populate the database with sample data
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "buildings")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "modules")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "aisle_numbers")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "aisles")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "side_orientations")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "sides")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "barcode_types")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "barcodes")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "shelf_numbers")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "shelf_position_numbers")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "ladder_numbers")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "ladders")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "owner_tiers")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "owners")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "container_types")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "size_class")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "shelf_types")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "shelves")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "shelf_positions")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "subcollections")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "media_types")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "users")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "accession_jobs")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "verification_jobs")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "shelving_jobs")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "trays")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "items")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "shelving_jobs")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "permissions")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "groups")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "pick_lists")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "request_types")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "requests")
    populate_record(client, CREATE_DATA_SAMPLER_FIXTURE, "refile_jobs")
