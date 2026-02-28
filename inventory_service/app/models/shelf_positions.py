# /app/models/shelf_positions.py - REFACTORED: Removed ShelfPositionNumber lookup table dependency

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship, Session
from sqlalchemy import BigInteger, Integer, SmallInteger, String, VARCHAR, ForeignKey
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


class ShelfPosition(Base): 
    """
    Model to represent the shelf positions table.
    """
    __tablename__ = "shelf_positions"

    __table_args__ = (
        UniqueConstraint(
            "shelf_id",
            "position_number",
            name="uq_shelf_id_position_number",
        ),
    )

    # Primary Key
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)

    # Location Fields removed (now properties)

    # Direct integer column (replaces shelf_position_number_id FK)
    position_number: Mapped[int] = mapped_column(SmallInteger, nullable=False)

    # Foreign Keys
    shelf_id: Mapped[int] = mapped_column(ForeignKey(Shelf.__table__.c.id), nullable=False)
    
    # --- RELATIONSHIPS ---
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
    @property
    def location(self) -> str: 
        shelf = self.shelf
        if not shelf: return "Unknown"
        ladder = shelf.ladder
        if not ladder: return "Unknown"
        side = ladder.side
        if not side: return "Unknown"
        aisle = side.aisle
        if not aisle: return "Unknown"
        module = aisle.module
        if not module: return "Unknown"
        building = module.building
        if not building: return "Unknown"

        return (
            f"{building.name}-{module.module_number}-{aisle.aisle_number}-"
            f"{side.side_orientation.name[0]}-{ladder.ladder_number}-{shelf.shelf_number}-{self.position_number}"
        )

    @property
    def internal_location(self) -> str:
        shelf = self.shelf
        if not shelf: return "Unknown"
        ladder = shelf.ladder
        if not ladder: return "Unknown"
        side = ladder.side
        if not side: return "Unknown"
        aisle = side.aisle
        if not aisle: return "Unknown"
        module = aisle.module
        if not module: return "Unknown"
        building = module.building
        if not building: return "Unknown"

        return (
            f"{building.id}-{module.id}-{aisle.id}-{side.id}"
            f"-{ladder.id}-{shelf.id}-{self.id}"
        )