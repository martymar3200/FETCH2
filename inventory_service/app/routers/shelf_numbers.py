# /code/app/routers/shelf_numbers.py - REFACRORED TO SQLALCHEMY V2

from fastapi import APIRouter, HTTPException, Depends
from fastapi_pagination import Page
# CRITICAL FIX: Changed from .ext.sqlmodel to .ext.sqlalchemy
from fastapi_pagination.ext.sqlalchemy import paginate
# CRITICAL FIX: Replaced from sqlmodel import Session, select
from sqlalchemy.orm import Session # Session is imported from sqlalchemy.orm now
from sqlalchemy import select     # select is imported from sqlalchemy now
from datetime import datetime, timezone
from sqlalchemy.exc import IntegrityError
from typing import Optional

from app.database.session import get_session
from app.filter_params import SortParams
from app.models.shelf_numbers import ShelfNumber
from app.schemas.shelf_numbers import (
    ShelfNumberInput,
    ShelfNumberListOutput,
    ShelfNumberDetailOutput,
)
from app.config.exceptions import (
    NotFound,
    ValidationException,
    InternalServerError,
)
from app.sorting import BaseSorter

from app.auth.dependencies import RequiresPermission

router = APIRouter(
    prefix="/shelves",
    tags=["shelves"],
    dependencies=[Depends(RequiresPermission("can_manage_locations"))],
)


@router.get("/numbers", response_model=Page[ShelfNumberListOutput])
def get_shelf_number_list(
    session: Session = Depends(get_session),
    sort_params: SortParams = Depends()
) -> list:
    """
    Get a paginated list of shelf numbers.
    """

    # Create a query to select all Shelf Number
    query = select(ShelfNumber)

    # Validate and Apply sorting based on sort_params
    if sort_params.sort_by:
        sorter = BaseSorter(ShelfNumber)
        query = sorter.apply_sorting(query, sort_params)

    # CRITICAL FIX: Paginate now takes only the query object
    return paginate(session, query)


@router.get("/numbers/{id}", response_model=ShelfNumberDetailOutput)
def get_shelf_number_detail(id: int, session: Session = Depends(get_session)):
    """
    Retrieve details of a shelf number by ID.
    """
    shelf_number = session.get(ShelfNumber, id)

    if shelf_number:
        return shelf_number

    raise NotFound(detail=f"Shelf Number ID {id} Not Found")


@router.post("/numbers", response_model=ShelfNumberDetailOutput, status_code=201)
def create_shelf_number(
    shelf_number_input: ShelfNumberInput, session: Session = Depends(get_session)
) -> ShelfNumber:
    """
    Create a shelf number:
    """
    try:
        new_shelf_number = ShelfNumber(**shelf_number_input.model_dump())
        session.add(new_shelf_number)
        session.commit()
        session.refresh(new_shelf_number)

        return new_shelf_number

    except IntegrityError as e:
        raise ValidationException(detail=f"{e}")


@router.patch("/numbers/{id}", response_model=ShelfNumberDetailOutput)
def update_shelf_number(
    id: int, shelf_number: ShelfNumberInput, session: Session = Depends(get_session)
):
    """
    Update a shelf number in the database.
    """
    try:
        existing_shelf_number = session.get(ShelfNumber, id)

        if not existing_shelf_number:
            raise NotFound(detail=f"Shelf Number ID {id} Not Found")

        mutated_data = shelf_number.model_dump(exclude_unset=True)

        for key, value in mutated_data.items():
            setattr(existing_shelf_number, key, value)

        setattr(existing_shelf_number, "update_dt", datetime.now(timezone.utc))
        session.add(existing_shelf_number)
        session.commit()
        session.refresh(existing_shelf_number)

        return existing_shelf_number

    except Exception as e:
        raise InternalServerError(detail=f"{e}")


@router.delete("/numbers/{id}")
def delete_shelf_number(id: int, session: Session = Depends(get_session)):
    """
    Delete a shelf number by ID.
    """
    shelf_number = session.get(ShelfNumber, id)

    if shelf_number:
        session.delete(shelf_number)
        session.commit()

        return HTTPException(
            status_code=204, detail=f"Shelf Number ID {id} Deleted "
                                    f"Successfully"
        )

    raise NotFound(detail=f"Shelf Number ID {id} Not Found")