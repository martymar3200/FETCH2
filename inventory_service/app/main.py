# /code/app/main.py - FINAL, STABLE VERSION

import subprocess
import os, sys

# --- CRITICAL FIX: Ensure package path resolution works for Uvicorn/Alembic ---
sys.path.append(os.path.dirname(os.path.abspath(__file__))) 
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from contextlib import asynccontextmanager
from starlette.exceptions import HTTPException as StarletteHTTPException
from starlette.middleware.base import BaseHTTPMiddleware
import importlib
import pkgutil 

from app.middlware import JWTMiddleware

from fastapi import FastAPI, Depends, Request
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware
from fastapi_pagination import add_pagination
import logging
import time

# Ensure all models are imported/registered
import app.models.all

from alembic.config import Config
from alembic import command

from app.config.config import get_settings
from sqlalchemy.exc import DBAPIError
from app.config.exceptions import (
    BadRequest,
    NotFound,
    ValidationException,
    InternalServerError,
    NotAuthorized,
    Forbidden,
    bad_request_exception_handler,
    not_found_exception_handler,
    validation_exception_handler,
    internal_server_error_exception_handler,
    not_authorized_exception_handler,
    forbidden_exception_handler,
    unhandled_exception_handler,
)
from app.routers import (
    buildings,
    modules,
    aisles,
    sides,
    side_orientations,
    barcode_types,
    barcodes,
    ladders,
    shelves,
    container_types,
    shelf_positions,
    owner_tiers,
    owners,
    accession_jobs,
    verification_jobs,
    trays,
    media_types,
    size_class,
    conveyance_bins,
    items,
    subcollection,
    non_tray_items,
    shelving_jobs,
    users,
    groups,
    permissions,
    request_types,
    priorities,
    delivery_locations,
    requests,
    pick_lists,
    refile_queue,
    refile_jobs,
    withdraw_jobs,
    auth,
    status,
    batch_upload,
    shelf_types,
    reporting,
    audit_trails,
    verification_changes,
    item_retrieval_events,
    non_tray_item_retrieval_events,
    system_settings,
    shipping_jobs,
)


def _force_load_all_models():
    """Forces load of all model modules to ensure Base.metadata is populated."""
    try:
        models_package = importlib.import_module('app.models')
        for module_loader, name, is_pkg in pkgutil.walk_packages(
            models_package.__path__,
            models_package.__name__ + '.'
        ):
            importlib.import_module(name)
    except Exception as e:
        print(f"Warning: Model discovery failure in main.py: {e}")
        pass
    
def alembic_context():
    # CRITICAL: Force-load models before Alembic tries to configure metadata
    _force_load_all_models() 
    
    alembic_cfg = Config("alembic.ini")
    try:
        # Run migrations
        print("Migrating...")
        command.upgrade(alembic_cfg, "head")

        # --- SCHEMASPY LOGIC (Disabled to prevent container crash) ---
        if get_settings().APP_ENVIRONMENT not in ["debug", "production"]:
            print("Updating Schema Docs...")
            try:
                bat_pos = get_settings().DATABASE_URL.find("@")
                at_pos = get_settings().DATABASE_URL.find("@") + 1
                last_colon_pos = get_settings().DATABASE_URL.rfind(":")
                last_slash_pos = get_settings().DATABASE_URL.find("/") + 1
                db_host = get_settings().DATABASE_URL[at_pos:last_colon_pos]
                db_port = get_settings().DATABASE_URL[
                    last_colon_pos + 1: last_colon_pos + 5
                ]
                db_user_password = get_settings().DATABASE_URL[last_slash_pos + 1 : bat_pos]
                db_user, db_password = db_user_password.split(":")
                
                create_schemaspy = [
                    "java",
                    "-jar",
                    "/code/schemaspy.jar",
                    "-t",
                    "pgsql11",
                    "-dp",
                    "/code/postgresql.jar",
                    "-o",
                    "/code/schema-docs",
                    "-u",
                    f"{db_user}",
                    "-p",
                    f"{db_password}",
                    "-db",
                    "inventory_service",
                    "-s",
                    "public",
                    "-host",
                    f"{db_host}",
                    "-port",
                    f"{db_port}",
                ]
                
                # CRITICAL: Commented out to prevent crash if Java is missing
                # subprocess.run(create_schemaspy, check=True)
                print("SchemaSpy generation skipped (Java requirement bypassed).")
                
            except Exception as e:
                print(f"SchemaSpy generation failed (Skipping): {e}")

    except Exception as e:
        print(f"Startup Error: {e}")
        # We re-raise here because if the DB migration fails, the app IS broken.
        # But we caught the SchemaSpy error above, so that won't stop us.
        raise


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Run migrations on startup
    alembic_context()
    
    # Seed permissions if any are missing (idempotent)
    from app.seed.seed_permissions_adhoc import seed_new_permissions
    try:
        print("Checking for new permissions...")
        seed_new_permissions()
        print("Permission check complete.")
    except Exception as e:
        # Non-fatal - log and continue
        print(f"Warning: Permission seeding encountered an error: {e}")
        print("Continuing with application startup...")
    
    # Mount schema docs if they exist
    if get_settings().APP_ENVIRONMENT not in ["debug", "production"]:
        # Only mount if directory exists to avoid errors
        if os.path.isdir("/code/schema-docs"):
            app.mount(
                "/schema",
                StaticFiles(directory="/code/schema-docs", html=True),
                name="schema-docs",
            )
            
    yield
    print("Shutting down...")


app = FastAPI(
    lifespan=lifespan,
    debug=True if get_settings().APP_ENVIRONMENT == "debug" else False
)

# add log and auth check middleware first
app.add_middleware(JWTMiddleware)

# add CORS middleware second
app.add_middleware(
    CORSMiddleware,
    allow_origin_regex=get_settings().ALLOWED_ORIGINS_REGEX,
    allow_origins=get_settings().ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
async def root():
    return {
        "message": f"{get_settings().APP_NAME} Inventory Service {get_settings().APP_ENVIRONMENT} environment api root"
    }


# app fallback exception handling
@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    return JSONResponse({"detail": str(exc.detail)}, status_code=exc.status_code)


# Register custom exception handlers
app.exception_handler(BadRequest)(bad_request_exception_handler)
app.exception_handler(NotFound)(not_found_exception_handler)
app.exception_handler(ValidationException)(validation_exception_handler)
app.exception_handler(InternalServerError)(internal_server_error_exception_handler)
app.exception_handler(NotAuthorized)(not_authorized_exception_handler)
app.exception_handler(Forbidden)(forbidden_exception_handler)
app.exception_handler(DBAPIError)(unhandled_exception_handler)
app.exception_handler(Exception)(unhandled_exception_handler)

# order matters for route matching [nested before base]
app.include_router(buildings.router)
app.include_router(modules.router)
app.include_router(aisles.router)
app.include_router(side_orientations.router)
app.include_router(sides.router)
app.include_router(barcode_types.router)
app.include_router(barcodes.router)
app.include_router(ladders.router)
app.include_router(container_types.router)
app.include_router(shelf_positions.router)
app.include_router(shelf_types.router)
app.include_router(shelves.router)
app.include_router(owner_tiers.router)
app.include_router(owners.router)
app.include_router(accession_jobs.router)
app.include_router(verification_jobs.router)
app.include_router(trays.router)
app.include_router(media_types.router)
app.include_router(size_class.router)
app.include_router(conveyance_bins.router)
app.include_router(items.router)
app.include_router(subcollection.router)
app.include_router(non_tray_items.router)
app.include_router(shelving_jobs.router)
app.include_router(users.router)
app.include_router(groups.router)
app.include_router(permissions.router)
app.include_router(request_types.router)
app.include_router(priorities.router)
app.include_router(delivery_locations.router)
app.include_router(requests.router)
app.include_router(pick_lists.router)
app.include_router(refile_queue.router)
app.include_router(refile_jobs.router)
app.include_router(withdraw_jobs.router)
app.include_router(auth.router)
app.include_router(status.router)
app.include_router(batch_upload.router)
app.include_router(reporting.router)
app.include_router(audit_trails.router)
app.include_router(verification_changes.router)
app.include_router(item_retrieval_events.router)
app.include_router(non_tray_item_retrieval_events.router)
app.include_router(system_settings.router)
app.include_router(shipping_jobs.router)

add_pagination(app)