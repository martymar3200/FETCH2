# /app/models/shelf_positions.py - FINAL CLEANUP

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship, Session
from sqlalchemy import BigInteger, Integer, String, VARCHAR, ForeignKey
from sqlalchemy.schema import UniqueConstraint

from typing import Optional, TYPE_CHECKING
from datetime import datetime, timezone

from app.database.base import Base

# --- CRITICAL: DEFER IMPORTS TO BREAK CIRCULARITY ---
if TYPE_CHECKING:
    from app.models.trays import Tray
    from app.models.non_tray_items import NonTrayItem
# -----------------------------------------------------

# --- ABSOLUTE FK IMPORTS ---
from app.models.shelves import Shelf
from app.models.shelf_position_numbers import ShelfPositionNumber


class ShelfPosition(Base): 
    """
    Model to represent the shelf positions table.
    """
    __tablename__ = "shelf_positions"

    __table_args__ = (
        UniqueConstraint(
            "shelf_id",
            "shelf_position_number_id",
            name="uq_shelf_id_shelf_position_number_id",
        ),
    )

    # Primary Key
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)

    # Location Fields
    location: Mapped[Optional[str]] = mapped_column(String(175), nullable=True, unique=True, default=None)
    internal_location: Mapped[Optional[str]] = mapped_column(String(200), nullable=True, unique=True, default=None)

    # Foreign Keys - CRITICAL: ABSOLUTE FK FIX (These MUST remain absolute)
    shelf_position_number_id: Mapped[int] = mapped_column(ForeignKey(ShelfPositionNumber.__table__.c.id), nullable=False)
    shelf_id: Mapped[int] = mapped_column(ForeignKey(Shelf.__table__.c.id), nullable=False)
    
    # --- RELATIONSHIPS ---
    # The simple, standard back_populates definition
    shelf_position_number: Mapped["ShelfPositionNumber"] = relationship(
        back_populates="shelf_positions"
    )
    shelf: Mapped["Shelf"] = relationship(back_populates="shelf_positions")
    
    # One-to-One Relationships (Uses string forward references)
    tray: Mapped[Optional["Tray"]] = relationship(
        back_populates="shelf_position",
        uselist=False
    )
    non_tray_item: Mapped[Optional["NonTrayItem"]] = relationship(
        back_populates="shelf_position",
        uselist=False
    )

    # --- CUSTOM METHOD ---
    def update_position_address(self, session: Optional[Session] = None) -> str: 
        if session and not self.shelf:
            session.refresh(self)

        # Assuming the necessary nested relationships (ladder, side, aisle, module, building) are loaded
        shelf_number = self.shelf.shelf_number.number
        ladder = self.shelf.ladder
        ladder_number = self.shelf.ladder.ladder_number.number
        side = self.shelf.ladder.side
        side_orientation = self.shelf.ladder.side.side_orientation.name
        aisle = self.shelf.ladder.side.aisle
        aisle_number = self.shelf.ladder.side.aisle.aisle_number.number
        module = self.shelf.ladder.side.aisle.module
        building = self.shelf.ladder.side.aisle.module.building

        self.location = (
            f"{building.name}-{module.module_number}-{aisle_number}-"
            f"{side_orientation[0]}-{ladder_number}-{shelf_number}-{self.shelf_position_number.number}"
        )

        self.internal_location = (
            f"{building.id}-{module.id}-{aisle.id}-{side.id}"
            f"-{ladder.id}-{self.shelf.id}-{self.id}"
        )