# /code/app/routers/shelf_position_numbers.py - REFACRORED TO SQLALCHEMY V2

from fastapi import APIRouter, HTTPException, Depends
from fastapi_pagination import Page
# CRITICAL FIX: Changed from .ext.sqlmodel to .ext.sqlalchemy
from fastapi_pagination.ext.sqlalchemy import paginate
# CRITICAL FIX: Replaced from sqlmodel import Session, select
from sqlalchemy.orm import Session # Session is imported from sqlalchemy.orm now
from sqlalchemy import select     # select is imported from sqlalchemy now
from datetime import datetime, timezone
from sqlalchemy.exc import IntegrityError

from app.database.session import get_session
from app.filter_params import SortParams
from app.models.shelf_position_numbers import ShelfPositionNumber
from app.schemas.shelf_position_numbers import (
    ShelfPositionNumberInput,
    ShelfPositionNumberListOutput,
    ShelfPositionNumberDetailOutput,
)
from app.config.exceptions import (
    NotFound,
    ValidationException,
    InternalServerError,
)
from app.sorting import BaseSorter
from typing import Optional

from app.auth.dependencies import RequiresPermission

router = APIRouter(
    prefix="/shelves/positions",
    tags=["shelves"],
    dependencies=[Depends(RequiresPermission("can_manage_locations"))],
)


@router.get("/numbers", response_model=Page[ShelfPositionNumberListOutput])
def get_shelf_position_number_list(
    session: Session = Depends(get_session),
    sort_params: SortParams = Depends()
) -> list:
    """
    Retrieve a paginated list of shelf position numbers.
    """
    # Create a query to select all SShelf Position Number
    query = select(ShelfPositionNumber)

    # Validate and Apply sorting based on sort_params
    if sort_params.sort_by:
        sorter = BaseSorter(ShelfPositionNumber)
        query = sorter.apply_sorting(query, sort_params)

    return paginate(session, query)


@router.get("/numbers/{id}", response_model=ShelfPositionNumberDetailOutput)
def get_shelf_position_number_detail(id: int, session: Session = Depends(get_session)):
    """
    Retrieve the details of a shelf position number.
    """
    shelf_position_number = session.get(ShelfPositionNumber, id)

    if shelf_position_number:
        return shelf_position_number

    raise NotFound(detail=f"Shelf Position Number ID {id} Not Found")


@router.post(
    "/numbers", response_model=ShelfPositionNumberDetailOutput, status_code=201
)
def create_shelf_position_number(
    shelf_position_number_input: ShelfPositionNumberInput,
    session: Session = Depends(get_session),
) -> ShelfPositionNumber:
    """
    Create a shelf position number:
    """
    try:
        new_shelf_position_number = ShelfPositionNumber(
            **shelf_position_number_input.model_dump()
        )
        session.add(new_shelf_position_number)
        session.commit()
        session.refresh(new_shelf_position_number)

        return new_shelf_position_number

    except IntegrityError as e:
        raise ValidationException(detail=f"{e}")


@router.patch("/numbers/{id}", response_model=ShelfPositionNumberDetailOutput)
def update_shelf_position_number(
    id: int,
    shelf_position_number: ShelfPositionNumberInput,
    session: Session = Depends(get_session),
):
    """
    Update a shelf position number in the database.
    """
    try:
        existing_shelf_position_number = session.get(ShelfPositionNumber, id)

        if existing_shelf_position_number is None:
            raise NotFound(detail=f"Shelf Position Number ID {id} Not Found")

        mutated_data = shelf_position_number.model_dump(exclude_unset=True)

        for key, value in mutated_data.items():
            setattr(existing_shelf_position_number, key, value)

        setattr(existing_shelf_position_number, "update_dt", datetime.now(timezone.utc))
        session.add(existing_shelf_position_number)
        session.commit()
        session.refresh(existing_shelf_position_number)

        return existing_shelf_position_number

    except Exception as e:
        raise InternalServerError(detail=f"{e}")


@router.delete("/numbers/{id}")
def delete_shelf_position_number(id: int, session: Session = Depends(get_session)):
    """
    Delete a shelf position number by its ID.
    """
    shelf_position_number = session.get(ShelfPositionNumber, id)

    if shelf_position_number:
        session.delete(shelf_position_number)
        session.commit()

        return HTTPException(
            status_code=204, detail=f"Shelf Position Number ID {id} Deleted "
                                    f"Successfully"
        )

    raise NotFound(detail=f"Shelf Position Number ID {id} Not Found")
