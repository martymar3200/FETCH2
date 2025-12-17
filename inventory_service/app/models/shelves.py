# /app/models/shelves.py - FINAL WITH CALC_AVAILABLE_SPACE

import uuid
import importlib # <--- Required for dynamic import
import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship, Session
from sqlalchemy import Integer, SmallInteger, VARCHAR, TIMESTAMP, ForeignKey, Numeric, String, CheckConstraint, text, select, func, or_ # <--- Added func, or_
from sqlalchemy.schema import UniqueConstraint

from typing import Optional, List
from datetime import datetime, timezone
from pydantic import condecimal 

from app.database.base import Base 
from app.models.owners import Owner
from app.models.ladders import Ladder
from app.models.container_types import ContainerType
from app.models.shelf_numbers import ShelfNumber
from app.models.shelf_types import ShelfType
from app.database.session import session_manager # <--- Required for session context


class Shelf(Base): 
    """
    Model to represent the shelves table.
    """
    # CRITICAL FIX: Override Base's faulty pluralization to match existing DB table name
    __tablename__ = "shelves" 

    __table_args__ = (
        UniqueConstraint(
            "ladder_id", "shelf_number_id", name="uq_ladder_id_shelf_number_id"
        ),
        UniqueConstraint("barcode_id"),
        UniqueConstraint(
            "location",
            "internal_location",
            name="uq_location_internal_location"
        ),
        UniqueConstraint(
            "ladder_id",
            "sort_priority",
            name="uq_ladder_id_sort_priority"
        )
    )

    # Primary Key
    id: Mapped[Optional[int]] = mapped_column(Integer, primary_key=True)

    # Simple Fields
    available_space: Mapped[int] = mapped_column(SmallInteger, default=0, nullable=False)
    location: Mapped[Optional[str]] = mapped_column(String(175), nullable=True, unique=True, default=None)
    internal_location: Mapped[Optional[str]] = mapped_column(String(200), nullable=True, unique=True, default=None)

    # Foreign Key (UUID)
    barcode_id: Mapped[Optional[uuid.UUID]] = mapped_column(ForeignKey("barcodes.id"), nullable=True, unique=True)
    
    # Numeric Fields
    height: Mapped[condecimal] = mapped_column(Numeric(precision=4, scale=2), nullable=False)
    width: Mapped[condecimal] = mapped_column(Numeric(precision=4, scale=2), nullable=False)
    depth: Mapped[condecimal] = mapped_column(Numeric(precision=4, scale=2), nullable=False)
    
    sort_priority: Mapped[Optional[int]] = mapped_column(SmallInteger, nullable=True, default=None)
    
    # Foreign Keys (Integer)
    container_type_id: Mapped[int] = mapped_column(ForeignKey(ContainerType.__table__.c.id), nullable=False)
    shelf_number_id: Mapped[int] = mapped_column(ForeignKey(ShelfNumber.__table__.c.id), nullable=False)
    shelf_type_id: Mapped[int] = mapped_column(ForeignKey(ShelfType.__table__.c.id), nullable=False)
    owner_id: Mapped[Optional[int]] = mapped_column(ForeignKey(Owner.__table__.c.id), nullable=True)
    ladder_id: Mapped[int] = mapped_column(ForeignKey(Ladder.__table__.c.id), nullable=False)
    
    # --- RELATIONSHIPS ---
    ladder: Mapped[Ladder] = relationship(back_populates="shelves")
    owner: Mapped[Optional[Owner]] = relationship(back_populates="shelves")
    barcode: Mapped[Optional["Barcode"]] = relationship(uselist=False)
    shelf_type: Mapped[ShelfType] = relationship(back_populates="shelves")
    shelf_number: Mapped[ShelfNumber] = relationship(back_populates="shelves")
    
    container_type: Mapped[ContainerType] = relationship(
        back_populates="shelves", 
        foreign_keys=[container_type_id]
    )
    
    shelf_positions: Mapped[List["ShelfPosition"]] = relationship(back_populates="shelf")

    # --- METHODS RESTORED ---

    def calc_available_space(self, session: Optional[Session] = None) -> int:
        """
        Calculates available space. Avoids N+1 select issues.
        """
        # Dynamic import to avoid circular dependency at module level
        ShelfPosition = importlib.import_module("app.models.shelf_positions").ShelfPosition
        
        # Check if session is valid
        if session is None or not session.is_active:
            with session_manager() as new_session:
                return self._calculate_space(new_session, ShelfPosition)
        else:
            return self._calculate_space(session, ShelfPosition)

    def _calculate_space(self, session: Session, ShelfPosition):
        # Get total positions count
        total_positions_query = select(func.count(ShelfPosition.id)).where(ShelfPosition.shelf_id == self.id)
        total_positions = session.execute(total_positions_query).scalar() or 0

        # Get occupied positions count (where tray OR non_tray_item is present)
        # Note: In V2, we check if the relationship is NOT NULL. 
        # This requires ShelfPosition to have 'tray' and 'non_tray_item' relationships defined.
        occupied_positions_query = (
            select(func.count(ShelfPosition.id))
            .where(ShelfPosition.shelf_id == self.id)
            .where(
                or_(
                    ShelfPosition.tray != None,
                    ShelfPosition.non_tray_item != None
                )
            )
        )
        occupied_positions = session.execute(occupied_positions_query).scalar() or 0

        self.available_space = total_positions - occupied_positions
        return self.available_space

    def update_shelf_address(self, session: Optional[Session] = None) -> str:
        if session and not self.ladder:
            session.refresh(self)

        shelf_number = self.shelf_number.number
        ladder = self.ladder
        ladder_number = self.ladder.ladder_number.number
        side = self.ladder.side
        side_orientation = self.ladder.side.side_orientation.name
        aisle = self.ladder.side.aisle
        aisle_number = self.ladder.side.aisle.aisle_number.number
        module = self.ladder.side.aisle.module
        building = self.ladder.side.aisle.module.building

        self.location = (
            f"{building.name}-{module.module_number}-{aisle_number}-"
            f"{side_orientation[0]}-{ladder_number}-{shelf_number}"
        )

        self.internal_location = (
            f"{building.id}-{module.id}-{aisle.id}-{side.id}"
            f"-{ladder.id}-{self.id}"
        )