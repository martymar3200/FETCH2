# /code/app/routers/shelves.py - FINAL REFACRORED TO SQLALCHEMY V2

import logging
from typing import List, Optional

from fastapi import APIRouter, HTTPException, Depends, Query
from fastapi_pagination import Page
from fastapi_pagination import paginate as paginate_list
# CRITICAL FIX: Changed from .ext.sqlmodel to .ext.sqlalchemy
from fastapi_pagination.ext.sqlalchemy import paginate
# CRITICAL FIX: Replaced from sqlmodel import Session, select
from sqlalchemy.orm import Session
from sqlalchemy import select, func
from datetime import datetime, timezone
from sqlalchemy.exc import IntegrityError

from app.database.session import get_session
from app.filter_params import SortParams, ShelfFilterParams
from app.models.owners import Owner
from app.models.shelf_types import ShelfType
from app.models.shelves import Shelf
from app.models.barcodes import Barcode
from app.models.shelf_numbers import ShelfNumber
from app.models.buildings import Building
from app.models.modules import Module
from app.models.aisles import Aisle
from app.models.sides import Side
from app.models.ladders import Ladder
from app.models.size_class import SizeClass
from app.models.trays import Tray
from app.models.non_tray_items import NonTrayItem
from app.models.shelf_positions import ShelfPosition
from app.models.shelf_position_numbers import ShelfPositionNumber
from app.schemas.shelves import (
    ShelfInput,
    ShelfUpdateInput,
    ShelfListOutput,
    ShelfDetailWriteOutput,
    ShelfDetailReadOutput,
)
from app.config.exceptions import NotFound, ValidationException, InternalServerError
from app.sorting import ShelvingSorter
from app.utilities import start_session_with_audit_info

router = APIRouter(
    prefix="/shelves",
    tags=["shelves"],
)

LOGGER = logging.getLogger("app.routers.shelves")


@router.get("/", response_model=Page[ShelfListOutput])
def get_shelf_list(
    session: Session = Depends(get_session),
    params: ShelfFilterParams = Depends(),
    sort_params: SortParams = Depends(),
    search: Optional[str] = Query(None, description="Search by Shelf location"),
) -> list:
    """
    Get a list of shelves.
    """
    shelf_queryset = select(Shelf)

    if search:
        shelf_queryset = shelf_queryset.where(Shelf.location.icontains(search))

    if params.owner_id:
        shelf_queryset = shelf_queryset.where(Shelf.owner_id == params.owner_id)

    if params.size_class_id:
        shelf_queryset = shelf_queryset.join(
            ShelfType, Shelf.shelf_type_id == ShelfType.id
        ).where(ShelfType.size_class_id == params.size_class_id)

    # location from most to least constrained
    if params.shelf_id:
        shelf_queryset = shelf_queryset.where(Shelf.id == params.shelf_id)
    elif params.ladder_id:
        shelf_queryset = shelf_queryset.join(
            Ladder, Shelf.ladder_id == Ladder.id
        ).where(Ladder.id == params.ladder_id)
    elif params.side_id:
        shelf_queryset = (
            shelf_queryset.join(Ladder, Shelf.ladder_id == Ladder.id)
            .join(Side, Ladder.side_id == Side.id)
            .where(Side.id == params.side_id)
        )
    elif params.aisle_id:
        shelf_queryset = (
            shelf_queryset.join(Ladder, Shelf.ladder_id == Ladder.id)
            .join(Side, Ladder.side_id == Side.id)
            .join(Aisle, Side.aisle_id == Aisle.id)
            .where(Aisle.id == params.aisle_id)
        )
    elif params.module_id:
        shelf_queryset = (
            shelf_queryset.join(Ladder, Shelf.ladder_id == Ladder.id)
            .join(Side, Ladder.side_id == Side.id)
            .join(Aisle, Side.aisle_id == Aisle.id)
            .join(Module, Aisle.module_id == Module.id)
            .where(Module.id == params.module_id)
        )
    elif params.building_id:
        shelf_queryset = (
            shelf_queryset.join(Ladder, Shelf.ladder_id == Ladder.id)
            .join(Side, Ladder.side_id == Side.id)
            .join(Aisle, Side.aisle_id == Aisle.id)
            .join(Module, Aisle.module_id == Module.id)
            .join(Building, Module.building_id == Building.id)
            .where(Building.id == params.building_id)
        )

    if params.unassigned:
        shelf_queryset = shelf_queryset.where(Shelf.barcode_id == None)
    if params.barcode_value:
        # V2 FIX: Use scalar_subquery
        barcode_value_subquery = select(Barcode.id).where(
            Barcode.value == params.barcode_value
        ).scalar_subquery()
        shelf_queryset = shelf_queryset.where(
            Shelf.barcode_id.in_(select(Barcode.id).where(Barcode.value == params.barcode_value))
        )
    if params.owner:
        # V2 FIX
        owner_subquery = select(Owner.id).where(Owner.name == params.owner).scalar_subquery()
        shelf_queryset = shelf_queryset.where(Shelf.owner_id == owner_subquery)
    if params.size_class:
        # V2 FIX
        size_class_subquery = select(SizeClass.id).where(
            SizeClass.name == params.size_class
        ).scalar_subquery()
        shelf_queryset = shelf_queryset.join(
            ShelfType, Shelf.shelf_type_id == ShelfType.id
        ).where(ShelfType.size_class_id == size_class_subquery)
    if params.location:
        shelf_queryset = shelf_queryset.where(Shelf.location == params.location)

    # Validate and Apply sorting based on sort_params
    if sort_params.sort_by:
        sorter = ShelvingSorter(Shelf)
        shelf_queryset = sorter.apply_sorting(shelf_queryset, sort_params)

    # CRITICAL FIX: Paginate now takes only the query object
    return paginate(session, shelf_queryset)


@router.get("/{id}", response_model=ShelfDetailReadOutput)
def get_shelf_detail(id: int, session: Session = Depends(get_session)):
    """
    Retrieves the details of a shelf with the given ID.
    """
    shelf = session.get(Shelf, id)

    if shelf:
        return shelf

    raise NotFound(detail=f"Shelf ID {id} Not Found")


@router.get("/barcode/{value}", response_model=ShelfDetailReadOutput)
def get_shelf_by_barcode_value(value: str, session: Session = Depends(get_session)):
    """
    Retrieve a shelf using a barcode value
    """
    statement = select(Shelf).join(Barcode).where(Barcode.value == value)
    # V2 FIX: session.exec() -> session.execute().scalars().first()
    shelf = session.execute(statement).scalars().first()
    if not shelf:
        raise NotFound(detail=f"Shelf with barcode value {value} not found")
    return shelf


def get_next_available_position(session: Session, shelf_id: int, direction: str = "low_to_high") -> Optional[int]:
    """
    Calculate the next available shelf position number.
    
    Args:
        session: Database session
        shelf_id: The shelf ID
        direction: 'low_to_high' or 'high_to_low'
    
    Returns:
        The next available position number, or None if shelf is full
    """
    # Get all positions for this shelf
    positions_query = (
        select(ShelfPosition, ShelfPositionNumber.number)
        .join(ShelfPositionNumber, ShelfPosition.shelf_position_number_id == ShelfPositionNumber.id)
        .where(ShelfPosition.shelf_id == shelf_id)
    )
    
    if direction == "high_to_low":
        positions_query = positions_query.order_by(ShelfPositionNumber.number.desc())
    else:
        positions_query = positions_query.order_by(ShelfPositionNumber.number.asc())
    
    positions = session.execute(positions_query).all()
    
    for position, position_number in positions:
        # Check if position is occupied by a tray
        tray = session.execute(
            select(Tray.id).where(Tray.shelf_position_id == position.id).limit(1)
        ).first()
        
        if tray:
            continue
        
        # Check if position is occupied by a non-tray item
        non_tray = session.execute(
            select(NonTrayItem.id).where(NonTrayItem.shelf_position_id == position.id).limit(1)
        ).first()
        
        if non_tray:
            continue
        
        # Position is available
        return position_number
    
    # No available positions
    return None


@router.get("/barcode/{value}/next-position")
def get_shelf_next_available_position(value: str, session: Session = Depends(get_session)):
    """
    Retrieve a shelf's next available position using a barcode value.
    Returns the shelf info along with next_available_position.
    """
    from app.routers.system_settings import get_setting_value
    
    statement = select(Shelf).join(Barcode).where(Barcode.value == value)
    shelf = session.execute(statement).scalars().first()
    if not shelf:
        raise NotFound(detail=f"Shelf with barcode value {value} not found")
    
    # Get the direction setting
    direction = get_setting_value(session, "shelf_position_auto_assign_direction", "low_to_high")
    
    # Get next available position
    next_position = get_next_available_position(session, shelf.id, direction)
    
    return {
        "shelf_id": shelf.id,
        "shelf_barcode_value": shelf.barcode.value if shelf.barcode else None,
        "owner_id": shelf.owner_id,
        "owner_name": shelf.owner.name if shelf.owner else None,
        "size_class_id": shelf.shelf_type.size_class_id if shelf.shelf_type else None,
        "size_class_name": shelf.shelf_type.size_class.name if shelf.shelf_type and shelf.shelf_type.size_class else None,
        "max_capacity": shelf.shelf_type.max_capacity if shelf.shelf_type else None,
        "available_space": shelf.available_space,
        "next_available_position": next_position
    }


@router.get("/barcode/{value}/shelved", response_model=Page[dict])
def get_shelved_entities_by_shelf_barcode_value(
    value: str, session: Session = Depends(get_session)
):
    """
    Retrieve tray and non_tray barcode list from things on a shelf
    using a shelf barcode value
    """
    shelf_statement = select(Shelf).join(Barcode).where(Barcode.value == value)
    # V2 FIX
    shelf = session.execute(shelf_statement).scalars().first()
    if not shelf:
        raise NotFound(detail=f"Shelf with barcode value {value} not found")

    # V2 FIX
    shelf_positions = list(
        session.execute(
            select(ShelfPosition).where(ShelfPosition.shelf_id == shelf.id)
        ).scalars().all()
    )

    position_ids = [p.id for p in shelf_positions]

    # V2 FIX
    trays = {
        t.shelf_position_id: t
        for t in session.execute(
            select(Tray)
            .join(Barcode, Tray.barcode_id == Barcode.id)
            .where(Tray.shelf_position_id.in_(position_ids))
        ).scalars().all()
    }

    # V2 FIX
    non_trays = {
        nt.shelf_position_id: nt
        for nt in session.execute(
            select(NonTrayItem)
            .join(Barcode, NonTrayItem.barcode_id == Barcode.id)
            .where(NonTrayItem.shelf_position_id.in_(position_ids))
        ).scalars().all()
    }

    results = []
    for shelf_position in shelf_positions:
        # Get the position number from the related shelf_position_number object.
        position_number = shelf_position.shelf_position_number.number

        if shelf_position.id in trays:
            results.append(
                {
                    "type": "tray",
                    "barcode_value": trays[shelf_position.id].barcode.value,
                    "shelf_position_number": position_number,
                }
            )
        elif shelf_position.id in non_trays:
            results.append(
                {
                    "type": "non_tray",
                    "barcode_value": non_trays[shelf_position.id].barcode.value,
                    "shelf_position_number": position_number,
                }
            )

    return paginate_list(results)


@router.post("/", response_model=ShelfDetailWriteOutput, status_code=201)
def create_shelf(
    shelf_input: ShelfInput, session: Session = Depends(get_session)
) -> Shelf:
    """
    Create a shelf and efficiently bulk-creates its associated shelf positions.
    """
    try:
        shelf_number = shelf_input.shelf_number
        shelf_number_id = shelf_input.shelf_number_id
        mutated_data = shelf_input.model_dump(exclude="shelf_number")
        audit_info = getattr(session, "audit_info", {"name": "System", "id": "0"})

        if not shelf_number_id and not shelf_number:
            raise ValidationException(detail="shelf_number_id OR shelf_number required")
        elif shelf_number and not shelf_number_id:
            # V2 FIX
            shelf_num_object = session.execute(select(ShelfNumber).where(ShelfNumber.number == shelf_number)).scalars().first()
            if not shelf_num_object:
                raise ValidationException(f"No shelf_number entity exists for shelf number {shelf_number}")
            mutated_data["shelf_number_id"] = shelf_num_object.id

        new_shelf = Shelf(**mutated_data)
        session.add(new_shelf)
        session.commit()
        session.refresh(new_shelf)

        shelf_type = session.get(ShelfType, new_shelf.shelf_type_id)
        if not shelf_type:
             raise InternalServerError(detail=f"ShelfType ID {new_shelf.shelf_type_id} not found.")

        # --- OPTIMIZED SHELF POSITION CREATION ---

        # 1. Fetch all required ShelfPositionNumber objects in a single query.
        required_numbers = list(range(1, shelf_type.max_capacity + 1))
        # V2 FIX
        position_numbers_query = select(ShelfPositionNumber).where(ShelfPositionNumber.number.in_(required_numbers))
        
        # 2. Create a dictionary map for instant O(1) lookups.
        position_numbers_map = {p.number: p for p in session.execute(position_numbers_query).scalars().all()}

        shelf_position_list = []
        for position_num in required_numbers:
            shelf_pos_num_obj = position_numbers_map.get(position_num)
            
            if not shelf_pos_num_obj:
                raise InternalServerError(f"ShelfPositionNumber for position {position_num} not found in database.")

            shelf_position_list.append({
                "shelf_id": new_shelf.id,
                "shelf_position_number_id": shelf_pos_num_obj.id,
            })

        if shelf_position_list:
            shelf_positions_to_create: List[ShelfPosition] = [
                ShelfPosition(**data) for data in shelf_position_list
            ]
            session.add_all(shelf_positions_to_create)
            start_session_with_audit_info(audit_info, session)
            session.commit()
        
        # Re-calculate available space.
        if hasattr(new_shelf, 'calc_available_space'):
            new_shelf.calc_available_space(session=session)
            session.add(new_shelf)
            session.commit()
            session.refresh(new_shelf)

        return new_shelf
    except Exception as e:
        raise InternalServerError(detail=f"{e}")


@router.patch("/{id}", response_model=ShelfDetailWriteOutput)
def update_shelf(
    id: int, shelf_input: ShelfUpdateInput, session: Session = Depends(get_session)
):
    """
    Update a shelf with the given ID.
    If the shelf_type is changed, this will adjust the number of shelf positions
    to match the new max_capacity, but only if the positions being removed are empty.
    """
    existing_shelf = session.get(Shelf, id)

    if existing_shelf is None:
        raise NotFound(detail=f"Shelf ID {id} Not Found")

    mutated_data = shelf_input.model_dump(exclude_unset=True)

    # --- START: LOGIC TO HANDLE SHELF CAPACITY CHANGES ---
    
    # Check if the shelf_type_id is being changed and is different from the current one.
    if "shelf_type_id" in mutated_data and mutated_data["shelf_type_id"] != existing_shelf.shelf_type_id:
        
        # Get the old and new shelf type objects to compare their capacities.
        old_shelf_type = session.get(ShelfType, existing_shelf.shelf_type_id)
        new_shelf_type = session.get(ShelfType, mutated_data["shelf_type_id"])

        if not new_shelf_type:
            raise ValidationException(detail=f"New Shelf Type ID {mutated_data['shelf_type_id']} not found.")

        old_capacity = old_shelf_type.max_capacity if old_shelf_type else 0
        new_capacity = new_shelf_type.max_capacity

        # --- PATH 1: DECREASING CAPACITY ---
        if new_capacity < old_capacity:
            # We need to remove positions, but first, we must verify they are empty.
            num_to_remove = old_capacity - new_capacity

            # Find the positions with the highest numbers to remove them.
            positions_to_check_query = (
                select(ShelfPosition)
                .join(ShelfPositionNumber, ShelfPosition.shelf_position_number_id == ShelfPositionNumber.id)
                .where(ShelfPosition.shelf_id == id)
                .order_by(ShelfPositionNumber.number.desc())
                .limit(num_to_remove)
            )
            # V2 FIX
            positions_to_delete = session.execute(positions_to_check_query).scalars().all()
            position_ids_to_delete = [p.id for p in positions_to_delete]

            if position_ids_to_delete:
                # Check for ANY trays or non-tray items in the positions slated for deletion.
                # V2 FIX
                occupied_tray = session.execute(select(Tray.id).where(Tray.shelf_position_id.in_(position_ids_to_delete)).limit(1)).first()
                # V2 FIX
                occupied_non_tray = session.execute(select(NonTrayItem.id).where(NonTrayItem.shelf_position_id.in_(position_ids_to_delete)).limit(1)).first()

                if occupied_tray or occupied_non_tray:
                    # An item exists! Block the update and throw a clear error.
                    raise ValidationException(detail="Shelf is not Empty")

                # If we reach here, the positions are empty and safe to delete.
                for pos in positions_to_delete:
                    session.delete(pos)
        
        # --- PATH 2: INCREASING CAPACITY ---
        elif new_capacity > old_capacity:
            # We need to add new empty positions.
            new_position_numbers_range = list(range(old_capacity + 1, new_capacity + 1))
            
            position_numbers_query = (
                select(ShelfPositionNumber)
                .filter(ShelfPositionNumber.number.in_(new_position_numbers_range))
            )
            # V2 FIX
            position_numbers_map = {p.number: p for p in session.execute(position_numbers_query).scalars().all()}

            for position_num in new_position_numbers_range:
                shelf_pos_num_obj = position_numbers_map.get(position_num)
                if not shelf_pos_num_obj:
                    raise InternalServerError(f"ShelfPositionNumber for position {position_num} not found in database.")
                
                new_position = ShelfPosition(
                    shelf_id=id,
                    shelf_position_number_id=shelf_pos_num_obj.id,
                )
                session.add(new_position)

    # --- END: LOGIC TO HANDLE SHELF CAPACITY CHANGES ---

    # Apply all other attribute changes from the request.
    for key, value in mutated_data.items():
        setattr(existing_shelf, key, value)

    # Update the timestamp and commit all changes (deletes, adds, updates).
    setattr(existing_shelf, "update_dt", datetime.now(timezone.utc))
    session.add(existing_shelf)
    session.commit()
    session.refresh(existing_shelf)

    # Re-calculate available space now that positions have changed.
    if hasattr(existing_shelf, 'calc_available_space'):
        existing_shelf.calc_available_space(session=session)
        session.add(existing_shelf)
        session.commit()
        session.refresh(existing_shelf)

    return existing_shelf


@router.delete("/{id}")
def delete_shelf(id: int, session: Session = Depends(get_session)):
    """
    Delete a shelf by its ID.
    """
    shelf = session.get(Shelf, id)

    if shelf:
        session.delete(shelf)
        session.commit()

        return HTTPException(
            status_code=204, detail=f"Shelf ID {id} Deleted Successfully"
        )

    raise NotFound(detail=f"Shelf ID {id} Not Found")