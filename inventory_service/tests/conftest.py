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
from app.database.base import Base # Imported Base
from app.main import app

# ... (omitted lines)

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
